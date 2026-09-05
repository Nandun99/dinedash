package com.dinedash.restaurant.storage;

import com.dinedash.common.error.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageUploadServiceTest {

    private final S3Presigner presigner = S3Presigner.builder()
            .region(Region.AP_SOUTH_1)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
            .build();
    private final ImageUploadService service = new ImageUploadService(presigner,
            new StorageProperties("ap-south-1", "dinedash-media", null, null));

    @AfterEach
    void close() {
        presigner.close();
    }

    @Test
    void createsScopedKeyAndSignedUrl() {
        var ticket = service.createUploadUrl("r1", ImageUploadService.ImageTarget.LOGO, "image/png");

        assertThat(ticket.key()).startsWith("restaurants/r1/logo/").endsWith(".png");
        assertThat(ticket.uploadUrl()).contains("X-Amz-Signature");
        assertThat(ticket.publicUrl()).isEqualTo("https://dinedash-media.s3.ap-south-1.amazonaws.com/" + ticket.key());
    }

    @Test
    void rejectsNonImageContentType() {
        assertThatThrownBy(() -> service.createUploadUrl("r1", ImageUploadService.ImageTarget.COVER, "application/pdf"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsKeysFromOtherRestaurants() {
        assertThatThrownBy(() -> service.publicUrlFor("r1", "restaurants/r2/logo/x.png"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.publicUrlFor("r1", "restaurants/r1/../r2/logo/x.png"))
                .isInstanceOf(BadRequestException.class);
    }
}
