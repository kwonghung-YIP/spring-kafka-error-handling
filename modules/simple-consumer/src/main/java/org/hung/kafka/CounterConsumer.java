package org.hung.kafka;

import lombok.extern.slf4j.Slf4j;
import org.hung.kafka.pojo.Counter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CounterConsumer {

    @KafkaListener(topics = {"counter"})
    public void receive(Counter counter, @Header(KafkaHeaders.RECEIVED_PARTITION) String partition) {
        if (counter.getValue()%10!=0) {
            log.info("received counter {}",counter.getValue());
        } else {
            throw new RuntimeException("Consumer exception!! [" + counter.getValue() + "] partition Id:" + partition);
        }
    }
}
