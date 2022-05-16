package com.microservices.demo.kafkatoelasticservice.transformer;

import com.microservices.demo.elastic.model.index.TwitterIndexModel;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AvroToElasticModelTransformer {

    /**
     * Transform avro model tweets into elasticsearch model tweets
     * @param twitterAvroModels avro-serialized tweets
     * @return list of TwitterIndexModel
     */
    public List<TwitterIndexModel> getElasticModels(List<TwitterAvroModel> twitterAvroModels) {
        return twitterAvroModels.stream()
                .map(avroModel -> TwitterIndexModel.builder()
                        .userId(avroModel.getUserId())
                        .id(String.valueOf(avroModel.getId()))
                        .text(avroModel.getText())
                        .createdAt(ZonedDateTime.ofInstant(Instant.ofEpochMilli(avroModel.getCreatedAt()), ZoneId.systemDefault()))
                        .build())
                .collect(Collectors.toList());
    }
}
