package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Status")
@Entity
public class StatusCommandEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_status_seq")
    @SequenceGenerator(name = "card_status_seq", sequenceName = "card_status_seq", allocationSize = 1)
    private Long id;

    private String code;
    private String description;
    private String persianDescription;
}