package com.microservices.demo.kafkatoelasticservice.consumer;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.KafkaConsumerConfigData;
import com.microservices.demo.elastic.indexclient.service.ElasticIndexClient;
import com.microservices.demo.elastic.model.index.TwitterIndexModel;
import com.microservices.demo.kafka.admin.clients.KafkaAdminClient;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafkatoelasticservice.consumer.KafkaConsumer;
import com.microservices.demo.kafkatoelasticservice.transformer.AvroToElasticModelTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwitterKafkaConsumer implements KafkaConsumer<Long, TwitterAvroModel> {

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private final KafkaAdminClient kafkaAdminClient;

    private final KafkaConfigData kafkaConfigData;

    private final KafkaConsumerConfigData kafkaConsumerConfigData;

    private final AvroToElasticModelTransformer avroToElasticModelTransformer;

    private final ElasticIndexClient<TwitterIndexModel> elasticIndexClient;

    //auto start should be false for this method to work
    @EventListener
    public void onAppStarted(ApplicationStartedEvent event) {
        kafkaAdminClient.checkTopicsCreated(); //ensure topics are created
        log.info("Topics with name {} ready!", kafkaConfigData.getTopicNamesToCreate().toArray());
        Objects.requireNonNull(kafkaListenerEndpointRegistry
                .getListenerContainer(kafkaConsumerConfigData.getConsumerGroupId())).start(); //then actually start consumer manually
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.consumer-group-id}", topics = "${kafka-config.topic-name}") //creates Kafka Consumer
    public void receive(@Payload List<TwitterAvroModel> messages,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<Integer> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of message received with keys {}, partitions {} and offsets {}, " +
                        "sending it to elastic: Thread id {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString(),
                Thread.currentThread().getId());
        List<TwitterIndexModel> elasticModels = avroToElasticModelTransformer.getElasticModels(messages);
        List<String> ids = elasticIndexClient.save(elasticModels);
        log.info("Documents saved to elasticsearch with ids {}", ids);
    }
}
