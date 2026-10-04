package com.skillproof.backend.media;

public class StorageException extends IllegalStateException {

    private final String code;

    public StorageException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
