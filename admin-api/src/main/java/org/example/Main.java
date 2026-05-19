package org.example;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.Properties;

import static org.example.Examples.TEST_TOPIC;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        try (Admin admin = Admin.create(props)) {
//            Examples.createAnsDescribeTopic(admin);
//            Examples.createTopicAndAlterReplicas(admin);
            Examples.describeTopic(admin, TEST_TOPIC);
        }
    }
}
