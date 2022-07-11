package com.redhat.rhosak;

import com.openshift.cloud.api.kas.DefaultApi;
import com.openshift.cloud.api.kas.invoker.ApiException;
import com.openshift.cloud.api.kas.models.Error;
import com.openshift.cloud.api.kas.models.KafkaRequest;
import com.openshift.cloud.api.kas.models.KafkaRequestPayload;
import com.redhat.rhosak.acl.KafkaAclCommand;
import com.redhat.rhosak.conf.KafkaConfigCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Objects;
import java.util.concurrent.Callable;

@Command(name = "kafka", mixinStandardHelpOptions = true, description = "Create, view and manage your Kafka instances",
        subcommands = {KafkaCreateCommand.class, KafkaListCommand.class, KafkaDeleteCommand.class,
                KafkaTopicCommand.class, KafkaAclCommand.class, KafkaConfigCommand.class})
public class KafkaCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        return 0;
    }
}

@Command(name = "create", mixinStandardHelpOptions = true, description = "Create kafka instance")
class KafkaCreateCommand implements Callable<Integer> {

    private final DefaultApi managementAPI;

    public KafkaCreateCommand() {
        this.managementAPI =
                new DefaultApi(
                        KafkaManagementClient.getKafkaManagementAPIClient()
                );
    }

    @CommandLine.Option(names = "--name", paramLabel = "string", required = true, description = "Name of the kafka instance")
    String instanceName;

    @Override
    public Integer call() {
        if (Objects.nonNull(instanceName)) {
            System.out.println(createInstance(instanceName));
        }

        return 0;
    }

    private KafkaRequest createInstance(String name) {
        KafkaRequestPayload kafkaRequestPayload = new KafkaRequestPayload();
        kafkaRequestPayload.setName(name);

        try {
            return managementAPI.createKafka(true, kafkaRequestPayload);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#createKafka");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            throw new RuntimeException(e.getMessage());
        }
    }
}

@Command(name = "list", mixinStandardHelpOptions = true, description = "List all kafka instances")
class KafkaListCommand implements Callable<Integer> {

    private final DefaultApi managementAPI;

    public KafkaListCommand() {
        this.managementAPI =
                new DefaultApi(
                        KafkaManagementClient.getKafkaManagementAPIClient()
                );
    }

    @Override
    public Integer call() {
        try {
            System.out.println(managementAPI.getKafkas(null, null, null, null).getItems());
        } catch (ApiException e) {
            throw new RuntimeException(e.getMessage());
        }
        return 0;
    }
}

@Command(name = "delete", mixinStandardHelpOptions = true, description = "Delete a kafka instance")
class KafkaDeleteCommand implements Callable<Integer> {

    private final DefaultApi managementAPI;

    @CommandLine.Option(names = "--id", paramLabel = "string", required = true, description = "Unique ID of the Kafka instance you want to delete")
    String kafkaId;

    public KafkaDeleteCommand() {
        this.managementAPI =
                new DefaultApi(
                        KafkaManagementClient.getKafkaManagementAPIClient()
                );
    }

    @Override
    public Integer call() {
        try {
            Error error = managementAPI.deleteKafkaById(kafkaId, true);
            if (error != null) {
                System.err.println(error);
            }
        } catch (ApiException e) {
            if (e.getCode() == 404 ) {
                System.err.println("Kafka instance not found. Id: " + kafkaId);
            } else {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return 0;
    }
}
