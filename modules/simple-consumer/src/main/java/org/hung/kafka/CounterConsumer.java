package org.hung.kafka;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hung.kafka.pojo.Counter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CounterConsumer {

    @KafkaListener(topics = {"counter"})
    public void receive(
            @Payload @Valid Counter counter,
            @Header(KafkaHeaders.GROUP_ID) String groupId,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition
    ) {
        if (counter.getValue()%5!=0) {
            log.info("received counter {} ({},{})",counter.getValue(),groupId,partition);
        } else {
            throw new RuntimeException("Consumer exception!! [" + counter.getValue() + "] (group,partition) : (" + groupId + ":" + partition + ")");
        }
    }
}
