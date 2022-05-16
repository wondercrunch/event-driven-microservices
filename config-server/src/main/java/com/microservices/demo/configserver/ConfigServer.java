package com.microservices.demo.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

//Adding spring-cloud-config-server dependency and using EnableConfigServer annotation will enable a config server

@EnableConfigServer //marks this class as a config server by spring-boot
@SpringBootApplication
public class ConfigServer {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}
