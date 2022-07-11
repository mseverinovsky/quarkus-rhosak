package com.redhat.rhosak.service.registry.artifact;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "artifact", mixinStandardHelpOptions = true)
public class ServiceRegistryArtifactCommand implements Callable<Integer> {

    public ServiceRegistryArtifactCommand() {}

    @Override
    public Integer call() {
//        String registryUrl = "https://bu98.serviceregistry.rhcloud.com/t/a2a99e47-3ea4-4f11-ba80-89ed30b57b6b";
//        RegistryClient client = RegistryClientFactory.create(registryUrl);
//        client.createArtifact()

        return 0;
    }
}

