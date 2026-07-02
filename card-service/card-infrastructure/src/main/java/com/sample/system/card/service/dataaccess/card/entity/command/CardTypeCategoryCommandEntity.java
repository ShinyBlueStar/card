package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

/**
 * CardTypeCategory Command Entity - Intermediate Entity
 * Represents the relationship between CardType and CardCategory
 * Following DDD principles with relationship-specific attributes
 */
@Audited
@AuditOverride(forClass = Auditable.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CARD_TYPE_CATEGORY")
public class CardTypeCategoryCommandEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_type_category_seq")
    @SequenceGenerator(name = "card_type_category_seq", sequenceName = "CARD_TYPE_CATEGORY_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_TYPE_ID", nullable = false)
    private CardTypeCommandEntity cardType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_CATEGORY_ID", nullable = false)
    private CardCategoryCommandEntity cardCategory;

    @Column(name = "IS_ACTIVE")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Activate this relationship
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate this relationship
     */
    public void deactivate() {
        this.isActive = false;
    }

}
