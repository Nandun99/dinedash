package com.dinedash.restaurant.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Credentials come from the default provider chain: the ECS task role in AWS,
 * or AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY locally.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class S3Config {

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(StorageProperties properties) {
        return S3Presigner.builder().region(Region.of(properties.region())).build();
    }
}
