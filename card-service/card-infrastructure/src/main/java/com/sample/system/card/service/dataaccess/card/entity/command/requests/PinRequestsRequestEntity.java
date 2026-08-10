package com.sample.system.card.service.dataaccess.card.entity.command.requests;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
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
@Table(name = "CARD_REQUEST_PIN_REQUESTS")
public class PinRequestsRequestEntity extends CardRequestCommandEntity {
    // درخواست‌های رمز - کلاس عمومی برای درخواست‌های مرتبط با PIN
}

