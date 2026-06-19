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
@Table(name = "CARD_CATEGORY")
public class CardCategoryCommandEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_category_seq")
    @SequenceGenerator(name = "card_category_seq", sequenceName = "CARD_CATEGORY_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "CODE", nullable = false, unique = true)
    private Integer code;

    @Column(name = "NAME", nullable = false,  unique = true,length = 100)
    private String name; // e.g. CREDIT, DEBIT, PREPAID, POSTPAID

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;
}