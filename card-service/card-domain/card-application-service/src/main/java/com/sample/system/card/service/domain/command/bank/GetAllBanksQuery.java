package com.sample.system.card.service.domain.command.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetAllBanksQuery {
    private final Boolean activeOnly;

    public GetAllBanksQuery() {
        this.activeOnly = false;
    }
}