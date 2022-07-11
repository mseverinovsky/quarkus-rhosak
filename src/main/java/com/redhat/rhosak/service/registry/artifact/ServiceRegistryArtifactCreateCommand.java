package com.redhat.rhosak.service.registry.artifact;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "list", mixinStandardHelpOptions = true)
class ServiceRegistryArtifactCreateCommand implements Callable<Integer> {

//    private final ApiClient apiInstanceClient;
//    private final SecurityApi securityAPI;



    public ServiceRegistryArtifactCreateCommand() {
//        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
//        this.securityAPI = new SecurityApi(KafkaManagementClient.getKafkaManagementAPIClient());

    }

    @Override
    public Integer call() {
//        String registryUrl = "https://bu98.serviceregistry.rhcloud.com/t/a2a99e47-3ea4-4f11-ba80-89ed30b57b6b";
//        RegistryClient client = RegistryClientFactory.create(registryUrl);
//        client.createArtifact()

        return 0;
    }
}

