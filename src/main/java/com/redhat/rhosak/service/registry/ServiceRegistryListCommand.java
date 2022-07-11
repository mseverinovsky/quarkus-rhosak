package com.redhat.rhosak.service.registry;

import com.redhat.rhosak.KafkaInstanceClient;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "list", mixinStandardHelpOptions = true)
class ServiceRegistryListCommand implements Callable<Integer> {

//    private final ApiClient apiInstanceClient;
//    private final SecurityApi securityAPI;



    public ServiceRegistryListCommand() {
//        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
//        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());

    }

    @Override
    public Integer call() {

        String accessToken = KafkaInstanceClient.checkTokenExpirationAndGetNewOne();

        // perform REST API call
        // https://api.openshift.com/api/serviceregistry_mgmt/v1/registries


        return 0;
    }
}

