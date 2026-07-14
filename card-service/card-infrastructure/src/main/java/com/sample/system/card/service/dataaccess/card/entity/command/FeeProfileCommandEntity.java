package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Fee Profile Command Entity
 * Represents a group of fees associated with cards or transactions.
 * Follows DDD + Hexagonal Architecture conventions.
 */
@Audited
@AuditOverride(forClass = Auditable.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "FEE_PROFILES")
public class FeeProfileCommandEntity extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fee_profile_seq")
    @SequenceGenerator(name = "fee_profile_seq", sequenceName = "FEE_PROFILE_SEQ", allocationSize = 1)
    private Long id;

    /** نام پروفایل کارمزد */
    @Column(name = "PROFILE_NAME", nullable = false, unique = true, length = 100)
    private String name;

    /** لیست کارمزدهای مرتبط با این پروفایل */
    @OneToMany(
            mappedBy = "feeProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<FeeCommandEntity> fees = new ArrayList<>();

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;
}
