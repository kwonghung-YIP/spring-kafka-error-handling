package org.hung.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.hung.kafka.pojo.Counter;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class CounterProducer {

    @Value("counter")
    private String topic;

    private long counter;
    private final KafkaTemplate template;

    public void publish() {
        counter++;
        log.info("Publish counter {} to topic {}",counter,topic);
        template.send(topic, UUID.randomUUID(), new Counter(counter));
    }
}
