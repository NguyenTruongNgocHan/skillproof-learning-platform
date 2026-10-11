package com.skillproof.backend.media.infrastructure.storage;

import com.skillproof.backend.media.application.port.DirectObjectUpload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Component
@Profile("!test")
public class S3DirectObjectUpload
        implements com.skillproof.backend.media.application.port.MultipartObjectUpload,
                DirectObjectUpload,
                com.skillproof.backend.media.application.port.MediaPlaybackLinks,
                AutoCloseable {
    private final S3Client client;
    private final S3Presigner signer;
    private final String bucket, prefix;

    public S3DirectObjectUpload(
            @Value("${skillproof.media.bucket}") String bucket,
            @Value("${skillproof.media.region}") String region,
            @Value("${skillproof.media.endpoint:}") String endpoint,
            @Value("${skillproof.media.public-endpoint:}") String browserEndpoint,
            @Value("${skillproof.media.access-key:}") String key,
            @Value("${skillproof.media.secret-key:}") String secret,
            @Value("${skillproof.media.prefix:skillproof/}") String prefix) {
        this.bucket = bucket;
        this.prefix = prefix.endsWith("/") ? prefix : prefix + "/";
        var c = S3Client.builder().region(Region.of(region));
        var s = S3Presigner.builder().region(Region.of(region));
        if (!endpoint.isBlank()) c.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        String publicUrl = browserEndpoint.isBlank() ? endpoint : browserEndpoint;
        if (!publicUrl.isBlank())
            s.endpointOverride(URI.create(publicUrl))
                    .serviceConfiguration(
                            software.amazon.awssdk.services.s3.S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build());
        if (!key.isBlank() && !secret.isBlank()) {
            var credentials =
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(key, secret));
            c.credentialsProvider(credentials);
            s.credentialsProvider(credentials);
        }
        client = c.build();
        signer = s.build();
    }

    public String signedPut(UUID key) {
        return signer.presignPutObject(
                        PutObjectPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(10))
                                .putObjectRequest(
                                        PutObjectRequest.builder()
                                                .bucket(bucket)
                                                .key(prefix + key)
                                                .build())
                                .build())
                .url()
                .toString();
    }

    public long size(UUID key) {
        return client.headObject(
                        HeadObjectRequest.builder().bucket(bucket).key(prefix + key).build())
                .contentLength();
    }

    @jakarta.annotation.PreDestroy
    public void close() {
        client.close();
        signer.close();
    }

    @Override
    public String signedGet(UUID key) {
        return signer.presignGetObject(
                        software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
                                .builder()
                                .signatureDuration(Duration.ofMinutes(5))
                                .getObjectRequest(
                                        GetObjectRequest.builder()
                                                .bucket(bucket)
                                                .key(prefix + key)
                                                .responseContentDisposition("inline")
                                                .build())
                                .build())
                .url()
                .toString();
    }

    public String start(UUID key) {
        return client.createMultipartUpload(
                        CreateMultipartUploadRequest.builder()
                                .bucket(bucket)
                                .key(prefix + key)
                                .build())
                .uploadId();
    }

    public String signedPart(UUID key, String uploadId, int number) {
        return signer.presignUploadPart(
                        software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest
                                .builder()
                                .signatureDuration(Duration.ofMinutes(10))
                                .uploadPartRequest(
                                        UploadPartRequest.builder()
                                                .bucket(bucket)
                                                .key(prefix + key)
                                                .uploadId(uploadId)
                                                .partNumber(number)
                                                .build())
                                .build())
                .url()
                .toString();
    }

    public java.util.List<com.skillproof.backend.media.application.port.MultipartObjectUpload.Part>
            parts(UUID key, String uploadId) {
        return client
                .listPartsPaginator(
                        ListPartsRequest.builder()
                                .bucket(bucket)
                                .key(prefix + key)
                                .uploadId(uploadId)
                                .build())
                .parts()
                .stream()
                .map(
                        p ->
                                new com.skillproof.backend.media.application.port
                                        .MultipartObjectUpload.Part(
                                        p.partNumber(), p.size(), p.eTag()))
                .sorted(
                        java.util.Comparator.comparingInt(
                                com.skillproof.backend.media.application.port.MultipartObjectUpload
                                                .Part
                                        ::number))
                .toList();
    }

    public void assemble(
            UUID key,
            String uploadId,
            java.util.List<com.skillproof.backend.media.application.port.MultipartObjectUpload.Part>
                    parts) {
        client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(prefix + key)
                        .uploadId(uploadId)
                        .multipartUpload(
                                CompletedMultipartUpload.builder()
                                        .parts(
                                                parts.stream()
                                                        .map(
                                                                p ->
                                                                        CompletedPart.builder()
                                                                                .partNumber(
                                                                                        p.number())
                                                                                .eTag(p.etag())
                                                                                .build())
                                                        .toList())
                                        .build())
                        .build());
    }

    public void abort(UUID key, String uploadId) {
        try {
            client.abortMultipartUpload(
                    AbortMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(prefix + key)
                            .uploadId(uploadId)
                            .build());
        } catch (NoSuchUploadException ignored) {
            /* Already completed or aborted. */
        }
    }
}
