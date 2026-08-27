package com.sample.system.card.service.domain.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GenerateAccessFileForRequestCommand {
    @NotNull
    private final Long requestId;
}
