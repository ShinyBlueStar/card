package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.domain.enums.CardRequestType;
import com.sample.system.card.service.domain.enums.CardRequestStatus;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CARD_REQUEST")
@Inheritance(strategy = InheritanceType.JOINED)// change the type
@DiscriminatorColumn(name = "REQUEST_TYPE", discriminatorType = DiscriminatorType.STRING, length = 100)
public class CardRequestCommandEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_request_seq")
    @SequenceGenerator(name = "card_request_seq", sequenceName = "card_request_seq", allocationSize = 1)
    private Long id;

    @Column(name = "REQUEST_STATUS")
    @Convert(converter = EnumConverters.CardRequestStatusConverter.class)
    private CardRequestStatus requestStatus; // وضعیت درخواست

    @Transient
     private CardRequestType requestType; // mapped from discriminator column
    // ToDo change to table and create a relationship between them put column type unique , put them in redis,
// persian name field

    @Column(name = "UNIT_CODE")
    private String unitCode;

    @Column(name = "UNIT_NAME")
    private String unitName;

    @Column(name = "UNIT_ID", length = 20)
    private String unitId;

    @Column(name = "NATIONAL_ID", length = 20)
    private String nationalId;

    @Column(name = "ISSUER_PERSON_ID", length = 20)
    private String issuerPersonId;

    @Column(name = "PROFILE_ID")
    private Long profileId;

    @Column(name = "FIRST_NAME", length = 100)
    private String firstName;

    @Column(name = "LAST_NAME", length = 100)
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    private CardCommandEntity card;

}
