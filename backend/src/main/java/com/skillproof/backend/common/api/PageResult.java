package com.skillproof.backend.common.api;

import java.util.List;

/** Stable pagination envelope for versioned HTTP contracts. */
public record PageResult<T>(
    List<T> content, long totalElements, int totalPages, int page, int size) {
  public static <T> PageResult<T> of(List<T> values, int page, int size) {
    if (page < 0 || size < 1 || size > 100)
      throw new com.skillproof.backend.common.exception.BadRequestException(
          "PAGE_INVALID", "Use page >= 0 and size 1–100");
    long offset = (long) page * size;
    var content =
        offset >= values.size()
            ? List.<T>of()
            : values.subList((int) offset, (int) Math.min(offset + size, values.size()));
    return new PageResult<>(
        List.copyOf(content), values.size(), (values.size() + size - 1) / size, page, size);
  }
}
