package com.redhat.rhosak;

import org.keycloak.adapters.installed.KeycloakInstalled;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "login", mixinStandardHelpOptions = true, description = "Login into RHOSAK")
public class LoginCommand implements Callable<Integer> {

    private final KeycloakInstalled keycloak;

    public LoginCommand() {
        this.keycloak = KeycloakInstance.getKeycloakInstance();
    }

    @Override
    public Integer call() throws IOException {
        try {
            keycloak.loginDesktop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Rhosak.storeTokenResponse(keycloak);
        return 0;
    }
}
