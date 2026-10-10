package com.skillproof.backend.library.domain;

public final class LibraryRules {

    private LibraryRules() {
    }

    public static boolean validPrice(long price) {
        return price == 0 || price >= 5000 && price <= 1_000_000_000L;
    }

    public static boolean hasContent(String body, boolean hasFiles) {
        return body != null && !body.isBlank() || hasFiles;
    }
}
