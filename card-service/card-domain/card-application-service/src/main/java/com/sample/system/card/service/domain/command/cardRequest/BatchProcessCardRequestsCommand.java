package com.sample.system.card.service.domain.command.cardRequest;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BatchProcessCardRequestsCommand {
    @NotEmpty(message = "Request IDs list cannot be empty")
    private final List<Long> requestIds;
}

