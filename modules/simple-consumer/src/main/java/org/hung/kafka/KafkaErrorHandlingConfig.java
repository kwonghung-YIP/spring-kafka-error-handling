package org.hung.kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.hung.kafka.pojo.Counter;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterRecoverer) {
        //BackOff backoff = new FixedBackOff(1000,5);
        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(10);
        backoff.setMaxInterval(100);
        backoff.setMultiplier(1.3);
        return new DefaultErrorHandler(deadLetterRecoverer, backoff);
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterRecoverer(KafkaTemplate<?, ?> bytesKafkaTemplate, KafkaTemplate<?, ?> counterProducerFactory) {
        Map<Class<?>, KafkaOperations<? extends Object, ? extends Object>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesKafkaTemplate);
        templates.put(Counter.class, counterProducerFactory);
        return new DeadLetterPublishingRecoverer(templates);
    }

    @Bean
    public ProducerFactory<UUID,Counter> counterProducerFactory(KafkaProperties kafkaProperties) {
        Map<String,Object> props = kafkaProperties.buildProducerProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        return new DefaultKafkaProducerFactory<UUID,Counter>(props);
    }

    @Bean
    public KafkaTemplate<UUID,Counter> counterKafkaTemplate(ProducerFactory<UUID,Counter> counterProducerFactory) {
        return new KafkaTemplate<>(counterProducerFactory);
    }
}
