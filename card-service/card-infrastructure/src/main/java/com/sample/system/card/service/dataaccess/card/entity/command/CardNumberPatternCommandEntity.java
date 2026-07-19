package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.dataaccess.card.entity.converter.EnumConverters;
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
@Table(name = "card_number_ranges")
public class CardNumberPatternCommandEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_number_range_seq")
    @SequenceGenerator(name = "card_number_range_seq", sequenceName = "card_number_range_seq", allocationSize = 1)
    private Long id;

    @Column(name = "CARD_NUMBER_FROM", length = 7)
    private String cardNumberFrom;

    @Column(name = "CARD_NUMBER_TO", length = 7)
    private String cardNumberTo;

    @Column(name = "FIRST_CARD_NUMBER", length = 20)
    private String firstCardNumber;

    @Column(name = "LAST_CARD_NUMBER", length = 20)
    private String lastCardNumber;

    @Column(name = "NEXT_CARD_NUMBER", length = 20)
    private String nextCardNumber;

    @Column(name = "PATTERN_STATUS")
    @Convert(converter = EnumConverters.CardNumberPatternStatusConverter.class)
    private CardNumberPatternStatus patternStatus;

    @Column(name = "CARD_GEN_METHOD")
    @Convert(converter = EnumConverters.CardNumGenerationMethodConverter.class)
    private CardNumGenerationMethod cardNumGenerationMethod;

    @Column(name = "PRODUCT_CODE", length = 10, nullable = false)
    private String productCode;

    @Column(name = "NAME", length = 50, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_PROFILE_ID")
    private CardProfileCommandEntity cardProfile;
}
