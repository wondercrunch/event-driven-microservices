package com.microservices.demo.twittertokafkaservice.runner;

import com.microservices.demo.config.TwitterToKafkaServiceConfigData;
import com.microservices.demo.twittertokafkaservice.listener.TwitterToKafkaStatusListener;
import com.microservices.demo.twittertokafkaservice.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import javax.annotation.PreDestroy;
import java.util.Arrays;

@Component
@ConditionalOnProperty(name="twitter-to-kafka-service.enable-mock-tweets", havingValue = "false")
@RequiredArgsConstructor
@Slf4j
public class TwitterToKafkaStreamRunner implements StreamRunner {


    private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;

    private final TwitterToKafkaStatusListener twitterToKafkaStatusListener;

    private TwitterStream twitterStream;

    @Override
    public void start() throws TwitterException {
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(twitterToKafkaStatusListener);
        addFilter();
    }

    @PreDestroy
    private void shutdown() {
        if (twitterStream != null) {
            log.info("Closing twitter stream");
            twitterStream.shutdown();
        }
    }

    private void addFilter() {
        String[] keywords = twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery = new FilterQuery(keywords);
        twitterStream.filter(filterQuery);
        log.info("Started filtering twitter stream for keywords: {}", Arrays.toString(keywords));
    }
}
