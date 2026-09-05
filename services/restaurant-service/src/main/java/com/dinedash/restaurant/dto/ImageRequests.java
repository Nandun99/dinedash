package com.dinedash.restaurant.dto;

import com.dinedash.restaurant.storage.ImageUploadService.ImageTarget;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ImageRequests {

    private ImageRequests() {
    }

    public record UploadUrlRequest(@NotNull ImageTarget target, @NotBlank String contentType) {
    }

    /** Sent after the browser finished uploading to S3. {@code menuItemId} is required for MENU_ITEM. */
    public record AttachImageRequest(@NotNull ImageTarget target, @NotBlank String key, String menuItemId) {
    }
}
