package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.domain.enums.FeePeriod;
import com.sample.system.card.service.domain.enums.ActionType;
import com.sample.system.card.service.domain.enums.FeeType;
import com.sample.system.card.service.dataaccess.card.entity.converter.EnumConverters;
import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.math.BigDecimal;

@Audited
@AuditOverride(forClass = Auditable.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "FEES")
public class FeeCommandEntity extends Auditable implements Serializable {

    /** شناسه کارمزد */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fee_seq")
    @SequenceGenerator(name = "fee_seq", sequenceName = "FEE_SEQ", allocationSize = 1)
    private Long id;

    /** نام کارمزد */
    @Column(name = "FEE_NAME", nullable = false, length = 100)
    private String name;

    /** مبلغ کارمزد */
    @Column(name = "FEE_AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "FEE_TYPE")
    @Convert(converter = EnumConverters.FeeTypeConverter.class)
    private FeeType feeType;

    @Column(name = "FEE_VALUE")
    private String FeeValue;

    @Convert(converter = EnumConverters.ActionTypeConverter.class)
    private ActionType actionType;

    @Column(name = "FEE_PERIOD")
    @Convert(converter = EnumConverters.FeePeriodConverter.class)
    private FeePeriod feePeriod;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FEE_PROFILE_ID", nullable = false)
    private FeeProfileCommandEntity feeProfile;
}
