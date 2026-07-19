package com.sample.system.card.service.domain.command.numberPattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query for getting all card number patterns with optional filters
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetAllCardNumberPatternsQuery {

    private final String status;
    private final Long cardProfileId;
    private final String belongTo;
    private final String placeInto;
    private final String description;
}
