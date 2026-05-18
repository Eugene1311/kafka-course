package org.example;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        try (Admin admin = Admin.create(props)) {
            String topicName = "my-topic";
            int partitions = 12;
            short replicationFactor = 1;
            // Create a compacted topic
            NewTopic topic = new NewTopic(topicName, partitions, replicationFactor)
                    .configs(Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT));


            admin.createTopics(Collections.singleton(topic))
                    .all()
                    .thenApply((v) -> admin.describeTopics(List.of(topicName)))
                    .thenApply(DescribeTopicsResult::topicNameValues)
                    .toCompletionStage()
                    .toCompletableFuture()
                    .thenCompose(result -> result.get(topicName).toCompletionStage())
                    .whenComplete((description, ex) -> {
                        if (description != null) {
                            System.out.println(description);
                        } else {
                            System.out.println(ex.getMessage());
                        }
                    })
                    .get();
        }
    }
}
