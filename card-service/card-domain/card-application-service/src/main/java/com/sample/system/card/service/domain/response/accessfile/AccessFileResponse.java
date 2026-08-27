package com.sample.system.card.service.domain.response.accessfile;

import com.sample.system.card.service.domain.response.accessfile.result.AccessFileResult;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessFileResponse {
    private String fileName;
    private String downloadUrl;
    private int recordCount;
    private int trackCount;
    private int customerCount;

    /**
     * Factory method to create response from AccessFileResult
     * Following DDD - converts domain result to application response
     */
    public static AccessFileResponse from(AccessFileResult result) {
        if (result == null) {
            return null;
        }

        return AccessFileResponse.builder()
                .fileName(result.getFileName())
                .downloadUrl(result.getDownloadUrl())
                .recordCount(result.getRecordCount())
                .trackCount(result.getTrackCount())
                .customerCount(result.getCustomerCount())
                .build();
    }
}
