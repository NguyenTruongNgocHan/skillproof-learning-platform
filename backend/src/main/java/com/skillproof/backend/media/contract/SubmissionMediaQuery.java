package com.skillproof.backend.media.contract;

import java.util.UUID;

public interface SubmissionMediaQuery {

    boolean hasSubmissionFiles(UUID submissionId);
}
