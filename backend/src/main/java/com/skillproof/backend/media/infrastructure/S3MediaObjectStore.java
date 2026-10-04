package com.skillproof.backend.media.infrastructure;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.skillproof.backend.media.MediaObjectStore;
import com.skillproof.backend.media.StorageException;

import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@Profile("!test")
public class S3MediaObjectStore implements MediaObjectStore {

    private final S3Client client;
    private final String bucket;
    private final String prefix;
    private final boolean ownsClient;

    @Autowired
    public S3MediaObjectStore(@Value("${skillproof.media.bucket}") String bucket,
            @Value("${skillproof.media.region}") String region,
            @Value("${skillproof.media.endpoint:}") String endpoint,
            @Value("${skillproof.media.access-key:}") String accessKey,
            @Value("${skillproof.media.secret-key:}") String secretKey,
            @Value("${skillproof.media.prefix:skillproof/}") String prefix) {
        this(bucket, region, endpoint, accessKey, secretKey, prefix, buildClient(region, endpoint, accessKey, secretKey), true);
    }

    S3MediaObjectStore(String bucket, String region, String endpoint, String accessKey,
            String secretKey, String prefix, S3Client client, boolean ownsClient) {
        if (bucket.isBlank() || region.isBlank() || prefix.contains("..") || prefix.startsWith("/")) {
            throw new IllegalArgumentException("Configure a private media bucket, region and safe prefix");
        }
        this.bucket = bucket;
        this.prefix = prefix.endsWith("/") ? prefix : prefix + "/";
        this.client = client;
        this.ownsClient = ownsClient;
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (software.amazon.awssdk.services.s3.model.S3Exception exception) {
            if (exception.statusCode() == 403) {
                throw new StorageException("STORAGE_ACCESS_DENIED",
                        "Object storage denied access to the configured bucket", exception);
            }
            if (exception.statusCode() == 404) {
                throw new StorageException("STORAGE_BUCKET_UNAVAILABLE",
                        "Configured object storage bucket is unavailable", exception);
            }
            throw new StorageException("STORAGE_CONNECTION_FAILED",
                    "Object storage health check failed", exception);
        } catch (SdkClientException exception) {
            throw new StorageException("STORAGE_CONNECTION_FAILED",
                    "Could not connect to configured object storage", exception);
        } catch (RuntimeException exception) {
            throw new StorageException("STORAGE_CONNECTION_FAILED",
                    "Object storage health check failed", exception);
        }
    }

    private static S3Client buildClient(String region, String endpoint, String accessKey, String secretKey) {
        if (region.isBlank()) {
            throw new IllegalArgumentException("MEDIA_S3_REGION is required");
        }
        var builder = S3Client.builder().region(Region.of(region))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofSeconds(10))
                        .apiCallAttemptTimeout(Duration.ofSeconds(5))
                        .retryPolicy(RetryPolicy.builder().numRetries(2).build())
                        .build());
        if (!endpoint.isBlank()) {
            if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                throw new IllegalArgumentException("MEDIA_S3_ENDPOINT must be an HTTP(S) URL");
            }
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }
        if (!accessKey.isBlank() || !secretKey.isBlank()) {
            if (accessKey.isBlank() || secretKey.isBlank()) {
                throw new IllegalArgumentException("Both object storage credentials are required");
            }
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        }
        return builder.build();
    }

    private String objectKey(UUID id) {
        return prefix + id;
    }

    @Override
    public void put(UUID id, InputStream input, long length, String mime) {
        client.putObject(PutObjectRequest.builder().bucket(bucket).key(objectKey(id)).contentType(mime).build(), RequestBody.fromInputStream(input, length));
    }

    @Override
    public InputStream open(UUID id) {
        return client.getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey(id)).build());
    }

    @Override
    public void delete(UUID id) {
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey(id)).build());
    }

    @PreDestroy
    public void close() {
        if (ownsClient) {
            client.close();
        }
    }
}
