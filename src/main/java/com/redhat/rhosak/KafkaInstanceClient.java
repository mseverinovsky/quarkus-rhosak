package com.redhat.rhosak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.cloud.api.kas.auth.invoker.auth.OAuth;

public class KafkaInstanceClient {

    private static final String API_INSTANCE_CLIENT_BASE_PATH = "https://identity.api.openshift.com";
    private static com.openshift.cloud.api.kas.auth.invoker.ApiClient kafkaInstanceAPIClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private KafkaInstanceClient() {
    }

    public static com.openshift.cloud.api.kas.auth.invoker.ApiClient getKafkaInstanceAPIClient() {
        if (kafkaInstanceAPIClient == null) {
            kafkaInstanceAPIClient = com.openshift.cloud.api.kas.auth.invoker.Configuration.getDefaultApiClient();
            String tokenString = KafkaManagementClient.getBearerToken();

            OAuth bearer = (OAuth) kafkaInstanceAPIClient.getAuthentication("Bearer");
            bearer.setAccessToken(tokenString);
        }
        return kafkaInstanceAPIClient;
    }
}
