package com.redhat.rhosak.service.registry.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
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

@CommandLine.Command(name = "metadata-get", mixinStandardHelpOptions = true,
        description = "Get the metadata for an artifact in a Service Registry instance.")
public class ServiceRegistryArtifactMetadataGetCommand extends CustomCommand implements Callable<Integer> {

    private final String ARTIFACT_METADATA_URL = "/apis/registry/v2/groups/default/artifacts/%s/meta";
    private final ApiClient apiInstanceClient;

    @CommandLine.Option(names = {"--artifact-id", "--id"}, paramLabel = "string", required = true, description = "ID of the artifact")
    String artifactId;

    public ServiceRegistryArtifactMetadataGetCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
    }

    @Override
    public Integer call() {
        apiInstanceClient.setBasePath(API_CLIENT_BASE_PATH);
        try {
            // Get Service Registry list
            Map<String, Object> serviceRegistryResultMap = apiInstanceClient.invokeAPI(
                    SERVICE_REGISTRY_MGMT_URL, "GET", null, null,
                    new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, APPLICATION_X_WWW_FORM_URLENCODED,
                    new String[]{"Bearer"}, new GenericType<>() {}
            );

            if ((serviceRegistryResultMap.get("items")) == null || ((ArrayList)serviceRegistryResultMap.get("items")).size() == 0) {
                System.err.println(">>> No Service Registries found!");
                return -1;
            } else {
                // Get first list element
                LinkedHashMap<String, Object> map = ((ArrayList<LinkedHashMap<String, Object>>)serviceRegistryResultMap.get("items")).get(0);
                // Get registry Url
                String registryUrl = (String) map.get("registryUrl");

                Map<String, Object> artifactsMap = getArtifactMetadata(registryUrl, artifactId, apiInstanceClient);
                return printArtifactMetadata(artifactId, map, artifactsMap);
            }
        } catch (ApiException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

