package com.microservices.demo.kafka.producer.service;

import org.apache.avro.specific.SpecificRecordBase;

import java.io.Serializable;

public interface KafkaProducer<K extends Serializable, V extends SpecificRecordBase> {
    /**
     * key - messages with same key will be inserted in same partition
     */
    void send(String topic, K key, V message);
}
