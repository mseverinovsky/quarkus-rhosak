package com.redhat.rhosak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.rhosak.KeycloakInstance;
import com.redhat.rhosak.RhoasTokens;
import org.keycloak.adapters.installed.KeycloakInstalled;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "login", mixinStandardHelpOptions = true, description = "Login into RHOSAK")
public class LoginCommand implements Callable<Integer> {

    private final ObjectMapper objectMapper;
    private final KeycloakInstalled keycloak;

    public LoginCommand() {
        this.objectMapper = new ObjectMapper();
        this.keycloak = KeycloakInstance.getKeycloakInstance();
    }

    @CommandLine.Option(names = "-tf, --tokens-file", paramLabel = "tokens-file", description = "File for storing obtained tokens.",
            defaultValue = RhosakFiles.DEFAULT_CREDENTIALS_FILENAME)
    Path tokensPath;

    @Override
    public Integer call() throws IOException {
        try {
            keycloak.loginDesktop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        storeTokenResponse(keycloak);
        return 0;
    }

    private void storeTokenResponse(KeycloakInstalled keycloak) throws IOException {
        RhoasTokens rhoasTokens = new RhoasTokens();
        rhoasTokens.setRefreshToken(keycloak.getRefreshToken());
        rhoasTokens.setAccessToken(keycloak.getTokenString());
        long timeMillis = System.currentTimeMillis();
        rhoasTokens.refresh_expiration = timeMillis + keycloak.getTokenResponse().getRefreshExpiresIn() * 1000;
        rhoasTokens.access_expiration = timeMillis + keycloak.getTokenResponse().getExpiresIn() * 1000;
        objectMapper.writeValue(tokensPath.toFile(), rhoasTokens);
    }
}
