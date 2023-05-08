package org.hung.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.hung.kafka.pojo.Counter;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaErrorHandlingConfig {

    private final KafkaProperties properties;

//    @Bean
//    public ConsumerFactory<?,Counter> consumerFactory() {
//        Map<String,Object> consumerProperties = properties.buildConsumerProperties();
//        DefaultKafkaConsumerFactory<?,Counter> factory = new DefaultKafkaConsumerFactory(consumerProperties);
//        ErrorHandlingDeserializer<Counter> deserializer = new ErrorHandlingDeserializer<Counter>();
//        deserializer.set
//        factory.setValueDeserializer(deserializer);
//        return factory;
//    }

    //When CommonLoggingErrorHandler was defined, the DeadLetterPublishingRecoverer was skipped
    //@Bean
    public CommonLoggingErrorHandler commonLoggingErrorHandler() {
        return new CommonLoggingErrorHandler();
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterRecoverer) {
        BackOff backoff = new FixedBackOff(1000,2);
//        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(10);
//        backoff.setMaxInterval(100);
//        backoff.setMultiplier(1.3);
        return new DefaultErrorHandler(deadLetterRecoverer, backoff);
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<?,?> voidKafkaTemplate, KafkaTemplate<?, ?> bytesKafkaTemplate, KafkaTemplate<?, ?> defaultKafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer((producerRecord) -> {
            if (producerRecord.value()==null) {
                return voidKafkaTemplate;
            } else if (producerRecord.value().getClass().isAssignableFrom(byte[].class)) {
                return bytesKafkaTemplate;
            } else {
                return defaultKafkaTemplate;
            }
        },false, (producerRecord,exception) -> {
            if (exception.getCause() instanceof DeserializationException e) {
                return new TopicPartition("dead-msg-queue",0);
            } else if (exception.getCause() instanceof MethodArgumentNotValidException e) {
                return new TopicPartition("empty-msg-queue",0);
            } else if (exception.getCause() instanceof NotReadablePropertyException e) {
                return new TopicPartition("invalid-payload-queue",0);
            } else {
                return new TopicPartition(producerRecord.topic() + "-DLQ", producerRecord.partition());
            }
        });
        return recoverer;
    }

    @Bean
    public ProducerFactory<Bytes, Void> voidProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    @Bean
    public KafkaTemplate<Bytes, Void> voidKafkaTemplate(ProducerFactory<Bytes, Void> voidProducerFactory) {
        return new KafkaTemplate<>(voidProducerFactory);
    }

    /**
     * This is the specific Producer for serialization exceptions.
     * We configure ByteArraySerializer for both the key and value serializer.
     * Because we don't know upfront in what 'format' the record from the topic caused the deserialization exception.
     */
    @Bean
    public ProducerFactory<Bytes, Bytes> bytesProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    /**
     * This is the specific Kafka template for serialization exceptions.
     */
    @Bean
    public KafkaTemplate<Bytes, Bytes> bytesKafkaTemplate(ProducerFactory<Bytes, Bytes> bytesProducerFactory) {
        return new KafkaTemplate<>(bytesProducerFactory);
    }

    /**
     * We have to also create the "default" kafkaProducerFactory.
     * The code is basically copied from:
     * {@link org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration#kafkaProducerFactory(ObjectProvider)}
     */
    @Bean
    public ProducerFactory<?, ?> defaultKafkaProducerFactory(
            ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(
                this.properties.buildProducerProperties());
        String transactionIdPrefix = this.properties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    /**
     * We have to also create the "default" kafkaTemplate.
     * The code is basically copied from:
     * {@link org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration#kafkaTemplate(ProducerFactory, ProducerListener, ObjectProvider)}
     */
    @Bean
    public KafkaTemplate<?, ?> defaultKafkaTemplate(ProducerFactory<Object, Object> defaultKafkaProducerFactory,
                                             ProducerListener<Object, Object> kafkaProducerListener,
                                             ObjectProvider<RecordMessageConverter> messageConverter) {
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(defaultKafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        kafkaTemplate.setProducerListener(kafkaProducerListener);
        kafkaTemplate.setDefaultTopic(this.properties.getTemplate().getDefaultTopic());
        return kafkaTemplate;
    }
}
