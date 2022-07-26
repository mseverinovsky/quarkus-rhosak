package com.redhat.rhosak.service.registry.artifact;

import com.openshift.cloud.api.kas.auth.invoker.ApiClient;
import com.openshift.cloud.api.kas.auth.invoker.ApiException;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.KafkaInstanceClient;
import picocli.CommandLine;

import javax.ws.rs.core.GenericType;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.redhat.rhosak.KafkaManagementClient.API_CLIENT_BASE_PATH;

@CommandLine.Command(name = "download", mixinStandardHelpOptions = true, description = "Download artifacts from Service Registry")
public class ServiceRegistryArtifactDownloadCommand extends CustomCommand implements Callable<Integer> {

    private final ApiClient apiInstanceClient;

    @CommandLine.Option(names = "--content-id", paramLabel = "int", description = "ContentId of the artifact")
    String contentId;

    @CommandLine.Option(names = "--global-id", paramLabel = "int", description = "Global ID of the artifact")
    String globalId;

    @CommandLine.Option(names = {"--output-file", "-o"}, paramLabel = "string", description = "File location of the artifact")
    String outputFile;

    public ServiceRegistryArtifactDownloadCommand() {
        this.apiInstanceClient = KafkaInstanceClient.getKafkaInstanceAPIClient();
    }

    @Override
    public Integer call() {
        final String ARTIFACT_DOWNLOAD_URL = "/apis/registry/v2/ids";

        apiInstanceClient.setBasePath(API_CLIENT_BASE_PATH);
        try {
            // Get Service Registry list
            Map<String, Object> res = apiInstanceClient.invokeAPI(
                    SERVICE_REGISTRY_MGMT_URL, "GET", null, null,
                    new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_JSON, APPLICATION_X_WWW_FORM_URLENCODED,
                    new String[]{"Bearer"}, new GenericType<>() {}
            );

            if ((res.get("items")) == null || ((ArrayList)res.get("items")).size() == 0) {
                System.err.println(">>> No Service Registries found!");
                return -1;
            } else {
                // Get first list element
                LinkedHashMap<String, Object> map = ((ArrayList<LinkedHashMap<String, Object>>)res.get("items")).get(0);
                // Get registry Url
                String registryUrl = (String) map.get("registryUrl");
                apiInstanceClient.setBasePath(registryUrl);

                String url;
                if (globalId != null) {
                    url = ARTIFACT_DOWNLOAD_URL + "/globalIds/" + globalId + "/";
                } else if (contentId != null) {
                    url = ARTIFACT_DOWNLOAD_URL + "/contentIds/" + contentId + "/";
                } else {
                    throw new IllegalArgumentException("Please specify at least one flag: [--content-id or --global-id]!");
                }

                String response = apiInstanceClient.invokeAPI(
                        url, "GET", null, null,
                        new HashMap<>(), new HashMap<>(), new HashMap<>(), ACCEPT_APPLICATION_OCTET_STREAM, CONTENT_TYPE_APPLICATION_JSON,
                        new String[]{"Bearer"}, new GenericType<>() {}
                );
                if (response == null) {
                    System.err.println(">>> No artifacts found!");
                    return -1;
                } else {
                    if (outputFile == null) {
                        System.out.println(response);
                    } else {
                        String fileName = outputFile;
                        File file = new File(fileName);
                        int n = 0;
                        int count = 0;
                        while (file.exists()) {
                            if (++count < 10) {
                                String fName = fileName + "("+ ++n +")";
                                file = new File(fName);
                            } else {
                                System.err.printf(">>> Too much attempts. Do you really need all these %d files?!\n", count);
                                System.err.println(">>> File name: " + outputFile);
                                return -1;
                            }
                        }
                        // write to file
                        Path artifactFile = file.toPath();
                        Files.writeString(artifactFile, response, StandardCharsets.UTF_8);
                        System.out.println(">>> Artifact has been stored in file:\n\t" + file);
                    }
                }
            }
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                System.err.println(">>> No artifacts found!");
                return -1;
            } else {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}

