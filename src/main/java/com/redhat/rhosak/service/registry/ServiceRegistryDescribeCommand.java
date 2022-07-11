package com.redhat.rhosak.service.registry;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "describe", mixinStandardHelpOptions = true)
class ServiceRegistryDescribeCommand implements Callable<Integer> {

    public ServiceRegistryDescribeCommand() {}

    @Override
    public Integer call() {

        return 0;
    }
}
