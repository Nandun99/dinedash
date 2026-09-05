package com.dinedash.restaurant.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "dinedash.storage")
public record StorageProperties(String region, String bucket, String publicBaseUrl, Duration uploadUrlTtl) {

    public StorageProperties {
        uploadUrlTtl = uploadUrlTtl == null ? Duration.ofMinutes(10) : uploadUrlTtl;
    }

    /** Public URL of an object, through CloudFront if configured, otherwise the bucket endpoint. */
    public String publicUrl(String key) {
        String base = publicBaseUrl != null && !publicBaseUrl.isBlank()
                ? publicBaseUrl
                : "https://" + bucket + ".s3." + region + ".amazonaws.com";
        return base.endsWith("/") ? base + key : base + "/" + key;
    }
}
