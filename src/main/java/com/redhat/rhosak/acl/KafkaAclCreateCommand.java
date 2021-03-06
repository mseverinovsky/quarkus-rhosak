package com.redhat.rhosak.acl;

import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.exception.NoKafkaInstanceFoundException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@CommandLine.Command(name = "create", mixinStandardHelpOptions = true, description = "Create a Kafka ACL")
public class KafkaAclCreateCommand extends CustomCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--operation", paramLabel = "string", required = true, description = "Set the ACL operation. Choose from: \"all\", \"alter\", \"alter-configs\", \"create\", \"delete\", \"describe\", \"describe-configs\", \"read\", \"write\"")
    String operation;

    @CommandLine.Option(names = "--permission", paramLabel = "string", required = true, description = "Set the ACL permission. Choose from: \"allow\", \"deny\"")
    String permission;

    @CommandLine.Option(names = "--service-account", paramLabel = "string", description = "Service account ID used as principal for this operation. Ex.(dd71f41b-262e-4443-b7b0-80c3cd79dcbb)")
    String serviceAccountId;

    @CommandLine.Option(names = "--topic", paramLabel = "string", defaultValue = "*", description = "Set the topic resource")
    String topicResource;

    public KafkaAclCreateCommand() {}

    @Override
    public Integer call() {
        final String HOST = "*";
        try {
            String principal = getPrincipal(serviceAccountId);
            if (principal == null) return -1;

            AdminClient adminClient;
            try {
                adminClient = getAdminClient();
            } catch (NoKafkaInstanceFoundException | IOException e) {
                System.err.println(e.getLocalizedMessage());
                return -1;
            }
            ResourcePattern resourcePattern = new ResourcePattern(ResourceType.TOPIC, topicResource, PatternType.LITERAL);
            AclOperation aclOperation = AclOperation.valueOf(operation.toUpperCase(Locale.ROOT));
            AclPermissionType permissionType = AclPermissionType.valueOf(permission.toUpperCase(Locale.ROOT));
            AccessControlEntry accessControlEntry = new AccessControlEntry(principal, HOST, aclOperation, permissionType);
            AclBinding aclBinding = new AclBinding(resourcePattern, accessControlEntry);

            Collection<AclBinding> aclBindings = new HashSet<>();
            aclBindings.add(aclBinding);
            adminClient.createAcls(aclBindings).values().get(aclBinding).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
