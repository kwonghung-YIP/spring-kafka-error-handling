package org.hung.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class SimpleProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleProducerApplication.class, args);
    }

    @Autowired
    private CounterProducer counter;

    @Scheduled(fixedRateString = "5000")
    public void publishCounter() {
        counter.publish();
    }
}
