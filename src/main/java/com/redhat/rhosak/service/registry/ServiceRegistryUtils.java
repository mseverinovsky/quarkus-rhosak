package com.redhat.rhosak.service.registry;

import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
import com.openshift.cloud.api.kas.auth.invoker.ApiException;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.KafkaInstanceClient;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.redhat.rhosak.KafkaManagementClient.API_CLIENT_BASE_PATH;

public class ServiceRegistryUtils extends CustomCommand {

    private static final ApiClient apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();

    public ServiceRegistryUtils() {
    }

    public static ServiceRegistryDTO getServiceRegistry() {

        ServiceRegistryDTO dto = null;
        apiInstanceClient.setBasePath(API_CLIENT_BASE_PATH);
        try {
            // Get Service Registry list
            Map<String, Object> res = apiInstanceClient.invokeAPI(
                    SERVICE_REGISTRY_MGMT_URL, "GET", null, null,
                    new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, CONTENT_TYPE_APPLICATION_JSON,
                    new String[]{"Bearer"}, new GenericType<>() {}
            );
            if ((res.get("items")) == null || ((ArrayList)res.get("items")).size() == 0) {
                System.err.println(">>> No Service Registries found!");
            } else {
                // Get first list element
                LinkedHashMap<String, Object> map = ((ArrayList<LinkedHashMap<String, Object>>) res.get("items")).get(0);

                dto = new ServiceRegistryDTO();
                dto.setId((String) map.get("id"));
                dto.setName((String) map.get("name"));
                dto.setOwner((String) map.get("owner"));
                dto.setStatus((String) map.get("status"));
                dto.setRegistryUrl((String) map.get("registryUrl"));
            }
        } catch (ApiException e) {
                throw new RuntimeException(e);
        }

        return dto;
    }
}
