package com.redhat.rhosak.service.registry.artifact;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create", mixinStandardHelpOptions = true, description = "Create new artifact")
class ServiceRegistryArtifactCreateCommand implements Callable<Integer> {

    public ServiceRegistryArtifactCreateCommand() {
    }

    @Override
    public Integer call() {

        return 0;
    }
}

