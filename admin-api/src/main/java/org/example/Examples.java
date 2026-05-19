package org.example;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewPartitionReassignment;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Examples {
    public static final String MY_TOPIC = "my-topic";
    public static final int MY_TOPIC_PARTITIONS = 12;
    public static final short MY_TOPIC_REPLICAS = 1;
    public static final String TEST_TOPIC = "test-2";
    public static final int TEST_TOPIC_PARTITIONS = 1;
    public static final short TEST_TOPIC_REPLICAS = 1;

    public static void createAnsDescribeTopic(Admin admin) {
        // Create a compacted topic
        NewTopic topic = new NewTopic(MY_TOPIC, MY_TOPIC_PARTITIONS, MY_TOPIC_REPLICAS)
                .configs(Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT));


        admin.createTopics(Collections.singleton(topic))
                .all()
                .thenApply((v) -> admin.describeTopics(List.of(MY_TOPIC)))
                .thenApply(DescribeTopicsResult::topicNameValues)
                .toCompletionStage()
                .toCompletableFuture()
                .thenCompose(result -> result.get(MY_TOPIC).toCompletionStage())
                .whenComplete((description, ex) -> {
                    if (description != null) {
                        System.out.println(description);
                    } else {
                        System.out.println(ex.getMessage());
                    }
                })
                .join();
    }

    public static void createTopicAndAlterReplicas(Admin admin) {
        NewTopic topic = new NewTopic(TEST_TOPIC, TEST_TOPIC_PARTITIONS, TEST_TOPIC_REPLICAS);

        TopicDescription topicDescription = admin.createTopics(List.of(topic))
                .all()
                .thenApply(v -> admin.alterPartitionReassignments(Map.of(
                        new TopicPartition(TEST_TOPIC, 0), Optional.of(new NewPartitionReassignment(List.of(1, 2, 3)))
                )))
                .thenApply((v) -> admin.describeTopics(List.of(TEST_TOPIC)))
                .thenApply(DescribeTopicsResult::topicNameValues)
                .toCompletionStage()
                .toCompletableFuture()
                .thenCompose(result -> result.get(TEST_TOPIC).toCompletionStage())
                .join();
        System.out.println(topicDescription);
    }

    public static void describeTopic(Admin admin, String topic) {
        TopicDescription topicDescription = admin.describeTopics(List.of(topic))
                .topicNameValues()
                .get(topic)
                .toCompletionStage()
                .toCompletableFuture()
                .join();
        System.out.println(topicDescription);
    }
}
