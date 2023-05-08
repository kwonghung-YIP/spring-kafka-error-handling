package org.hung.kafka;

import org.hung.kafka.pojo.Counter;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Configuration
public class MyKafkaListenerConfig implements KafkaListenerConfigurer {

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(new CounterValidator());
    }

    static public class CounterValidator implements Validator {

        @Override
        public boolean supports(Class<?> clazz) {
            return Counter.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            Counter counter = (Counter)target;
            if (counter.getValue()<0) {
                errors.rejectValue("nagetive-value","counter cannot be negative value!!!");
            }
        }
    }
}
