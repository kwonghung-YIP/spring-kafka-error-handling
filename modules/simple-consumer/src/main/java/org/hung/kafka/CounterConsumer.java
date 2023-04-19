package org.hung.kafka;

import lombok.extern.slf4j.Slf4j;
import org.hung.kafka.pojo.Counter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CounterConsumer {

    @KafkaListener(topics = {"counter"})
    public void receive(Counter counter) {
        if (counter.getValue()%20!=0) {
            log.info("received counter {}",counter.getValue());
        } else {
            throw new RuntimeException("Consumer exception!!");
        }
    }
}
