package com.sample.system.card.service.domain.command.cardProfile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CreateCardProfileCommand {
    @NotNull(message = "نام پروفایل الزامی است")
    @NotBlank(message = "نام پروفایل نمی‌تواند خالی باشد")
    @Size(min = 2, max = 100, message = "نام پروفایل باید بین 2 تا 100 کاراکتر باشد")
    private final String profileName;
    @NotNull(message = "صادرکننده الزامی است")
    private final Long issuingBankId;
    @NotNull(message = "پروفایل الگوی کارمزد الزامی است")
    private final Long feeProfileId; // FeeProfileId
    @NotNull(message = "نوع کارت الزامی است")
    private final Long cardCategoryId; // CardCategory ID
    @NotNull(message = "تکنولوژی کارت الزامی است")
    private final Long cardTypeId;// CardType ID
    @NotNull(message = "حداقل طول رمز الزامی است")
    private final Integer minimumPasswordLength;// حداقل طول رمز
    @NotNull(message = "حداکثر طول رمز الزامی است")
    private final Integer maximumPasswordLength;//حداکثر طول رمز
    @NotNull(message = "حداقل طول رمز دوم الزامی است")
    private final Integer minimumSecondaryPasswordLength;// حداقل طول رمز دوم
    @NotNull(message = "حداکثر طول رمز دوم الزامی است")
    private final Integer maximumSecondaryPasswordLength;//حداکثر طول رمز دوم
    @NotNull(message = "دفعات مجاز تکرار رمز اول الزامی است")
    private final Integer allowedPinAttempts;//دفعات مجاز تکرار PIN
    @Min(value = 3, message = "طول cvv2 باید 3,4")
    @Max(value = 4, message = "طول cvv2 باید 3,4 باشد")
    @NotNull(message = "طول CVV2 الزامی است")
    private final Integer cvv2Length;
    @Min(value = 1, message = "مدت اعتبار کارت باید بزرگ‌تر از صفر باشد")
    @Max(value = 120, message = "مدت اعتبار کارت باید حداکثر 120 ماه باشد")
    @NotNull(message = "دوره اعتبار کارت الزامی است")
    private final Integer validityPeriod;// دوره اعتبار کارت (ماه، سال، روز)
    @Schema(defaultValue = "ماه")
    @NotBlank(message = "در حال حاضر پیش فرض ماه است")
    private final String validityType; // نوع دوره (M=ماهانه، Y=سالانه)
    @Schema(defaultValue = "1", allowableValues = {"1", "0"})
    @NotNull(message = "نحوه صدور الزامی است")
    private final Integer cardIssueMethod;
    @NotNull(message = "الگوی شماره کارت الزامی است")
    private final Long cardNumberPatternId;
    @NotNull(message = "وضعیت اولیه کارت الزامی است")
    private final Boolean initialCardStatus;// وضعیت اولیه کارت (فعال، غیرفعال)
    @NotNull(message = "انتخاب یک گزینه الزامی است")
    private final Boolean changeCvv2OnExpirationDateRenewal; // تغییر CVV2 هنگام تمدید تاریخ انقضا
    @NotNull(message = "وضعیت فعال بودن پروفایل الزامی است")
    private final Boolean isActive; // فعال / غیرفعال
    private final Boolean additionalInfoIsRequired;
    @NotNull(message = "نحوه تولید رمز دوم الزامی است")
    @Min(value = 1, message = "نحوه تولید رمز دوم فقط می‌تواند 1،2 ")
    @Max(value = 3, message = "نحوه تولید رمز دوم فقط می‌تواند 1،2 " )
    @Schema(defaultValue = "1", allowableValues = {"1", "2", "3"})
    private final Integer pin2GenMethod; //enum Pin2GenMethod
    @Min(value = 1, message = "قابلیت شارژ مجدد فقط می‌تواند 1، 2 یا 3 باشد")
    @Max(value = 3, message = "قابلیت شارژ مجدد فقط می‌تواند 1، 2 یا 3 باشد")
    @Schema(defaultValue = "1", allowableValues = {"1", "2", "3"})
    private final Integer reloadable;//  قابلیت شارژ مجدد (0=غیرقابل شارژ, 1=قابل شارژ)
    @NotNull(message = "تعداد مجاز تکرار رمز دوم الزامی است")
    private final Integer allowedPin2Attempts; // دفعات مجاز تکرار PIN2
    private final Integer salesMethod;// نحوه فروش  SalesMethod
    private final Integer maxNumCreditCard; // حداکثر کارت اعتباری
    private final BigDecimal maximumAllowedAmount;//حداکثر مبلغ مجاز
    @Schema(defaultValue = "1", allowableValues = {"1", "2"})
    private final Integer cardRenewalType; // نوع تمدید کارت (کد enum CardRenewalType)
}
