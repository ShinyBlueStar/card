package com.sample.system.card.service.domain.response.accessfile.result;

import lombok.Builder;
import lombok.Value;
import java.nio.file.Path;

@Value
@Builder
public class AccessFileResult {
    Path filePath;

    String fileName;

    String downloadUrl;

    int recordCount;

    int trackCount;

    int customerCount;
}
