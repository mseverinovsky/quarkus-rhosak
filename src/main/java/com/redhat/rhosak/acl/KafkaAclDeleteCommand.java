package com.redhat.rhosak.acl;

import com.redhat.rhosak.CustomCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

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


        return 0;
    }
}
