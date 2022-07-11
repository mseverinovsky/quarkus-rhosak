package com.redhat.rhosak.service.registry;

import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.KafkaInstanceClient;
import picocli.CommandLine;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.redhat.rhosak.KafkaManagementClient.API_CLIENT_BASE_PATH;

@CommandLine.Command(name = "create", mixinStandardHelpOptions = true, description = "Create a Service Registry instance")
class ServiceRegistryCreateCommand extends CustomCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--name", paramLabel = "string", required = true, description = "Unique ID of the Service Registry instance")
    String name;

    private final ApiClient apiInstanceClient;

    public ServiceRegistryCreateCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
        apiInstanceClient.setBasePath(API_CLIENT_BASE_PATH);
    }

    @Override
    public Integer call() {
        Map<String, Object> formParametersMap = new HashMap<>() {{
            put("name", name);
        }};
        GenericType<Map<String, Object>> returnTypeClass = new GenericType<>() {};
        Map<String, Object> res;
        try {
            res = apiInstanceClient.invokeAPI(
                    SERVICE_REGISTRY_MGMT_URL,
                    "POST",
                    new ArrayList<>(),
                    formParametersMap,
                    new HashMap<>(),
                    new HashMap<>(),
                    null,
                    "*/*",
                    CONTENT_TYPE_APPLICATION_JSON,
                    new String[]{"Bearer"},
                    returnTypeClass
            );

            System.out.println("Response: " + res);
            System.out.println(" ============= Service Registry created ============= ");
            System.out.println("  id: " + res.get("id"));
            System.out.println("  name: " + res.get("name"));
            System.out.println("  owner: " + res.get("owner"));
            System.out.println("  status: " + res.get("status"));

        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            if (e.getCode() == 409) {
                System.err.println(">>> User already has the maximum number of allowed Evaluation instances!");
                return -1;
            } else {
                throw new RuntimeException(e);
            }
        }

        return 0;
    }
}
