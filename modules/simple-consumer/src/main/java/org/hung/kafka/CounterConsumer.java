package org.hung.kafka;

import lombok.extern.slf4j.Slf4j;
import org.hung.kafka.pojo.Counter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CounterConsumer {

    @KafkaListener(topics = {"counter"})
    public void receive(Counter counter) {
        log.info("received counter {}",counter.getValue());
    }
}
