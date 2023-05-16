package org.hung.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
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

    @Value("counter")
    private String topic;

    @Bean
    public NewTopic counterTopic() {
        return TopicBuilder.name(topic)
                .partitions(10)
                .build();
    }

    @Scheduled(fixedRateString = "10000")
    public void publishCounter() {
        counter.publish();
    }
}
