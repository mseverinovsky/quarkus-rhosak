package com.redhat.rhosak.acl;

import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.exception.NoKafkaInstanceFoundException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@CommandLine.Command(name = "delete", mixinStandardHelpOptions = true, description = "Delete a Kafka ACL")
public class KafkaAclDeleteCommand  extends CustomCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--operation", paramLabel = "string", required = true, description = "Set the ACL operation. Choose from: \"all\", \"alter\", \"alter-configs\", \"create\", \"delete\", \"describe\", \"describe-configs\", \"read\", \"write\"")
    String operation;

    @CommandLine.Option(names = "--permission", paramLabel = "string", required = true, description = "Set the ACL permission. Choose from: \"allow\", \"deny\"")
    String permission;

    @CommandLine.Option(names = "--service-account", paramLabel = "string", defaultValue = "*", description = "Service account client ID used as principal for this operation")
    String serviceAccountId;

    @CommandLine.Option(names = "--topic", paramLabel = "string", defaultValue = "*", description = "Set the topic resource")
    String topicResource;

    public KafkaAclDeleteCommand() {
    }

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

            AclOperation aclOperation = AclOperation.valueOf(operation.toUpperCase(Locale.ROOT));
            AclPermissionType permissionType = AclPermissionType.valueOf(permission.toUpperCase(Locale.ROOT));

            ResourcePatternFilter resourcePatternFilter = new ResourcePatternFilter(ResourceType.TOPIC, topicResource, PatternType.LITERAL);
            AccessControlEntryFilter accessControlEntryFilter = new AccessControlEntryFilter(principal, HOST, aclOperation, permissionType);

            AclBindingFilter aclBindingFilter = new AclBindingFilter(resourcePatternFilter, accessControlEntryFilter);
            Collection<AclBindingFilter> filters = new HashSet<>();
            filters.add(aclBindingFilter);

            adminClient.deleteAcls(filters).values().get(aclBindingFilter).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
