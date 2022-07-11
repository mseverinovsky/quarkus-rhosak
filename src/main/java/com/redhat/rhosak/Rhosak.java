///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.keycloak:keycloak-installed-adapter:18.0.0
//DEPS com.redhat.cloud:kafka-management-sdk:0.20.4
//DEPS com.redhat.cloud:kafka-instance-sdk:0.20.4
//DEPS info.picocli:picocli:4.6.3
//DEPS com.fasterxml.jackson.core:jackson-core:2.13.3
//DEPS com.fasterxml.jackson.core:jackson-annotations:2.13.3
//DEPS org.apache.kafka:kafka-clients:3.2.0
//SOURCES RhosakFiles.java
//SOURCES LoginCommand.java
//SOURCES KafkaCommand.java
//SOURCES CustomCommand.java
//SOURCES acl/KafkaAclCommand.java
//SOURCES acl/KafkaAclListCommand.java
//SOURCES acl/KafkaAclCreateCommand.java
//SOURCES acl/KafkaAclDeleteCommand.java
//SOURCES service/registry/ServiceRegistryCommand.java
//SOURCES service/registry/ServiceRegistryListCommand.java
//SOURCES service/registry/ServiceRegistryDescribeCommand.java
//SOURCES service/registry/artifact/ServiceRegistryArtifactCommand.java
//SOURCES conf/KafkaConfigCommand.java
//SOURCES exception/NoKafkaInstanceFoundException.java
//SOURCES KafkaTopicCommand.java
//SOURCES KafkaManagementClient.java
//SOURCES KafkaInstanceClient.java
//SOURCES ServiceAccountCommand.java

package com.redhat.rhosak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.redhat.rhosak.conf.KafkaConfigCommand;
import com.redhat.rhosak.service.registry.ServiceRegistryCommand;
import org.keycloak.adapters.installed.KeycloakInstalled;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Callable;

@Command(name = "rhosak", mixinStandardHelpOptions = true,
        subcommands = {LoginCommand.class, KafkaCommand.class, ServiceAccountCommand.class,
                ServiceRegistryCommand.class, KafkaConfigCommand.class})
public class Rhosak implements Callable<Integer> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Integer call() {
        return 0;
    }

    public static void main(String[] args) {
        Rhosak rhosak = new Rhosak();
        int exitCode = new CommandLine(rhosak).execute(args);

        System.exit(exitCode);
    }

    public static void storeTokenResponse(KeycloakInstalled keycloak) throws IOException {
        Path tokensPath = Path.of(RhosakFiles.DEFAULT_CREDENTIALS_FILENAME);
        RhoasTokens rhoasTokens = new RhoasTokens();
        rhoasTokens.refresh_token = keycloak.getRefreshToken();
        rhoasTokens.access_token = keycloak.getTokenString();
        long timeMillis = System.currentTimeMillis();
        rhoasTokens.refresh_expiration = timeMillis + keycloak.getTokenResponse().getRefreshExpiresIn() * 1000;
        rhoasTokens.access_expiration = timeMillis + keycloak.getTokenResponse().getExpiresIn() * 1000;
        objectMapper.writeValue(tokensPath.toFile(), rhoasTokens);
    }

    public static ServiceAccount loadServiceAccountFromFile() throws IOException {
        File saFile = Path.of(RhosakFiles.SA_FILE_NAME + ".json").toFile();
        if (!saFile.exists()) {
            throw new FileNotFoundException(saFile + " does not exist");
        }
        return objectMapper.readValue(saFile, ServiceAccount.class);
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

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
