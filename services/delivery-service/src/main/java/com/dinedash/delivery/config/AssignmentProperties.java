package com.dinedash.delivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "dinedash.assignment")
public record AssignmentProperties(double searchRadiusKm, Duration driverStaleAfter, Duration retryInterval) {
}
