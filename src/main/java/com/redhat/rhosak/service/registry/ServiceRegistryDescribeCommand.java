package com.redhat.rhosak.service.registry;

import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.KafkaInstanceClient;
import picocli.CommandLine;

import javax.ws.rs.core.GenericType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.redhat.rhosak.KafkaManagementClient.API_CLIENT_BASE_PATH;

@CommandLine.Command(name = "describe", aliases = {"get"}, mixinStandardHelpOptions = true, description = "Describe a Service Registry instance.")
class ServiceRegistryDescribeCommand extends CustomCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--id", paramLabel = "string", required = true, description = "Unique ID of the Service Registry instance")
    String id;

    private final ApiClient apiInstanceClient;

    public ServiceRegistryDescribeCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
        apiInstanceClient.setBasePath(API_CLIENT_BASE_PATH);
    }

    @Override
    public Integer call() {
        Map<String, Object> formParametersMap = new HashMap<>();
        GenericType<Map<String, Object>> returnTypeClass = new GenericType<>() {};
        try {
            Map<String, Object> res = apiInstanceClient.invokeAPI(
                    SERVICE_REGISTRY_MGMT_URL + "/" + id,
                    "GET",
                    null,
                    null,
                    new HashMap<>(),
                    new HashMap<>(),
                    formParametersMap,
                    ACCEPT_APPLICATION_JSON,
                    APPLICATION_X_WWW_FORM_URLENCODED,
                    new String[]{"Bearer"},
                    returnTypeClass
            );

            if (res == null || res.size() == 0) {
                System.err.println(">>> Service registry not found! Id: " + id);
                return -1;
            }
            System.out.println("============= Service Registry ============= ");
            res.forEach((key, value) -> System.out.printf("%20s: %s\n", key, value));
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            if (e.getCode() == 404) {
                System.err.println(">>> Service registry not found! Id: " + id);
                return -1;
            } else {
                throw new RuntimeException(e);
            }
        }

        return 0;
    }
}
