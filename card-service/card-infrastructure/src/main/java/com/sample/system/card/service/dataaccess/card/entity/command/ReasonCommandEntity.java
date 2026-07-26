package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import com.sample.system.card.service.domain.enums.ReasonGroup;
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
@Table(name = "action_reason")
public class ReasonCommandEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reason_seq")
    @SequenceGenerator(name = "reason_seq", sequenceName = "reason_seq", allocationSize = 1)
    private Long id;

    @Column(name = "code")
    private Integer code;

    @Column(name = "title")
    String title;

    @Column(name = "reason")
    String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_id")
    private ReasonGroup groupId;
}
