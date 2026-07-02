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
@Table(name = "CARD_TYPE")
public class CardTypeCommandEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_type_seq")
    @SequenceGenerator(name = "card_type_seq", sequenceName = "CARD_TYPE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "CODE", nullable = false, unique = true)
    private Integer code; //

    @Column(name = "NAME", nullable = false, unique = true, length = 100)
    private String name; // e.g. MAGNETIC, SMART, HYBRID

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "IS_ACTIVE")
    @Builder.Default
    private Boolean isActive = true;

}
