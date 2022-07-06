package com.redhat.rhosak.conf;

import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.redhat.rhosak.CustomCommand;
import com.redhat.rhosak.Rhosak;
import com.redhat.rhosak.exception.NoKafkaInstanceFoundException;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "config", mixinStandardHelpOptions = true,
        description = "Generate and print Kafka configuration")
public class KafkaConfigCommand extends CustomCommand implements Callable<Integer> {

    public KafkaConfigCommand() {}

    @Override
    public Integer call() {

        try {
            System.out.println("========== Configuration for application.properties ==========================");
            // get kafka
            String bootstrapServerUrl = getBootstrapServerUrl();
            // load SA from file
            ServiceAccount sa = Rhosak.loadServiceAccountFromFile();
            String client_id = sa.getClientId();
            String client_secret = sa.getClientSecret();
            String configFormat =
                "kafka.bootstrap.servers=%s\n" +
                "kafka.security.protocol=SASL_SSL\n" +
                "kafka.sasl.mechanism=PLAIN\n" +
                "kafka.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \\\n" +
                "  username=\"%s\" \\\n" +
                "  password=\"%s\";\n";

            System.out.printf(configFormat, bootstrapServerUrl, client_id, client_secret);

        } catch (IOException | NoKafkaInstanceFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
