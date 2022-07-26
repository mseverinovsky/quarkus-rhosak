package com.redhat.rhosak;

import com.openshift.cloud.api.kas.auth.invoker.auth.OAuth;

public class KafkaInstanceClient {

    private static com.openshift.cloud.api.kas.auth.invoker.ApiClient kafkaInstanceAPIClient;

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
