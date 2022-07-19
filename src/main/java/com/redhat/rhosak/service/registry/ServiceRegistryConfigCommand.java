package com.redhat.rhosak.service.registry;

import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
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

@CommandLine.Command(name = "config", mixinStandardHelpOptions = true,
        description = "Generate and print Service Registry configuration")
class ServiceRegistryConfigCommand extends CustomCommand implements Callable<Integer> {

    private final ApiClient apiInstanceClient;

    public ServiceRegistryConfigCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
        apiInstanceClient.setBasePath(API_CLIENT_BASE_PATH);
    }

    @Override
    public Integer call() {
        try {
            Map<String, Object> res = apiInstanceClient.invokeAPI(
                    SERVICE_REGISTRY_MGMT_URL,
                    "GET",
                    null,
                    null,
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    ACCEPT_APPLICATION_JSON,
                    APPLICATION_X_WWW_FORM_URLENCODED,
                    new String[]{"Bearer"},
                    new GenericType<>() {}
            );

            if ((res.get("items")) == null || ((ArrayList)res.get("items")).size() == 0) {
                System.err.println(">>> No Service Registries found!");
                return -1;
            }
            for (LinkedHashMap<String, Object> item : (ArrayList<LinkedHashMap<String, Object>>)res.get("items")) {
                System.out.println("# ===== Service Registry configuration ====================");
                item.forEach((key, value) -> System.out.printf("%s=%s\n", key, value));
            }
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}

