package com.redhat.rhosak.acl;

import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.Rhosak;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;

import static org.apache.kafka.common.acl.AclOperation.DESCRIBE;
import static org.apache.kafka.common.acl.AclPermissionType.ALLOW;

@CommandLine.Command(name = "create", mixinStandardHelpOptions = true, description = "Create a Kafka ACL")
public class KafkaAclCreateCommand extends CustomCommand implements Callable<Integer> {
    private static final String TOPIC_NAME = "test2";

    @CommandLine.Option(names = "--operation", paramLabel = "string", required = false, description = "Set the ACL operation. Choose from: \"all\", \"alter\", \"alter-configs\", \"create\", \"delete\", \"describe\", \"describe-configs\", \"read\", \"write\"")
    String operation;

    @CommandLine.Option(names = "--permission", paramLabel = "string", required = false, description = "Set the ACL permission. Choose from: \"allow\", \"deny\"")
    String permission;

    @CommandLine.Option(names = "--service-account", paramLabel = "string", description = "Service account client ID used as principal for this operation")
    String serviceAccountClientID;

    @CommandLine.Option(names = "--topic", paramLabel = "string", defaultValue = TOPIC_NAME, description = "Set the topic resource")
    String topicResource;

    public KafkaAclCreateCommand() {
    }

    @Override
    public Integer call() {
        try {
            ServiceAccount sa = Rhosak.loadServiceAccountFromFile();
            String principal = "User:" + sa.getClientId();
            if (principal == null) {
                throw new RuntimeException("Principal should not be null!");
            }
            AdminClient adminClient = getAdminClient();
            ResourcePattern resourcePattern = new ResourcePattern(ResourceType.TOPIC, topicResource, PatternType.LITERAL);
            AccessControlEntry accessControlEntry = new AccessControlEntry(principal, "*", DESCRIBE, ALLOW);
            AclBinding aclBinding = new AclBinding(resourcePattern, accessControlEntry);

//        AclPermissionType permType = AclPermissionType.ALLOW;

//        aclBinding.setPermission(permType);                // Permission
//        aclBinding.setResourceName(topicResource);         // Topic instance name
//        aclBinding.setResourceType(AclResourceType.TOPIC); // Topic
//        aclBinding.setPrincipal("User:*");   // User:...
//        aclBinding.setOperation(AclOperation.ALL);         // Operation
//        aclBinding.setPatternType(AclPatternType.LITERAL); // Pattern Type

            Collection<AclBinding> aclBindings = new HashSet<>();
            aclBindings.add(aclBinding);
            CreateAclsResult res = adminClient.createAcls(aclBindings);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
