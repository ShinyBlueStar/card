package com.sample.system.card.service.domain.command.numberPattern;

import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CreateCardNumberPatternCommand implements Serializable {
    @NotNull(message = "کد محصول الزامی است")
    @Size(min = 2, max = 2, message = "کد محصول باید دقیقاً ۲ رقم باشد")
    @Pattern(regexp = "\\d{2}", message = "کد محصول باید فقط شامل دو رقم (۰-۹) باشد")
    private final String productCode;
    @NotNull(message = "انتخاب روش تولید شماره کارت الزامی است")
    private final Integer cardNumGenerationMethod;
    @NotNull(message = "نام الگوی تولید شماره کارت الزامی است")
    private final String name;

    @Schema(defaultValue = "1", allowableValues = {"1", "2"})
    private final Integer status; // PatternStatus: 1=ACTIVE, 2=INACTIVE

    @AssertTrue(message = "وضعیت الگو باید 1 (فعال) یا 2 (غیرفعال) باشد")
    private boolean isStatusValid() {
        if (status == null) {
            return true; // null is allowed (optional field)
        }
        return Set.of(1, 2).contains(status);
    }

    private final String cardNumberFrom;
    private final String cardNumberTo;

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && !cardNumber.isBlank() && cardNumber.matches("\\d{7}");
    }

    @AssertTrue(message = "شروع بازه شماره کارت الزامی است و باید 7 رقمی باشد")
    private boolean isCardNumberFromValid() {
        CardNumGenerationMethod method = CardNumGenerationMethod.fromCode(cardNumGenerationMethod);

        if (method == CardNumGenerationMethod.INCREMENTAL) {
            return true;
        }
        return isValidCardNumber(cardNumberFrom);
    }

    @AssertTrue(message = "پایان بازه شماره کارت الزامی است و باید 7 رقمی باشد")
    private boolean isCardNumberToValid() {
        CardNumGenerationMethod method = CardNumGenerationMethod.fromCode(cardNumGenerationMethod);

        if (method == CardNumGenerationMethod.INCREMENTAL) {
            return true;
        }
        return isValidCardNumber(cardNumberTo);
    }

    @AssertTrue(message = "برای روش افزایشی (INCREMENTAL)، فیلدهای شروع و پایان بازه باید خالی باشند")
    private boolean isRangeFieldsNullForIncremental() {
        CardNumGenerationMethod method = CardNumGenerationMethod.fromCode(cardNumGenerationMethod);

        if (method == CardNumGenerationMethod.INCREMENTAL) {
            return cardNumberFrom == null && cardNumberTo == null;
        }
        return true; // ignore for RANGE_BASED
    }
}
