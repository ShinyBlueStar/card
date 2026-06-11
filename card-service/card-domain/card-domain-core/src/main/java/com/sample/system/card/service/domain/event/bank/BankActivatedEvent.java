package com.sample.system.card.service.domain.event.bank;

import com.sample.system.card.service.domain.entity.Bank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BankActivatedEvent {
    private final Bank bank;
    private final ZonedDateTime activatedAt;
}
