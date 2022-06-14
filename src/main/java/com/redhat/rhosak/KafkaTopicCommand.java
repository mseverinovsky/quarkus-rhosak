package com.redhat.rhosak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.auth.TopicsApi;
import com.openshift.cloud.api.kas.auth.models.ConfigEntry;
import com.openshift.cloud.api.kas.auth.models.NewTopicInput;
import com.openshift.cloud.api.kas.auth.models.Topic;
import com.openshift.cloud.api.kas.auth.models.TopicSettings;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "topic", mixinStandardHelpOptions = true, description = "Create, describe, update, list, and delete topics",
        subcommands = KafkaTopicCreateCommand.class)
public class KafkaTopicCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        return 0;
    }
}

@Command(name = "create", mixinStandardHelpOptions = true)
class KafkaTopicCreateCommand extends CustomCommand implements Callable<Integer> {

    private final ObjectMapper objectMapper;
    private final com.openshift.cloud.api.kas.auth.invoker.ApiClient apiInstanceClient;
    private final TopicsApi apiInstanceTopic;

    public KafkaTopicCreateCommand() {
        this.objectMapper = new ObjectMapper();
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
        this.apiInstanceTopic = new TopicsApi(apiInstanceClient);
    }

    @CommandLine.Option(names = "--name", paramLabel = "string", required = true, description = "Topic name")
    String topicName;

    @Override
    public Integer call() {
        try {
            KafkaInstanceClient.checkTokenExpirationAndGotNewOne();
            System.out.println(createInstanceTopic(topicName));
        } catch (com.openshift.cloud.api.kas.auth.invoker.ApiException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private Topic createInstanceTopic(String topicName) throws com.openshift.cloud.api.kas.auth.invoker.ApiException {
        NewTopicInput topicInput = getTopicInput(topicName);
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

    private NewTopicInput getTopicInput(String topicName) {
        NewTopicInput topicInput = new NewTopicInput();
        topicInput.setName(topicName);
        TopicSettings ts = new TopicSettings();
        List<ConfigEntry> list = new ArrayList<>();
        ts.setConfig(list);
        ts.setNumPartitions(1);
        topicInput.setSettings(ts);
        return topicInput;
    }

    private String rhosakApiToken() {
        try {
            RhoasTokens tokens = objectMapper.readValue(Path.of(RhosakFiles.RHOSAK_API_CREDS_FILE_NAME + ".json").toFile(), RhoasTokens.class);
            return tokens.access_token;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

