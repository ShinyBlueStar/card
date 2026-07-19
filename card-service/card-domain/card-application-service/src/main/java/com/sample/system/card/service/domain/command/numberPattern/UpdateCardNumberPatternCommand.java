package com.sample.system.card.service.domain.command.numberPattern;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class UpdateCardNumberPatternCommand {

    @NotNull(message = "شناسه الگوی شماره کارت الزامی است")
    private final Long patternId;

    @Pattern(regexp = "\\d{7}", message = "شروع بازه شماره کارت باید 7 رقمی باشد")
    private final String cardNumberFrom;

    @Pattern(regexp = "\\d{7}", message = "پایان بازه شماره کارت باید 7 رقمی باشد")
    private final String cardNumberTo;

    private final String name;

    @Schema(defaultValue = "null", allowableValues = {"1", "2"})
    private final Integer status; // PatternStatus: 1=ACTIVE, 2=INACTIVE

    @AssertTrue(message = "وضعیت الگو باید 1 (فعال) یا 2 (غیرفعال) باشد")
    private boolean isStatusValid() {
        if (status == null) {
            return true; // null is allowed (optional field in update)
        }
        return Set.of(1, 2).contains(status);
    }
}