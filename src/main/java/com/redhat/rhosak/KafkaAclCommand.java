package com.redhat.rhosak;

import java.util.concurrent.Callable;

import com.openshift.cloud.api.kas.auth.AclsApi;
import com.openshift.cloud.api.kas.auth.invoker.auth.OAuth;
import com.openshift.cloud.api.kas.auth.models.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;


@Command(name = "acl", mixinStandardHelpOptions = true,
        description = "Manage Kafka ACLs for users and service accounts",
        subcommands = KafkaAclCreateCommand.class)
public class KafkaAclCommand implements Callable<Integer> {

    public KafkaAclCommand() {}

    @Override
    public Integer call() {
        return 0;
    }
}

@Command(name = "create", mixinStandardHelpOptions = true, description = "Create a Kafka ACL")
class KafkaAclCreateCommand extends CustomCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--operation", paramLabel = "string", required = true, description = "Set the ACL operation. Choose from: \"all\", \"alter\", \"alter-configs\", \"create\", \"delete\", \"describe\", \"describe-configs\", \"read\", \"write\"")
    String operation;

    @CommandLine.Option(names = "--permission", paramLabel = "string", required = true, description = "Set the ACL permission. Choose from: \"allow\", \"deny\"")
    String permission;

    @CommandLine.Option(names = "--service-account", paramLabel = "string", description = "Service account client ID used as principal for this operation")
    String serviceAccountClientID;

    @CommandLine.Option(names = "--topic", paramLabel = "string", description = "Set the topic resource")
    String topicResource;

    public KafkaAclCreateCommand() {}

    @Override
    public Integer call() {
        com.openshift.cloud.api.kas.auth.invoker.ApiClient defaultClient =
                com.openshift.cloud.api.kas.auth.invoker.Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        // Configure OAuth2 access token for authorization: Bearer
        OAuth bearer = (OAuth) defaultClient.getAuthentication("Bearer");
        bearer.setAccessToken(KafkaManagementClient.getBearerToken());

        AclsApi aclsApi = new AclsApi(defaultClient);
        aclsApi.getApiClient().setBasePath(getServerUrl());

        AclBinding aclBinding =  new AclBinding();
        AclPermissionType permType = AclPermissionType.ALLOW;

        aclBinding.setPermission(permType);                // Permission
        aclBinding.setResourceName(topicResource);         // Topic instance name
        aclBinding.setResourceType(AclResourceType.TOPIC); // Topic
        aclBinding.setPrincipal("User:*");   // User:...
        aclBinding.setOperation(AclOperation.ALL);         // Operation
        aclBinding.setPatternType(AclPatternType.LITERAL); // Pattern Type
        try {
            aclsApi.createAcl(aclBinding);

        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
