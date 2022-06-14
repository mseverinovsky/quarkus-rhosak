package com.redhat.rhosak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.SecurityApi;
import com.openshift.cloud.api.kas.invoker.ApiException;
import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.openshift.cloud.api.kas.models.ServiceAccountRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "service-account", mixinStandardHelpOptions = true, subcommands = {ServiceAccountCreateCommand.class, ServiceAccountListCommand.class, ServiceAccountResetCredentialsCommand.class, ServiceAccountDeleteCommand.class})
public class ServiceAccountCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        return 0;
    }
}

@Command(name = "create", mixinStandardHelpOptions = true)
class ServiceAccountCreateCommand implements Callable<Integer> {
    private final ObjectMapper objectMapper;
    private final com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient;
    private final SecurityApi securityAPI;

    public ServiceAccountCreateCommand() {
        this.objectMapper = new ObjectMapper();
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
    }

    @CommandLine.Option(names = "--name", paramLabel = "string", description = "Service account name")
    String name;
    @CommandLine.Option(names = "--file-format", paramLabel = "string", description = "Format in which to save the service account credentials (default: \"json\")", defaultValue = "json")
    String fileFormat;

    @CommandLine.Option(names = "--short-description", paramLabel = "string", description = "Short description of the service account")
    String shortDescription;

    @Override
    public Integer call() {
        try {
            saveServiceAccountToFile(
                    createServiceAccount(name, shortDescription)
            );
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return 0;
    }

    private ServiceAccount createServiceAccount(String name, String shortDescription) {
        ServiceAccountRequest serviceAccountRequest = new ServiceAccountRequest();
        serviceAccountRequest.setName(name);
        serviceAccountRequest.setDescription(shortDescription);

        try {
            return securityAPI.createServiceAccount(serviceAccountRequest);
        } catch (ApiException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void saveServiceAccountToFile(ServiceAccount serviceAccount) throws IOException {
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
            tokens.access_token = res.get("access_token");
            objectMapper.writeValue(apiTokensFile.toFile(), tokens);
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e);
        }
    }
}

@Command(name = "list", mixinStandardHelpOptions = true)
class ServiceAccountListCommand implements Callable<Integer> {

    private final SecurityApi securityAPI;

    public ServiceAccountListCommand() {
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
    }

    @Override
    public Integer call() {
        try {
            System.out.println(securityAPI.getServiceAccounts(null).getItems());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}

@Command(name = "delete", mixinStandardHelpOptions = true)
class ServiceAccountDeleteCommand implements Callable<Integer> {

    private final SecurityApi securityAPI;

    @CommandLine.Option(names = "--id", paramLabel = "string", required = true, description = "The unique ID of the service account to delete")
    String name;

    public ServiceAccountDeleteCommand() {
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
    }

    @Override
    public Integer call() {
        try {
            securityAPI.deleteServiceAccountById(name);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}

@Command(name = "reset-credentials", mixinStandardHelpOptions = true)
class ServiceAccountResetCredentialsCommand implements Callable<Integer> {

    private final SecurityApi securityAPI;

    @CommandLine.Option(names = "--id", paramLabel = "string", required = true, description = "The unique ID of the service account to delete")
    String name;

    public ServiceAccountResetCredentialsCommand() {
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
    }

    @Override
    public Integer call() {
        try {
            securityAPI.resetServiceAccountCreds(name);
        } catch (ApiException e) {
            System.err.println("ApiException: " + e.getLocalizedMessage());
//            e.printStackTrace();
        }
        return 0;
    }
}
