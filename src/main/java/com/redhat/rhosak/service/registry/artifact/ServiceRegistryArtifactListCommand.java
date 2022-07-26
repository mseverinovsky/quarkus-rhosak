package com.redhat.rhosak.service.registry.artifact;

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

@CommandLine.Command(name = "list", mixinStandardHelpOptions = true, description = "List all artifacts")
public class ServiceRegistryArtifactListCommand extends CustomCommand implements Callable<Integer> {

    private final String ARTIFACT_LIST_URL = "/apis/registry/v2/search/artifacts?group=default";
    private final ApiClient apiInstanceClient;

    public ServiceRegistryArtifactListCommand() {
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
                        ARTIFACT_LIST_URL, "GET", null, null,
                        new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, APPLICATION_X_WWW_FORM_URLENCODED,
                        new String[]{"Bearer"}, new GenericType<>() {}
                );
                if ((artifactsMap.get("artifacts")) == null || ((ArrayList)artifactsMap.get("artifacts")).size() == 0) {
                    System.err.println(">>> No artifacts found!");
                    return -1;
                } else {
                    ArrayList<LinkedHashMap<String, String>> artifacts = (ArrayList<LinkedHashMap<String, String>>) artifactsMap.get("artifacts");
                    System.out.printf("  ID (%3d)                               NAME        CREATED ON                 CREATED BY                       TYPE   STATE\n", artifacts.size());
                    System.out.println(" -------------------------------------- ----------- -------------------------- -------------------------------- ------ ---------");
                    for (LinkedHashMap<String, String> item : artifacts) {
                        System.out.printf("  %36s %11s   %s   %s  %5s   %s\n",
                                item.get("id"),
                                (item.get("name") == null) ? "" : item.get("name"),
                                item.get("createdOn"),
                                item.get("createdBy"),
                                item.get("type"),
                                item.get("state")
                        );
                    }
                }
            }

        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}

