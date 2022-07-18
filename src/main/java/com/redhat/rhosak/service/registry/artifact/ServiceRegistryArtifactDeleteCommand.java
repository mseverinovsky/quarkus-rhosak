package com.redhat.rhosak.service.registry.artifact;

import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
import com.openshift.cloud.api.kas.auth.invoker.ApiException;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.KafkaInstanceClient;
import com.redhat.rhosak.service.registry.ServiceRegistryDTO;
import com.redhat.rhosak.service.registry.ServiceRegistryUtils;
import picocli.CommandLine;

import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete", mixinStandardHelpOptions = true, description = "Delete the artifact")
class ServiceRegistryArtifactDeleteCommand extends CustomCommand implements Callable<Integer> {

    private final ApiClient apiInstanceClient;

    @CommandLine.Option(names = "--file", paramLabel = "string", description = "File location of the artifact")
    String fileName;

    public ServiceRegistryArtifactDeleteCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
    }

    @Override
    public Integer call() {

        String body;
        try {
            if (fileName == null) {
                // read from System.in
                body = new String(System.in.readAllBytes());
            } else {
                // read the artifact from file
                body = new String(Files.readAllBytes(Paths.get(fileName)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ServiceRegistryDTO serviceRegistry = ServiceRegistryUtils.getServiceRegistry();
        String registryUrl = serviceRegistry.getRegistryUrl();
        apiInstanceClient.setBasePath(registryUrl);
        final String ARTIFACT_CREATE_URL = "/apis/registry/v2/groups/default/artifacts";

        try {
            Map<String, Object> artifactsMap = apiInstanceClient.invokeAPI(
                    ARTIFACT_CREATE_URL, "POST", null, body,
                    new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, CONTENT_TYPE_APPLICATION_JSON,
                    new String[]{"Bearer"}, new GenericType<>() {}
            );
            System.out.println(">>> Artifact created");
            System.out.println("You can view or manage this artifact in your browser by accessing:");
            System.out.printf("https://console.redhat.com/application-services/service-registry/t/%s/artifacts/default/%s/versions/1\n",
                    serviceRegistry.getId(), artifactsMap.get("id"));
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return 0;
    }
}

