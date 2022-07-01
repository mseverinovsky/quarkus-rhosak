package com.redhat.rhosak.acl;

import com.redhat.rhosak.CustomCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete", mixinStandardHelpOptions = true, description = "Delete a Kafka ACL")
public class KafkaAclDeleteCommand  extends CustomCommand implements Callable<Integer> {

    public KafkaAclDeleteCommand() {
    }

    @Override
    public Integer call() {


        return 0;
    }
}
