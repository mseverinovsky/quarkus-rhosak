package com.redhat.rhosak.service.registry.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
import com.openshift.cloud.api.kas.auth.invoker.ApiException;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.KafkaInstanceClient;
import picocli.CommandLine;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.redhat.rhosak.KafkaManagementClient.API_CLIENT_BASE_PATH;

@CommandLine.Command(name = "get", mixinStandardHelpOptions = true,
        description = "Get an artifact from the registry based on the artifact ID")
public class ServiceRegistryArtifactGetCommand extends CustomCommand implements Callable<Integer> {

    private final String ARTIFACT_GET_URL = "/apis/registry/v2/groups/default/artifacts/";
    private final ApiClient apiInstanceClient;

    @CommandLine.Option(names = {"--artifact-id", "--id"}, paramLabel = "string", required = true, description = "ID of the artifact")
    String artifactId;

    public ServiceRegistryArtifactGetCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
    }

    @Override
    public Integer call() {
        apiInstanceClient.setBasePath(API_CLIENT_BASE_PATH);
        try {
            // Get Service Registry list
            Map<String, Object> res = apiInstanceClient.invokeAPI(
                    SERVICE_REGISTRY_MGMT_URL, "GET", null, null,
                    new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, APPLICATION_X_WWW_FORM_URLENCODED,
                    new String[]{"Bearer"}, new GenericType<>() {}
            );

            if ((res.get("items")) == null || ((ArrayList)res.get("items")).size() == 0) {
                System.err.println(">>> No Service Registries found!");
                return -1;
            } else {
                // Get first list element
                LinkedHashMap<String, Object> map = ((ArrayList<LinkedHashMap<String, Object>>)res.get("items")).get(0);
                // Get registry Url
                String registryUrl = (String) map.get("registryUrl");
                apiInstanceClient.setBasePath(registryUrl);

                Map<String, Object> artifactsMap = apiInstanceClient.invokeAPI(
                        ARTIFACT_GET_URL + artifactId, "GET", null, null,
                        new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, APPLICATION_X_WWW_FORM_URLENCODED,
                        new String[]{"Bearer"}, new GenericType<>() {}
                );
                if (artifactsMap == null || artifactsMap.size() == 0) {
                    System.err.println(">>> No artifacts found!");
                    return -1;
                } else {
                    String registryId = (String) map.get("id");
                    System.out.println("=================================================");
                    System.out.println("Registry ID: " + registryId);
                    System.out.println("Artifact ID: " + artifactId);
                    System.out.println("=================================================");
                    System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(artifactsMap));
                }
            }
        } catch (ApiException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}

