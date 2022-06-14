package com.redhat.rhosak;

import com.openshift.cloud.api.kas.auth.TopicsApi;
import com.openshift.cloud.api.kas.auth.models.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "topic", mixinStandardHelpOptions = true, description = "Create, describe, update, list, and delete topics",
        subcommands = {KafkaTopicCreateCommand.class/*, KafkaTopicListCommand.class, KafkaTopicDeleteCommand.class*/})
public class KafkaTopicCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        return 0;
    }
}

@Command(name = "create", mixinStandardHelpOptions = true)
class KafkaTopicCreateCommand extends CustomCommand implements Callable<Integer> {

    private static final String DEFAULT_RETENTION_MS = "86400000"; // 24 hours in milliseconds

    private final com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient;
    private final TopicsApi apiInstanceTopic;

    public KafkaTopicCreateCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
        this.apiInstanceTopic = new TopicsApi(apiInstanceClient);
    }

    @CommandLine.Option(names = "--name", paramLabel = "string", required = true, description = "Topic name")
    String topicName;

    @CommandLine.Option(names = "--retention-ms", paramLabel = "string", description = "Retention time in milliseconds")
    String retentionMs;

    @Override
    public Integer call() {
        try {
            KafkaInstanceClient.checkTokenExpirationAndGetNewOne();
            System.out.println(createInstanceTopic(topicName, retentionMs));
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private Topic createInstanceTopic(String topicName, String retentionMs) throws com.openshift.cloud.api.kas.auth.invoker.ApiException {
        NewTopicInput topicInput = createTopicInput(topicName, retentionMs);
        String serverUrl = getServerUrl();

        apiInstanceClient.setBasePath(serverUrl);
        apiInstanceClient.setAccessToken(rhosakApiToken());

        try {
            Topic topic = apiInstanceTopic.createTopic(topicInput);
            System.out.println(">>> topic: " + topic);
            return topic;
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private NewTopicInput createTopicInput(String topicName, String retentionMs) {
        NewTopicInput topicInput = new NewTopicInput();
        topicInput.setName(topicName);
        TopicSettings ts = new TopicSettings();

        ConfigEntry configEntry = new ConfigEntry();
        configEntry.setKey("retention.ms");
        if (retentionMs == null || retentionMs.equals("")) {
            configEntry.setValue(DEFAULT_RETENTION_MS);
        } else {
            configEntry.setValue(retentionMs);
        }
        List<ConfigEntry> list = new ArrayList<>();
        list.add(configEntry);
        ts.setConfig(list);
        ts.setNumPartitions(1);
        topicInput.setSettings(ts);
        return topicInput;
    }

}

