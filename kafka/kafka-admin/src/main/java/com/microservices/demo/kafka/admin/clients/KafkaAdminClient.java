package com.microservices.demo.kafka.admin.clients;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.RetryConfigData;
import com.microservices.demo.kafka.admin.config.KafkaAdminConfig;
import com.microservices.demo.kafka.admin.exception.KafkaClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaAdminClient {

    private final KafkaConfigData kafkaConfigData;

    private final RetryConfigData retryConfigData;

    private final AdminClient adminClient;

    private final RetryTemplate retryTemplate;

    private final WebClient webClient;

    /**
     * Create topics written in kafka config data
     */
    public void createTopics() {
        CreateTopicsResult createTopicsResult;
        try {
            createTopicsResult = retryTemplate.execute(this::doCreateTopics);
        } catch (Throwable t) {
            throw new KafkaClientException("Reached max number of retry attempts for creating Kafka topics", t);
        }
        checkTopicsCreated();
    }

    private CreateTopicsResult doCreateTopics(RetryContext retryContext) {
        List<String> topicNames = kafkaConfigData.getTopicNamesToCreate();
        log.info("Creating {} topics, attempt {}", topicNames.size(), retryContext.getRetryCount());
        List<NewTopic> topics = topicNames.stream().map(topic -> new NewTopic(topic.trim(),
                        kafkaConfigData.getNumOfPartitions(),
                        kafkaConfigData.getReplicationFactor()))
                .collect(Collectors.toList());

        return adminClient.createTopics(topics);
    }

    /**
     * Check that topics are created
     */
    public void checkTopicsCreated() {
        Collection<TopicListing> topics = getTopics();
        int retryCount = 1;
        Integer maxRetryCount = retryConfigData.getMaxAttempts();
        int multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();

        for (String topic : kafkaConfigData.getTopicNamesToCreate()) {
            while (!isTopicCreated(topics, topic)) {
                checkMaxRetry(retryCount++, maxRetryCount);
                sleep(sleepTimeMs);
                sleepTimeMs *= multiplier;
                topics = getTopics();
            }
        }
    }

    /**
     * Check that schema registry is created
     */
    public void checkSchemaRegistry() {
        int retryCount = 1;
        Integer maxRetryCount = retryConfigData.getMaxAttempts();
        int multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        while (getSchemaRegistryStatus() == HttpStatus.SERVICE_UNAVAILABLE) {
            checkMaxRetry(retryCount++, maxRetryCount);
            sleep(sleepTimeMs);
            sleepTimeMs *= multiplier;
        }

    }

    /**
     * use WebClient to get schema registry status
     * @return successful status on success, HttpStatus.SERVICE_UNAVAILABLE on fail/exception
     */
    private HttpStatus getSchemaRegistryStatus() {
        try {
            return webClient
                    .method(HttpMethod.GET)
                    .uri(kafkaConfigData.getSchemaRegistryUrl())
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is2xxSuccessful())
                            return Mono.just(clientResponse.statusCode());
                        else
                            return Mono.just(HttpStatus.SERVICE_UNAVAILABLE);
                    })
                    .block();
        } catch (Exception e) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

    }

    private void sleep(Long sleepTimeMs) {
        if (sleepTimeMs <= 0) return;
        try {
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException e) {
            throw new KafkaClientException("Interrupted while sleep-waiting for new created topics");
        }
    }

    private void checkMaxRetry(int retry, Integer maxRetryCount) {
        if (retry > maxRetryCount)
            throw new KafkaClientException("Reached max number of retries for reading Kafka topics");
    }

    private boolean isTopicCreated(Collection<TopicListing> topics, String topic) {
        if (topics == null) return false;
        return topics.stream().anyMatch(topicListing -> topicListing.name().equals(topic));
    }

    private Collection<TopicListing> getTopics() {
        Collection<TopicListing> topics;
        try {
            topics = retryTemplate.execute(this::doGetTopics);
        } catch (Throwable t) {
            throw new KafkaClientException("Reached max number of retry attempts for getting Kafka topics", t);
        }
        return topics;
    }

    private Collection<TopicListing> doGetTopics(RetryContext retryContext) throws ExecutionException, InterruptedException {
        log.info("Reading kafka topic {}, attempt {}",
                kafkaConfigData.getTopicNamesToCreate().toArray(), retryContext.getRetryCount());

        Collection<TopicListing> topics = adminClient.listTopics().listings().get();
        if (topics != null) {
            topics.forEach( topic -> log.info("Topic with name {}", topic.name()));
        }
        return topics;
    }
}
