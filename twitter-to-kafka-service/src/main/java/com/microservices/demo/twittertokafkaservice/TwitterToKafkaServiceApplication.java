package com.microservices.demo.twittertokafkaservice;


import com.microservices.demo.twittertokafkaservice.init.StreamInitializer;
import com.microservices.demo.twittertokafkaservice.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
@RequiredArgsConstructor
@Slf4j
public class TwitterToKafkaServiceApplication implements CommandLineRunner {

    private final StreamRunner streamRunner;

    private final StreamInitializer streamInitializer;

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("App startup");
        streamInitializer.init();
        streamRunner.start();
    }
}
