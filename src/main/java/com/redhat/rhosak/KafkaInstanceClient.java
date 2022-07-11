package com.redhat.rhosak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.auth.invoker.auth.OAuth;
import com.openshift.cloud.api.kas.models.ServiceAccount;

import javax.ws.rs.core.GenericType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KafkaInstanceClient {

    private static final String API_INSTANCE_CLIENT_BASE_PATH = "https://identity.api.openshift.com";
    private static com.openshift.cloud.api.kas.auth.invoker.ApiClient kafkaInstanceAPIClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private KafkaInstanceClient() {
    }

    public static com.openshift.cloud.api.kas.auth.invoker.ApiClient getKafkaInstanceAPIClient() {
        if (kafkaInstanceAPIClient == null) {
            kafkaInstanceAPIClient = com.openshift.cloud.api.kas.auth.invoker.Configuration.getDefaultApiClient();
            kafkaInstanceAPIClient.setBasePath(API_INSTANCE_CLIENT_BASE_PATH);
            String tokenString = KafkaManagementClient.getBearerToken();

            OAuth bearer = (OAuth) kafkaInstanceAPIClient.getAuthentication("Bearer");
            bearer.setAccessToken(tokenString);
        }
        return kafkaInstanceAPIClient;
    }

    public static String checkTokenExpirationAndGetNewOne() {
        String accessToken;
        try {
            File apiCredsFile = Path.of(RhosakFiles.RHOSAK_API_CREDS_FILE_NAME + ".json").toFile();
            if (!apiCredsFile.exists()) {
                throw new RuntimeException(apiCredsFile.getName() + " does not exist. Try to create service account first");
            }
            RhoasTokens tokens = objectMapper.readValue(apiCredsFile, RhoasTokens.class);
            String[] parts = tokens.getAccess_token().split("\\.");

            ServiceAccount serviceAccount = Rhosak.loadServiceAccountFromFile();
            JsonNode readValue = objectMapper.readValue(decode(parts[1]), JsonNode.class);

            JsonNode expiration = readValue.get("exp");
            if (expiration.asLong() < (System.currentTimeMillis() / 1000)) {
                Map<String, Object> formParametersMap = new HashMap<>() {{
                    put("grant_type", "client_credentials");
                    put("client_id", serviceAccount.getClientId());
                    put("client_secret", serviceAccount.getClientSecret());
                    put("scope", "openid");
                }};
                GenericType<Map<String, String>> genericType = new GenericType<>() {
                };
                try {
                    Map<String, String> res = kafkaInstanceAPIClient.invokeAPI(
                            "/auth/realms/rhoas/protocol/openid-connect/token",
                            "POST",
                            null,
                            null,
                            new HashMap<>(),
                            new HashMap<>(),
                            formParametersMap,
                            "application/json",
                            "application/x-www-form-urlencoded",
                            new String[]{"Bearer"},
                            genericType
                    );

                    Path apiTokensFile = Path.of(RhosakFiles.RHOSAK_API_CREDS_FILE_NAME + ".json");
                    accessToken = res.get("access_token");
                    tokens.setAccess_token(accessToken);
                    objectMapper.writeValue(apiTokensFile.toFile(), tokens);
                } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                accessToken = tokens.getAccess_token();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return accessToken;
    }


    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }
}
