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
@Table(name = "banks")
public class BankCommandEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_seq")
    @SequenceGenerator(name = "bank_seq", sequenceName = "BANK_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "BIN_CODE", length = 6, nullable = false, unique = true)
    private String binCode;

    @Column(name = "bank_name" , nullable = false, length = 100)
    private String  name;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

}