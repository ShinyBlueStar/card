package com.sample.system.card.service.dataaccess.card.entity.command.requests;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardRequestCommandEntity;
import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "CARD_REQUEST_LOGICAL_ISSUANCE")
@DiscriminatorValue("CARD_ISSUANCE")
public class IssueCardRequestEntity extends CardRequestCommandEntity {
    // صدور کارت منطقی - درخواست صدور اولین بار PAN کارت که از طرف کاربر درخواست شده
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_PROFILE_ID")
    private CardProfileCommandEntity cardProfile;

    @Column(name = "CASE_NUMBER")
    private String caseNumber;
}

