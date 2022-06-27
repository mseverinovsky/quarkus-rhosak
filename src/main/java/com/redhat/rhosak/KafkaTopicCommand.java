package com.redhat.rhosak;

import com.openshift.cloud.api.kas.auth.TopicsApi;
import com.openshift.cloud.api.kas.auth.models.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@Command(name = "topic", mixinStandardHelpOptions = true, description = "Create, describe, update, list, and delete topics",
        subcommands = {KafkaTopicCreateCommand.class, KafkaTopicListCommand.class, KafkaTopicDeleteCommand.class})
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
            String accessToken = KafkaInstanceClient.checkTokenExpirationAndGetNewOne();
            createInstanceTopic(accessToken, topicName, retentionMs);
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private void createInstanceTopic(String accessToken, String topicName, String retentionMs) throws com.openshift.cloud.api.kas.auth.invoker.ApiException {
        NewTopicInput topicInput = createTopicInput(topicName, retentionMs);
        String serverUrl = getServerUrl();

        if (accessToken == null || accessToken.equals("")) {
            accessToken = rhosakApiToken();
        }

        apiInstanceClient.setBasePath(serverUrl);
        apiInstanceClient.setAccessToken(accessToken);

        try {
            Topic topic = apiInstanceTopic.createTopic(topicInput);
            System.out.println(">>> topic: " + topic);
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

@Command(name = "list", mixinStandardHelpOptions = true)
class KafkaTopicListCommand extends CustomCommand implements Callable<Integer> {

    private final com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient;
    private final TopicsApi apiInstanceTopic;

    public KafkaTopicListCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
        this.apiInstanceTopic = new TopicsApi(apiInstanceClient);
    }

    @Override
    public Integer call() {
        String accessToken = KafkaInstanceClient.checkTokenExpirationAndGetNewOne();
        listTopics(accessToken);
        return 0;
    }

    private void listTopics(String accessToken) {
        String serverUrl = getServerUrl();

        if (accessToken == null || accessToken.equals("")) {
            accessToken = rhosakApiToken();
        }

        apiInstanceClient.setBasePath(serverUrl);
        apiInstanceClient.setAccessToken(accessToken);

        try {
            TopicsList topicsList = apiInstanceTopic.getTopics(null, null, null, null, null);
            System.out.print(
                "  NAME             PARTITIONS   RETENTION TIME (MS)   RETENTION SIZE (BYTES)  \n" +
                " ---------------- ------------ --------------------- ------------------------ \n"
            );
            for (Topic topic : topicsList.getItems()) {
                String retentionSize = Objects.requireNonNull(topic.getConfig()).get(21).getValue();
                if (retentionSize.equals("-1")) retentionSize += " (Unlimited)";
                System.out.format(
                "  %-15s%11d%18s%25s\n",
                        topic.getName(),
                        Objects.requireNonNull(topic.getPartitions()).size(),         // Partitions
                        Objects.requireNonNull(topic.getConfig()).get(9).getValue(),  // Retention time in milliseconds
                        retentionSize                                                 // retention size. Unlimited if == -1
                );
            }
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

@Command(name = "delete", mixinStandardHelpOptions = true)
class KafkaTopicDeleteCommand extends CustomCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        return 0;
    }
}


