package org.hung.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class SimpleConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleConsumerApplication.class, args);
    }

}
