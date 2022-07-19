package com.redhat.rhosak.service.registry.artifact;

import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.KafkaInstanceClient;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "download", mixinStandardHelpOptions = true, description = "Download artifacts from Service Registry")
public class ServiceRegistryArtifactDownloadCommand extends CustomCommand implements Callable<Integer> {

    private final ApiClient apiInstanceClient;

    @CommandLine.Option(names = "--file", paramLabel = "string", description = "File location of the artifact")
    String fileName;

    @CommandLine.Option(names = "--type", paramLabel = "string", description = "Type of artifact")
    String artifactType;

    public ServiceRegistryArtifactDownloadCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
    }

    @Override
    public Integer call() {

//        String body;
//        try {
//            if (fileName == null) {
//                // read the artifact from System.in
//                body = new String(System.in.readAllBytes());
//            } else {
//                // read the artifact from file
//                body = new String(Files.readAllBytes(Paths.get(fileName)));
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        final String artifactTypeRequestHeader = "X-Registry-Artifacttype";
//        Map<String, String> headerParams = new HashMap<>();
//        if (artifactType != null) {
//            // Add artifact type Header
//            headerParams.put(artifactTypeRequestHeader, artifactType);
//        }
//        ServiceRegistryDTO serviceRegistry = ServiceRegistryUtils.getServiceRegistry();
//        String registryUrl = serviceRegistry.getRegistryUrl();
//        apiInstanceClient.setBasePath(registryUrl);
//        final String ARTIFACT_CREATE_URL = "/apis/registry/v2/groups/default/artifacts";
//
//        try {
//            Map<String, Object> artifactsMap = apiInstanceClient.invokeAPI(
//                    ARTIFACT_CREATE_URL, "POST", null, body,
//                    headerParams, new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, CONTENT_TYPE_APPLICATION_JSON,
//                    new String[]{"Bearer"}, new GenericType<>() {}
//            );
//            System.out.println(">>> Artifact created");
//            System.out.println("You can view or manage this artifact in your browser by accessing:");
//            System.out.printf("https://console.redhat.com/application-services/service-registry/t/%s/artifacts/default/%s/versions/1\n",
//                    serviceRegistry.getId(), artifactsMap.get("id"));
//        } catch (ApiException e) {
//            if (e.getCode() == 400) {
//                String msgUnableToExtractParameter =
//                        "Unable to extract parameter from http request: javax.ws.rs.HeaderParam(\\\"X-Registry-ArtifactType\\\")";
//                String msgFailedToDiscoverArtifactType = "Failed to discover artifact type from content";
//                if (e.getMessage().contains(msgUnableToExtractParameter)) {
//                    System.err.println(">>> " + msgUnableToExtractParameter + " value is '" + artifactType + "'");
//                    System.err.println(">>> Please specify correct artifact type (XML, AVRO, JSON, etc.)");
//                } else if (e.getMessage().contains(msgFailedToDiscoverArtifactType)) {
//                    System.err.println(">>> " + msgFailedToDiscoverArtifactType);
//                    System.err.println(">>> Please specify artifact type explicitly (--type XML, AVRO, JSON, etc.)");
//                } else {
//                    System.err.println(e.getLocalizedMessage());
//                }
//            }
//            return -1;
//        }

        return 0;
    }
}

