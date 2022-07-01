package com.redhat.rhosak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.DefaultApi;
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
import java.util.List;
import java.util.Map;

public class CustomCommand {
    private final ObjectMapper objectMapper;
    private final ApiClient apiManagementClient;
    private final DefaultApi managementApi;

    public CustomCommand() {
        this.objectMapper = new ObjectMapper();
        this.apiManagementClient = KafkaManagementClient.getKafkaManagementAPIClient();
        this.managementApi = new DefaultApi(apiManagementClient);
    }

    protected void saveServiceAccountToFile(com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient,
                                            ServiceAccount serviceAccount, String fileFormat) throws IOException {
        Path saFile = Path.of(RhosakFiles.SA_FILE_NAME + "." + fileFormat);
        serviceAccount.setCreatedAt(null); // otherwise .rhosak-sa file will be broken
        objectMapper.writeValue(saFile.toFile(), serviceAccount);

        String clientId = serviceAccount.getClientId();
        String clientSecret = serviceAccount.getClientSecret();

        Map<String, Object> formParametersMap = new HashMap<>() {{
            put("grant_type", "client_credentials");
            put("client_id", clientId);
            put("client_secret", clientSecret);
            put("scope", "openid");
        }};
        String acceptString = "application/json";
        String contentTypeString = "application/x-www-form-urlencoded";
        GenericType<Map<String, String>> returnTypeClass = new GenericType<>() {
        };
        String URL = "/auth/realms/rhoas/protocol/openid-connect/token";
        try {
            Map<String, String> res = apiInstanceClient.invokeAPI(URL,
                    "POST",
                    null,
                    null,
                    new HashMap<>(),
                    new HashMap<>(),
                    formParametersMap,
                    acceptString,
                    contentTypeString,
                    new String[]{"Bearer"},
                    returnTypeClass
            );

            Path apiTokensFile = Path.of(RhosakFiles.RHOSAK_API_CREDS_FILE_NAME + "." + fileFormat);
            RhoasTokens tokens = new RhoasTokens();
            tokens.setAccess_token(res.get("access_token"));
            objectMapper.writeValue(apiTokensFile.toFile(), tokens);
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e);
        }
    }

    protected String rhosakApiToken() {
        try {
            RhoasTokens tokens = objectMapper.readValue(Path.of(RhosakFiles.RHOSAK_API_CREDS_FILE_NAME + ".json").toFile(), RhoasTokens.class);
            return tokens.getAccess_token();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getServerUrl() throws NoKafkaInstanceFoundException {
        return "https://admin-server-" + getBootstrapServerUrl();
    }

    protected String getBootstrapServerUrl() throws NoKafkaInstanceFoundException {
        try {
            List<KafkaRequest> list = managementApi.getKafkas(null, null, null, null).getItems();
            if (list.isEmpty()) {
                throw new NoKafkaInstanceFoundException("No Kafka instance found");
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

}
