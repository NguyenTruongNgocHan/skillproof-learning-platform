package com.skillproof.backend.media.domain;

import java.util.Arrays;

import com.skillproof.backend.common.exception.BadRequestException;

/**
 * Identifies supported uploads by their file signatures before storage. *
 */
final public class MediaSignature {

    private MediaSignature() {
    }

    public static String detect(byte[] head) {
        if (head.length >= 3 && (head[0] & 255) == 255 && (head[1] & 255) == 216 && (head[2] & 255) == 255) {
            return "image/jpeg";
        }
        if (head.length >= 8 && Arrays.equals(Arrays.copyOf(head, 8), new byte[]{
            (byte) 137, 80, 78, 71, 13, 10, 26, 10
        })) {
            return "image/png";
        }
        if (head.length >= 12 && new String(head, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(head, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP")) {
            return "image/webp";
        }
        if (head.length >= 5 && new String(head, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) {
            return "application/pdf";
        }
        if (head.length >= 12 && new String(head, 4, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("ftyp")) {
            return "video/mp4";
        }
        if (head.length >= 4 && Arrays.equals(Arrays.copyOf(head, 4), new byte[]{
            26, 69, (byte) 223, (byte) 163
        })) {
            return "video/webm";
        }
        if (head.length >= 3 && new String(head, 0, 3, java.nio.charset.StandardCharsets.US_ASCII).equals("ID3")) {
            return "audio/mpeg";
        }
        if (head.length >= 2 && (head[0] & 255) == 255 && ((head[1] & 224) == 224)) {
            return "audio/mpeg";
        }
        if (head.length >= 12 && new String(head, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(head, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WAVE")) {
            return "audio/wav";
        }
        throw new BadRequestException("UNSUPPORTED_MEDIA", "Allowed: JPEG, PNG, WebP, PDF, MP4, WebM, MP3, WAV");
    }
}
