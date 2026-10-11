package com.skillproof.backend.pricing.contract;

import java.util.UUID;

public interface CourseSaleQuery {
    record Sale(String state, String accessMode, long learningPrice, long certificationPrice) {}

    Sale sale(UUID version, String mode, long learning, long certification);
}
