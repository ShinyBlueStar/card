package com.sample.system.card.service.domain.entity;

import lombok.*;

import java.io.Serializable;

/**
 * Customer Domain Entity
 * Represents a customer in the card service domain
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AggregateRoot<Long> implements Serializable {

    private String nationalId;
    private String firstName;
    private String lastName;
    private String address;
}

