package com.dinedash.restaurant.storage;

import com.dinedash.common.error.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.util.Map;
import java.util.UUID;

/**
 * Hands the browser a short-lived pre-signed URL so images go straight to S3
 * instead of streaming through the service.
 */
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final S3Presigner presigner;
    private final StorageProperties properties;

    public UploadTicket createUploadUrl(String restaurantId, ImageTarget target, String contentType) {
        String extension = ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new BadRequestException("Only JPEG, PNG or WebP images are allowed");
        }
        String key = "restaurants/%s/%s/%s.%s".formatted(restaurantId, target.folder(), UUID.randomUUID(), extension);
        PresignedPutObjectRequest presigned = presigner.presignPutObject(r -> r
                .signatureDuration(properties.uploadUrlTtl())
                .putObjectRequest(PutObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(key)
                        .contentType(contentType)
                        .build()));
        return new UploadTicket(presigned.url().toString(), key, properties.publicUrl(key));
    }

    /** Guards against clients pointing an image field at an arbitrary key. */
    public String publicUrlFor(String restaurantId, String key) {
        if (key == null || !key.startsWith("restaurants/" + restaurantId + "/") || key.contains("..")) {
            throw new BadRequestException("Image does not belong to this restaurant");
        }
        return properties.publicUrl(key);
    }

    public enum ImageTarget {
        LOGO("logo"), COVER("cover"), MENU_ITEM("menu");

        private final String folder;

        ImageTarget(String folder) {
            this.folder = folder;
        }

        String folder() {
            return folder;
        }
    }

    public record UploadTicket(String uploadUrl, String key, String publicUrl) {
    }
}
