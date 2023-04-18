package org.hung.kafka;

import lombok.extern.slf4j.Slf4j;
import org.hung.kafka.pojo.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterRecoverer) {
        return new DefaultErrorHandler(deadLetterRecoverer, new FixedBackOff(1000,5));
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterRecoverer(KafkaTemplate<?, ?> bytesKafkaTemplate, KafkaTemplate<?, ?> kafkaTemplate) {
        Map<Class<?>, KafkaOperations<? extends Object, ? extends Object>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesKafkaTemplate);
        templates.put(Counter.class, kafkaTemplate);
        return new DeadLetterPublishingRecoverer(templates);
    }
}
