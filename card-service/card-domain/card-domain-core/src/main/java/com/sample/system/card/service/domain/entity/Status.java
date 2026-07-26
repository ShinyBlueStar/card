package com.sample.system.card.service.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Status extends BaseEntity<Long> {

    private String persianDescription;
    private String code;
    private String description;

}
