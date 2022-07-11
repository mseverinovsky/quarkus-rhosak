package com.redhat.rhosak.service.registry;

import com.redhat.rhosak.service.registry.artifact.ServiceRegistryArtifactCommand;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "service-registry", mixinStandardHelpOptions = true,
        subcommands = {ServiceRegistryDescribeCommand.class, ServiceRegistryListCommand.class,
                ServiceRegistryArtifactCommand.class})
public class ServiceRegistryCommand implements Callable<Integer> {
    public ServiceRegistryCommand() {}

    @Override
    public Integer call() {

        return 0;
    }
}


