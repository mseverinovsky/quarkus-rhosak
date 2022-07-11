package com.redhat.rhosak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.invoker.ApiClient;
import com.openshift.cloud.api.kas.invoker.Configuration;
import com.openshift.cloud.api.kas.invoker.auth.HttpBearerAuth;
import org.keycloak.adapters.installed.KeycloakInstalled;

import java.nio.file.Path;
import java.time.Duration;

public class KafkaManagementClient {

    public static final String API_CLIENT_BASE_PATH = "https://api.openshift.com";

    private static final Duration MIN_TOKEN_VALIDITY = Duration.ofSeconds(30);
    private static ApiClient kafkaManagementAPIClientInstance;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final KeycloakInstalled keycloak = KeycloakInstance.getKeycloakInstance();

    private KafkaManagementClient() {}

    public static ApiClient getKafkaManagementAPIClient() {
        if (kafkaManagementAPIClientInstance == null) {
            kafkaManagementAPIClientInstance = Configuration.getDefaultApiClient();
            kafkaManagementAPIClientInstance.setBasePath(API_CLIENT_BASE_PATH);
            String tokenString = getBearerToken();

            // Configure HTTP bearer authorization: Bearer
            HttpBearerAuth bearer = (HttpBearerAuth) kafkaManagementAPIClientInstance.getAuthentication("Bearer");
            bearer.setBearerToken(tokenString);
        }
        return kafkaManagementAPIClientInstance;
    }

    private static RhoasTokens getStoredTokenResponse() {
        try {
            return objectMapper.readValue(
                    Path.of(RhosakFiles.DEFAULT_CREDENTIALS_FILENAME).toFile(),
                    RhoasTokens.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static String getBearerToken() {
        try {
            RhoasTokens storedTokens = getStoredTokenResponse();
            String accessToken;

            // ensure token is valid for at least 30 seconds
            if (storedTokens != null && storedTokens.accessTokenIsValidFor(MIN_TOKEN_VALIDITY)) {
                accessToken = storedTokens.getAccess_token();
            } else if (storedTokens != null && storedTokens.refreshTokenIsValidFor(MIN_TOKEN_VALIDITY)) {
                keycloak.refreshToken(storedTokens.getRefresh_token());
                Rhosak.storeTokenResponse(keycloak);
                accessToken = keycloak.getTokenString();
            } else {
                storedTokens = new RhoasTokens();
                keycloak.loginDesktop();
                storedTokens.setAccess_token(keycloak.getTokenString());
                Rhosak.storeTokenResponse(keycloak);
                accessToken = keycloak.getTokenString();
            }
            return accessToken;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
