package com.dinedash.restaurant.config;

import com.dinedash.common.kafka.KafkaErrorHandling;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;

@Configuration
public class KafkaConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
        return KafkaErrorHandling.retryThenDeadLetter(template);
    }
}
