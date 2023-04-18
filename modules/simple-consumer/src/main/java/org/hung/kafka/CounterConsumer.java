package org.hung.kafka;

import lombok.extern.slf4j.Slf4j;
import org.hung.kafka.pojo.Counter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Random;

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
