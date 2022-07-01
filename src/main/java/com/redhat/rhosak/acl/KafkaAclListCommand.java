package com.redhat.rhosak.acl;

import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.exception.NoKafkaInstanceFoundException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsOptions;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@CommandLine.Command(name = "list", mixinStandardHelpOptions = true, description = "Create a Kafka ACL")
public class KafkaAclListCommand extends CustomCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        AclBindingFilter filter = new AclBindingFilter(ResourcePatternFilter.ANY, AccessControlEntryFilter.ANY);

        //todo: Add options to filter

        try {
            AdminClient adminClient = null;
            try {
                adminClient = getAdminClient();
            } catch (NoKafkaInstanceFoundException | IOException e) {
                System.err.println(e.getLocalizedMessage());
                return -1;
            }
            if (adminClient == null) return -1;
            Collection<AclBinding> aclBindings = adminClient.describeAcls(filter, new DescribeAclsOptions().timeoutMs(5_000)).values().get();

            System.out.printf("  PRINCIPAL %5s   PERMISSION   OPERATION          DESCRIPTION              \n", "(" + aclBindings.size() + ")");
            System.out.print(" ----------------- ------------ ------------------ ------------------------- \n");
            for (AclBinding ab : aclBindings) {
                AccessControlEntry entry = ab.entry();
                System.out.printf("  %-17s %-12s %-18s %s is %s\n",
                        entry.principal(),
                        entry.permissionType(),
                        entry.operation(),
                        ab.pattern().resourceType(), ab.pattern().name());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
