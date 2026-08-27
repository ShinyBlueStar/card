package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

@Audited
@AuditOverride(forClass = Auditable.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_downloads", indexes = {
    @Index(name = "idx_hash_id", columnList = "hash_id", unique = true)
})
public class FileDownloadCommandEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_download_seq")
    @SequenceGenerator(name = "file_download_seq", sequenceName = "FILE_DOWNLOAD_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "hash_id", length = 255, nullable = false, unique = true)
    private String hashId;

    @Column(name = "file_path", length = 1000, nullable = false)
    private String filePath;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "expires_at")
    private java.sql.Timestamp expiresAt;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;
}

