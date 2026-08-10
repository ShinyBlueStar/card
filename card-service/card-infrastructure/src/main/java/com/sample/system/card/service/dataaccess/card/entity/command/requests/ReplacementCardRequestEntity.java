package com.sample.system.card.service.dataaccess.card.entity.command.requests;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import com.sample.system.card.service.dataaccess.card.entity.command.CardRequestCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.ReasonCommandEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.AuditOverrides;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Audited
@AuditOverrides({
        @AuditOverride(forClass = CardRequestCommandEntity.class),
        @AuditOverride(forClass = Auditable.class)
})
@Entity
@ToString(callSuper = true)
@PrimaryKeyJoinColumn(name = "card_request_id", referencedColumnName = "ID")
@Table(name = "CARD_REQUEST_REPLACEMENT")
@DiscriminatorValue("CARD_REPLACEMENT")
public class ReplacementCardRequestEntity extends CardRequestCommandEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REASON_ID")
    private ReasonCommandEntity reason;// سرقت - مفقودی  - تعویض(آسیب دیدن کارت) - منقضی شدن کارت

    @Column(name = "OLD_CARD")
    private Long oldCardId;
    // Note: cardId is stored in parent entity CardRequestCommandEntity.card
}
