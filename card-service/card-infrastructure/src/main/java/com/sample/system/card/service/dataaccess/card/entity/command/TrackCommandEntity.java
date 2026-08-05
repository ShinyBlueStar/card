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
@Table(name = "tracks")
public class TrackCommandEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_seq")
    @SequenceGenerator(name = "track_seq", sequenceName = "TRACK_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "RTRACK1", length = 4000)
    private String  track1;

    @Column(name = "RTRACK2", length = 4000)
    private String  track2;

    @Column(name = "RTRACK3", length = 4000)
    private String  track3;

    @Column(name = "PIN1" , length = 200)
    private String  pin1;

    @Column(name = "PIN2" , length = 200)
    private String  pin2;

    @Column(name = "PIN1_PART2", length = 200)
    private String pin1Part2;

    @Column(name = "PIN2_PART2", length = 200)
    private String pin2Part2;

    @OneToOne
    @JoinColumn(name = "CARD_ID")
    private CardCommandEntity card;
}