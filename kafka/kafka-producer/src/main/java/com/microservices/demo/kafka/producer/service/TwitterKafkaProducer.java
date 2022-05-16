package com.microservices.demo.kafka.producer.service;

import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.service.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PreDestroy;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {


    private final KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate;


    @Override
    public void send(String topic, Long key, TwitterAvroModel message) {
        log.info("Sending message '{}' to topic '{}'", message, topic);
        ListenableFuture<SendResult<Long, TwitterAvroModel>> future = kafkaTemplate.send(topic, key, message);
        addCallback(topic, message, future);
    }

    private void addCallback(String topic, TwitterAvroModel message, ListenableFuture<SendResult<Long, TwitterAvroModel>> future) {
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable t) {
                log.error("Error while sending message {} to topic {}", message.toString(), topic, t);
            }

            @Override
            public void onSuccess(SendResult<Long, TwitterAvroModel> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.debug("Received new metadata. Topic {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());
            }
        });
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing kafka producer");
            kafkaTemplate.destroy();
        }
    }
}
