package com.sample.system.card.service.domain.command.cardType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetAllCardTypesQuery {
    private final Boolean activeOnly;

    public GetAllCardTypesQuery() {
        this.activeOnly = false;
    }
}