package com.redhat.rhosak.service.registry.artifact;

import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
import com.openshift.cloud.api.kas.auth.invoker.ApiException;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.KafkaInstanceClient;
import com.redhat.rhosak.service.registry.ServiceRegistryDTO;
import com.redhat.rhosak.service.registry.ServiceRegistryUtils;
import picocli.CommandLine;

import javax.ws.rs.core.GenericType;
import java.util.HashMap;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete", mixinStandardHelpOptions = true, description = "Delete the artifact")
public class ServiceRegistryArtifactDeleteCommand extends CustomCommand implements Callable<Integer> {

    private final ApiClient apiInstanceClient;

    @CommandLine.Option(names = {"--artifact-id", "--id"}, required = true, paramLabel = "string", description = "ID of the artifact")
    String artifactId;

    public ServiceRegistryArtifactDeleteCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
    }

    @Override
    public Integer call() {
        ServiceRegistryDTO serviceRegistry = ServiceRegistryUtils.getServiceRegistry();
        String registryUrl = serviceRegistry.getRegistryUrl();
        apiInstanceClient.setBasePath(registryUrl);
        final String ARTIFACT_DELETE_URL = "/apis/registry/v2/groups/default/artifacts/";

        try {
            apiInstanceClient.invokeAPI(
                    ARTIFACT_DELETE_URL + artifactId, "DELETE", null, null,
                    new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, CONTENT_TYPE_APPLICATION_JSON,
                    new String[]{"Bearer"}, new GenericType<>() {}
            );
            System.out.println(">>> Artifact deleted. Id: " + artifactId);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return 0;
    }
}

