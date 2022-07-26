package com.redhat.rhosak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.DefaultApi;
import com.openshift.cloud.api.kas.SecurityApi;
import com.openshift.cloud.api.kas.invoker.ApiClient;
import com.openshift.cloud.api.kas.invoker.ApiException;
import com.openshift.cloud.api.kas.models.KafkaRequest;
import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.redhat.rhosak.exception.NoKafkaInstanceFoundException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomCommand {
    private final ObjectMapper objectMapper;
//    private final DefaultApi managementApi;

    public static final String SERVICE_REGISTRY_MGMT_URL = "/api/serviceregistry_mgmt/v1/registries";
    private static final String ARTIFACT_METADATA_URL = "/apis/registry/v2/groups/default/artifacts/%s/meta";

    public static final String ACCEPT_APPLICATION_JSON = "application/json";
    public static final String ACCEPT_APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x_www_form_urlencoded";

    public CustomCommand() {
        this.objectMapper = new ObjectMapper();
//        ApiClient apiManagementClient = KafkaManagementClient.getKafkaManagementAPIClient();
//        this.managementApi = new DefaultApi(apiManagementClient);
    }

    protected Map<String, Object> getArtifactMetadata(String registryUrl, Object artifactId,
                                                      com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient)
            throws com.openshift.cloud.api.kas.auth.invoker.ApiException {
        apiInstanceClient.setBasePath(registryUrl);
        String url = String.format(ARTIFACT_METADATA_URL, artifactId);
        return apiInstanceClient.invokeAPI(
                url, "GET", null, null,
                new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, APPLICATION_X_WWW_FORM_URLENCODED,
                new String[]{"Bearer"}, new GenericType<>() {}
        );
    }

    protected int printArtifactMetadata(String id, LinkedHashMap<String, Object> map, Map<String, Object> artifactsMap)
            throws JsonProcessingException {
        if (artifactsMap == null || artifactsMap.size() == 0) {
            System.err.println(">>> No artifacts found!");
            return -1;
        } else {
            String registryId = (String) map.get("id");
            System.out.println("=================================================");
            System.out.println("Registry ID: " + registryId);
            System.out.println("Artifact ID: " + id);
            System.out.println("=================================================");
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(artifactsMap));
        }
        return 0;
    }

    protected void saveServiceAccountToFile(com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient,
                                            ServiceAccount serviceAccount) throws IOException {
        System.out.println(">>> Saving Service account...");
        Path saFile = Path.of(RhosakFiles.SA_FILE_NAME);
        serviceAccount.setCreatedAt(null); // otherwise .rhosak_sa file will be broken
        objectMapper.writeValue(saFile.toFile(), serviceAccount);
    }

    protected String getServerUrl() throws NoKafkaInstanceFoundException {
        return "https://admin-server-" + getBootstrapServerUrl();
    }

    protected String getBootstrapServerUrl() throws NoKafkaInstanceFoundException {
        try {
            ApiClient apiManagementClient = KafkaManagementClient.getKafkaManagementAPIClient();
            DefaultApi managementApi = new DefaultApi(apiManagementClient);
            List<KafkaRequest> list = managementApi.getKafkas(null, null, null, null).getItems();
            if (list.isEmpty()) {
                throw new NoKafkaInstanceFoundException(">>> No Kafka instance found!");
            }
            return list.get(0).getBootstrapServerHost();
        } catch (ApiException e) {
            throw new RuntimeException("Cannot get kafka url", e.getCause());
        }
    }

    protected AdminClient getAdminClient() throws IOException, NoKafkaInstanceFoundException {
        ServiceAccount sa = Rhosak.loadServiceAccountFromFile();
        String userName = sa.getClientId();
        String password = sa.getClientSecret();
        String serverUrl = getBootstrapServerUrl();
        Map<String, Object> configMap = new HashMap<>() {{
            put("security.protocol", "SASL_SSL");
            put("sasl.mechanism", "PLAIN");
            put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required "
                    + " username=\"" + userName + "\""
                    + " password=\"" + password + "\";");
            put("bootstrap.servers", serverUrl);
        }};

        Map<String, Object> conf = new HashMap<>(configMap);
        conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");

        return AdminClient.create(conf);
    }

    protected boolean checkServiceAccountExists(String serviceAccountId) {
        SecurityApi securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
        try {
            return securityAPI.getServiceAccountById(serviceAccountId) != null;
        } catch (ApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected ServiceAccount getServiceAccountById(String serviceAccountId) {
        SecurityApi securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
        try {
            return securityAPI.getServiceAccountById(serviceAccountId);
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String getPrincipal(String serviceAccountId) {
        String principal;
        ServiceAccount sa;
        if (serviceAccountId == null) {
            try {
                System.out.println("No principal specified. Trying to load from file ...");
                sa = Rhosak.loadServiceAccountFromFile();
                if (!checkServiceAccountExists(sa.getId())) {
                    System.err.println("Principal not found. Id: " + sa.getId());
                    return null;
                }
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
                return null;
            }
        } else {
            sa = getServiceAccountById(serviceAccountId);
//            System.err.println(serviceAccountId);
//            System.err.println(sa);
            if (sa == null) {
                System.err.println("Principal not found. Id: " + serviceAccountId);
                return null;
            }
        }

        principal = "User:" + sa.getName();
//        principal = "User:" + sa.getId();

        return principal;
    }
}
