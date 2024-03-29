package com.redhat.rhosak;

import com.openshift.cloud.api.kas.SecurityApi;
import com.openshift.cloud.api.kas.invoker.ApiException;
import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.openshift.cloud.api.kas.models.ServiceAccountListItem;
import com.openshift.cloud.api.kas.models.ServiceAccountRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "service-account", mixinStandardHelpOptions = true,
        description = "Create, list, describe, delete service accounts",
        subcommands = {ServiceAccountCreateCommand.class, ServiceAccountListCommand.class,
                ServiceAccountResetCredentialsCommand.class, ServiceAccountDescribeCommand.class,
                ServiceAccountDeleteCommand.class})
public class ServiceAccountCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }
}

@Command(name = "create", mixinStandardHelpOptions = true, description = "Create a service account")
class ServiceAccountCreateCommand extends CustomCommand implements Callable<Integer> {
    private final com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient;
    private final SecurityApi securityAPI;

    public ServiceAccountCreateCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
    }

    @CommandLine.Option(names = "--name", paramLabel = "string", description = "Service account name")
    String name;

    @CommandLine.Option(names = {"--short-description", "--descr"}, paramLabel = "string", description = "Short description of the service account")
    String shortDescription;

    @Override
    public Integer call() {
        try {
            ServiceAccount serviceAccount = createServiceAccount(name, shortDescription);
            saveServiceAccountToFile(apiInstanceClient, serviceAccount);
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
}

@Command(name = "list", mixinStandardHelpOptions = true, description = "List all service accounts")
class ServiceAccountListCommand implements Callable<Integer> {

    private final SecurityApi securityAPI;

    public ServiceAccountListCommand() {
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
    }

    @Override
    public Integer call() {
        try {
            List<ServiceAccountListItem> list = securityAPI.getServiceAccounts(null).getItems();
            System.err.println(">>> Service accounts count: " + list.size());
            System.out.println("===================================================");
            list.forEach(item -> {
                prettyPrintItem(item);
                System.out.println("---------------------------------------------------");
            });
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    private static void prettyPrintItem(ServiceAccountListItem item) {
        System.out.println("         id: " + item.getId());
        System.out.println("       kind: " + item.getKind());
        System.out.println("       href: " + item.getHref());
        System.out.println("   clientId: " + item.getClientId());
        System.out.println("       name: " + item.getName());
        System.out.println("  createdBy: " + item.getCreatedBy());
        System.out.println("  createdAt: " + item.getCreatedAt());
        System.out.println("description: " + item.getDescription());
    }
}

@Command(name = "delete", mixinStandardHelpOptions = true, description = "Delete a service account")
class ServiceAccountDeleteCommand implements Callable<Integer> {

    private final SecurityApi securityAPI;

    @CommandLine.Option(names = "--id", paramLabel = "string", required = true, description = "The unique ID of the service account to delete")
    String id;

    public ServiceAccountDeleteCommand() {
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
    }

    @Override
    public Integer call() {
        try {
            securityAPI.deleteServiceAccountById(id);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}

@Command(name = "describe", aliases = {"get"}, mixinStandardHelpOptions = true, description = "View configuration details for a service account")
class ServiceAccountDescribeCommand implements Callable<Integer> {

    private final SecurityApi securityAPI;

    @CommandLine.Option(names = "--id", paramLabel = "string", required = true, description = "The unique ID of the service account to view")
    String id;

    public ServiceAccountDescribeCommand() {
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
    }

    @Override
    public Integer call() {
        try {
            ServiceAccount serviceAccount = securityAPI.getServiceAccountById(id);
            System.out.print("\n============= Service Account =============");
            String saLines = serviceAccount.toString()
                    .replaceFirst("class ServiceAccount \\{","")
                    .replaceAll("\\n[\\s]+","\n")
                    .replaceFirst("\\}","");
            String[] lines = saLines.split("\\n");
            for (String line : lines) {
                int n = line.indexOf(':');
                if (n > 0) line = " ".repeat(20 - n) + line;
                System.out.println(line);
            }
        } catch (ApiException e) {
            if (e.getMessage().contains("\"reason\":\"service account not found")) {
                System.err.println(">>> Service account not found! Id: " + id);
                return -1;
            } else {
                throw new RuntimeException(e);
            }
        }

        return 0;
    }
}

@Command(name = "reset-credentials", aliases = {"reset"}, mixinStandardHelpOptions = true, description = "Reset service account credentials")
class ServiceAccountResetCredentialsCommand extends CustomCommand implements Callable<Integer> {

    private final SecurityApi securityAPI;
    private final com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient;

    @CommandLine.Option(names = "--id", paramLabel = "string", required = true, description = "The unique ID of the service account to reset credentials")
    String id;

    public ServiceAccountResetCredentialsCommand() {
        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
    }

    @Override
    public Integer call() {
        try {
            ServiceAccount serviceAccount = securityAPI.resetServiceAccountCreds(id);
            saveServiceAccountToFile(apiInstanceClient, serviceAccount);
        } catch (ApiException | IOException e) {
            System.err.println(">>> ApiException: " + e.getLocalizedMessage());
        }
        return 0;
    }
}
