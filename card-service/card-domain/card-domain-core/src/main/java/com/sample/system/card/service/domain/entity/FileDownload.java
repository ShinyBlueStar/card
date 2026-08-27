package com.sample.system.card.service.domain.entity;

import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownload extends AggregateRoot<Long> implements Serializable {

    private String hashId;
    private String filePath;
    private String fileName;
    private Long requestId;
    private Timestamp expiresAt;
    private Boolean isUsed;
}

