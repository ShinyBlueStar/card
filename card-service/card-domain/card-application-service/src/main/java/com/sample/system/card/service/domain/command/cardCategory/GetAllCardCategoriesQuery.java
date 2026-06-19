package com.sample.system.card.service.domain.command.cardCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetAllCardCategoriesQuery {
    private final Boolean activeOnly;

    public GetAllCardCategoriesQuery() {
        this.activeOnly = false;
    }
}