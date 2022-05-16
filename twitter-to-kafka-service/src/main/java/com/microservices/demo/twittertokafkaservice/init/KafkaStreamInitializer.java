package com.microservices.demo.twittertokafkaservice.init;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.admin.clients.KafkaAdminClient;
import com.microservices.demo.twittertokafkaservice.init.StreamInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaStreamInitializer implements StreamInitializer {

    private final KafkaConfigData kafkaConfigData;

    private final KafkaAdminClient kafkaAdminClient;

    @Override
    public void init() {
        kafkaAdminClient.createTopics();
        kafkaAdminClient.checkSchemaRegistry();
        log.info("Topics with name {} are ready for operations", kafkaConfigData.getTopicNamesToCreate().toArray());
    }
}
