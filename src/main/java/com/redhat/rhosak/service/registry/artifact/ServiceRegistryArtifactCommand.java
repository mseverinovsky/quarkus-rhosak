package com.redhat.rhosak.service.registry.artifact;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "artifact", mixinStandardHelpOptions = true, subcommands = {
        ServiceRegistryArtifactCreateCommand.class, ServiceRegistryArtifactGetCommand.class,
        ServiceRegistryArtifactDeleteCommand.class, ServiceRegistryArtifactDownloadCommand.class,
        ServiceRegistryArtifactListCommand.class, ServiceRegistryArtifactUpdateCommand.class},
        description = "Manage Service Registry schema and API artifacts")
public class ServiceRegistryArtifactCommand implements Callable<Integer> {

    public ServiceRegistryArtifactCommand() {}

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);

        return 0;
    }
}
