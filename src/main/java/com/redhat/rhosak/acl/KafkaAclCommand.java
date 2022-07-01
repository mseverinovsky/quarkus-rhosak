package com.redhat.rhosak.acl;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;


@Command(name = "acl", mixinStandardHelpOptions = true,
        description = "Manage Kafka ACLs for users and service accounts",
        subcommands = {KafkaAclCreateCommand.class, KafkaAclListCommand.class, KafkaAclDeleteCommand.class})
public class KafkaAclCommand implements Callable<Integer> {

    public KafkaAclCommand() {}

    @Override
    public Integer call() {
        return 0;
    }
}

