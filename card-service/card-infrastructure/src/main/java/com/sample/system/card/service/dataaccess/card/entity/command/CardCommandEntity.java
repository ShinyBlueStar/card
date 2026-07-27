package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.domain.enums.CardPhysicalStatus;
import com.sample.system.card.service.domain.enums.CardStatus;
import com.sample.system.card.service.dataaccess.card.entity.converter.EnumConverters;
import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.AuditOverride;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Audited
@AuditOverride(forClass = Auditable.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "Card.withProfileAndCustomer",
        attributeNodes = {
                @NamedAttributeNode("cardProfile"),
                @NamedAttributeNode("customer")
        }
)
@Entity
@Table(name = "cards")
public class CardCommandEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_seq")
    @SequenceGenerator(name = "card_seq", sequenceName = "CARD_SEQ", allocationSize = 1)
    private Long id;

    /**
     * Unique card identifier (UUID) for SSM integration; generated on card creation.
     */
    @Column(name = "CARD_ID", unique = true, length = 36)
    private UUID cardId;

    @Column(name = "ISSUE_DATE")
    private LocalDateTime issueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_PROFILE_ID")
    private CardProfileCommandEntity cardProfile;

    @Column(name = "CURRENT_STATUS")
    @Convert(converter = EnumConverters.CardStatusConverter.class)
    private CardStatus cardStatus;

    @OneToOne(mappedBy = "card", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private TrackCommandEntity track;

    @Builder.Default
    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY)
    private List<CardRequestCommandEntity> requests = new ArrayList<>();

    @Column(name = "CASE_NUMBER", length = 200)
    private String caseNumber;

    @Column(name = "CVV2", length = 10)
    private String cvv2;

    // full PAN column (CSV has PAN too)
    @Column(name = "PAN", length = 32, nullable = false)
    private String pan;

    // Masked PAN for logging purposes (PCI DSS compliance)
    @Column(name = "MASKED_PAN", length = 32)
    private String maskedPan;

    @Column(name = "PHYSICAL_STATUS")
    @Convert(converter = EnumConverters.CardPhysicalStatusConverter.class)
    private CardPhysicalStatus physicalStatus;

    @Column(name = "EXPDATE", length = 50)
    private String expDate;

    @Column(name = "BonCardText", length = 2000)
    private String bonCardText;

    @Column(name = "CreditLimit", precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "Address", length = 2000)
    private String address;

/// /////////////////////////////////////////////////////////
    @Column(name = "UNIT_CODE", length = 50)
    private String unitCode;

    @Column(name = "UNIT_NAME", length = 200)
    private String unitName;

    @Column(name = "ISSUING_BANK", length = 50)
    private String issuingBank;//?

    @Column(name = "DELEIVERY_PERSON_ID", length = 20)
    private String deliveryPersonId;

    @Column(name = "ISSUER_PERSON_ID", length = 20)
    private String issuerPersonId;

    @Column(name = "IS_VIRTUAL_CARD", length = 1)
    private Boolean isVirtualCard;

    @Column(name = "RECIVED_DATE")
    private LocalDateTime receivedDate;

    @Column(name = "SENT_DATE")
    private LocalDateTime sentDate;

    @Column(name = "IS_2NDPASS_HOT")
    private Boolean isSecondPassHot;

    @Column(name = "CHANGE_STATE_DESCRIPTION")
    private String changeStateDescription;//

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private CustomerCommandEntity customer;
}