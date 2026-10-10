package com.skillproof.backend.media.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@EnabledIfEnvironmentVariable(named = "S3_IT_ENABLED", matches = "true")
class S3CompatibilityIntegrationTest {

    @Test
    void uploadDownloadAndDeleteAgainstConfiguredObjectStorage() {
        String bucket = required("MEDIA_S3_BUCKET");
        String endpoint = required("MEDIA_S3_ENDPOINT");
        String region = required("MEDIA_S3_REGION");
        String accessKey = required("MEDIA_S3_ACCESS_KEY");
        String secretKey = required("MEDIA_S3_SECRET_KEY");
        UUID id = UUID.randomUUID();
        byte[] expected = "s3-integration".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (S3Client client = S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(java.net.URI.create(endpoint))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .overrideConfiguration(c -> c.apiCallTimeout(Duration.ofSeconds(10)))
                .build()) {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            client.putObject(PutObjectRequest.builder().bucket(bucket).key("it/" + id)
                    .contentType("text/plain").build(), RequestBody.fromBytes(expected));
            try (var stream = client.getObject(GetObjectRequest.builder().bucket(bucket).key("it/" + id).build())) {
                assertArrayEquals(expected, stream.readAllBytes());
            } catch (java.io.IOException exception) {
                throw new AssertionError("Could not read S3 object", exception);
            }
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key("it/" + id).build());
            var missing = assertThrows(S3Exception.class, () -> client.headObject(
                    software.amazon.awssdk.services.s3.model.HeadObjectRequest.builder()
                            .bucket(bucket).key("it/" + id).build()));
            org.junit.jupiter.api.Assertions.assertEquals(404, missing.statusCode());
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required for S3 integration");
        }
        return value;
    }
}
