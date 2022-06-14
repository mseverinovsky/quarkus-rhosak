///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.keycloak:keycloak-installed-adapter:18.0.0
//DEPS com.redhat.cloud:kafka-management-sdk:0.20.4
//DEPS com.redhat.cloud:kafka-instance-sdk:0.20.4
//DEPS info.picocli:picocli:4.6.3
//DEPS com.fasterxml.jackson.core:jackson-core:2.13.3
//DEPS com.fasterxml.jackson.core:jackson-annotations:2.13.3
//SOURCES RhosakFiles.java
//SOURCES LoginCommand.java
//SOURCES KafkaCommand.java
//SOURCES KafkaAclCommand.java
//SOURCES KafkaTopicCommand.java
//SOURCES ServiceAccountCommand.java

package com.redhat.rhosak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.DefaultApi;
import com.openshift.cloud.api.kas.auth.invoker.auth.OAuth;
import com.openshift.cloud.api.kas.invoker.ApiClient;
import com.openshift.cloud.api.kas.invoker.ApiException;
import com.openshift.cloud.api.kas.invoker.Configuration;
import com.openshift.cloud.api.kas.invoker.auth.HttpBearerAuth;
import com.openshift.cloud.api.kas.models.ServiceAccount;
import org.keycloak.adapters.installed.KeycloakInstalled;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.ws.rs.core.GenericType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "rhosak", mixinStandardHelpOptions = true,
        subcommands = {LoginCommand.class, KafkaCommand.class, ServiceAccountCommand.class})
public class Rhosak implements Callable<Integer> {

    @Override
    public Integer call() {
        return 0;
    }

    public static void main(String[] args) {
        Rhosak rhosak = new Rhosak();
        int exitCode = new CommandLine(rhosak).execute(args);

        System.exit(exitCode);
    }
}

class CustomCommand {
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
            tokens.setAccessToken(res.get("access_token"));
            objectMapper.writeValue(apiTokensFile.toFile(), tokens);
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e);
        }
    }

    protected String rhosakApiToken() {
        try {
            RhoasTokens tokens = objectMapper.readValue(Path.of(RhosakFiles.RHOSAK_API_CREDS_FILE_NAME + ".json").toFile(), RhoasTokens.class);
            return tokens.getAccessToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getServerUrl() {
        try {
            return "https://admin-server-" + managementApi.getKafkas(null, null, null, null).getItems().get(0).getBootstrapServerHost();
        } catch (ApiException e) {
            throw new RuntimeException("Cannot get kafka url", e.getCause());
        }
    }

}

class KafkaManagementClient {

    private static final String API_CLIENT_BASE_PATH = "https://api.openshift.com";
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

            // ensure token is valid for at least 30 seconds
            if (storedTokens != null && storedTokens.accessTokenIsValidFor(MIN_TOKEN_VALIDITY)) {
                return storedTokens.getAccessToken();
            } else if (storedTokens != null && storedTokens.refreshTokenIsValidFor(MIN_TOKEN_VALIDITY)) {
                keycloak.refreshToken(storedTokens.getRefreshToken());
                return keycloak.getTokenString();
            } else {
                keycloak.loginDesktop();
                return keycloak.getTokenString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class KafkaInstanceClient {

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
            RhoasTokens tokens = objectMapper.readValue(Path.of(RhosakFiles.RHOSAK_API_CREDS_FILE_NAME + ".json").toFile(), RhoasTokens.class);
            String[] parts = tokens.getAccessToken().split("\\.");

            ServiceAccount serviceAccount = objectMapper.readValue(Path.of(RhosakFiles.SA_FILE_NAME + ".json").toFile(), ServiceAccount.class);
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
                    tokens.setAccessToken(accessToken);
                    objectMapper.writeValue(apiTokensFile.toFile(), tokens);
                } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                accessToken = tokens.getAccessToken();
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

class KeycloakInstance {
    private static KeycloakInstalled keycloak;

    private KeycloakInstance() {}

    public static KeycloakInstalled getKeycloakInstance() {
        if (keycloak == null) {
            String initialString = "{\n" +
                    "  \"realm\": \"redhat-external\",\n" +
                    "  \"auth-server-url\": \"https://sso.redhat.com/auth/\",\n" +
                    "  \"ssl-required\": \"external\",\n" +
                    "  \"resource\": \"rhoas-cli-prod\",\n" +
                    "  \"public-client\": true,\n" +
                    "  \"confidential-port\": 0,\n" +
                    "  \"use-resource-role-mappings\": true,\n" +
                    "  \"enable-pkce\": true\n" +
                    "}\n";

            InputStream config = new ByteArrayInputStream(initialString.getBytes());
            keycloak = new KeycloakInstalled(config);
        }
        return keycloak;
    }
}

class RhoasTokens {

    public String refresh_token;
    public String access_token;
    public Long access_expiration;
    public Long refresh_expiration;

    boolean accessTokenIsValidFor(Duration duration) {
        return (access_expiration) - duration.toMillis() >= System.currentTimeMillis();
    }

    boolean refreshTokenIsValidFor(Duration duration) {
        return (refresh_expiration) - duration.toMillis() >= System.currentTimeMillis();
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refresh_token = refreshToken;
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }
}
