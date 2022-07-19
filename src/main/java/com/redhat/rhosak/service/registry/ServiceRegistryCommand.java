package com.redhat.rhosak.service.registry;

import com.redhat.rhosak.service.registry.artifact.ServiceRegistryArtifactCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "service-registry", mixinStandardHelpOptions = true,
        subcommands = {ServiceRegistryArtifactCommand.class, ServiceRegistryConfigCommand.class,
                ServiceRegistryCreateCommand.class, ServiceRegistryDeleteCommand.class,
                ServiceRegistryDescribeCommand.class, ServiceRegistryListCommand.class})
public class ServiceRegistryCommand implements Callable<Integer> {
    public ServiceRegistryCommand() {}

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }
}


