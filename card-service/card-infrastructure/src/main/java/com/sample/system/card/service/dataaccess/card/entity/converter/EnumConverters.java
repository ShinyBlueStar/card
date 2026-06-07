package com.sample.system.card.service.dataaccess.card.entity.converter;

import com.sample.system.card.service.domain.enums.ActionType;
import com.sample.system.card.service.domain.enums.CardIssueMethod;
import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.enums.CardPhysicalStatus;
import com.sample.system.card.service.domain.enums.CardRenewalType;
import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.domain.enums.CardStatus;
import com.sample.system.card.service.domain.enums.FeePeriod;
import com.sample.system.card.service.domain.enums.FeeType;
import com.sample.system.card.service.domain.enums.Pin2GenMethod;
import com.sample.system.card.service.domain.enums.Reloadable;
import com.sample.system.card.service.domain.enums.SalesMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// کانورترهای اینام هایی که در دیتابیس با کد ذخیره میشوند
public final class EnumConverters {

    private EnumConverters() {
    }

    @Converter
    public static class CardStatusConverter extends EnumCodeConverter<CardStatus> {
        public CardStatusConverter() {
            super(CardStatus.class);
        }
    }

    @Converter
    public static class CardPhysicalStatusConverter extends EnumCodeConverter<CardPhysicalStatus> {
        public CardPhysicalStatusConverter() {
            super(CardPhysicalStatus.class);
        }
    }

    @Converter
    public static class CardNumberPatternStatusConverter extends EnumCodeConverter<CardNumberPatternStatus> {
        public CardNumberPatternStatusConverter() {
            super(CardNumberPatternStatus.class);
        }
    }

    @Converter
    public static class CardNumGenerationMethodConverter extends EnumCodeConverter<CardNumGenerationMethod> {
        public CardNumGenerationMethodConverter() {
            super(CardNumGenerationMethod.class);
        }
    }

    @Converter
    public static class ReloadableConverter extends EnumCodeConverter<Reloadable> {
        public ReloadableConverter() {
            super(Reloadable.class);
        }
    }

    @Converter
    public static class SalesMethodConverter extends EnumCodeConverter<SalesMethod> {
        public SalesMethodConverter() {
            super(SalesMethod.class);
        }
    }

    @Converter
    public static class Pin2GenMethodConverter extends EnumCodeConverter<Pin2GenMethod> {
        public Pin2GenMethodConverter() {
            super(Pin2GenMethod.class);
        }
    }

    @Converter
    public static class CardIssueMethodConverter extends EnumCodeConverter<CardIssueMethod> {
        public CardIssueMethodConverter() {
            super(CardIssueMethod.class);
        }
    }

    @Converter
    public static class CardRequestStatusConverter extends EnumCodeConverter<CardRequestStatus> {
        public CardRequestStatusConverter() {
            super(CardRequestStatus.class);
        }
    }

    @Converter
    public static class FeeTypeConverter extends EnumCodeConverter<FeeType> {
        public FeeTypeConverter() {
            super(FeeType.class);
        }
    }

    @Converter
    public static class ActionTypeConverter extends EnumCodeConverter<ActionType> {
        public ActionTypeConverter() {
            super(ActionType.class);
        }
    }

    @Converter
    public static class FeePeriodConverter extends EnumCodeConverter<FeePeriod> {
        public FeePeriodConverter() {
            super(FeePeriod.class);
        }
    }

    // ستون CARD_RENEWAL_TYPE از نوع رشته است و کد را به صورت متن نگه میدارد
    @Converter
    public static class CardRenewalTypeConverter implements AttributeConverter<CardRenewalType, String> {

        @Override
        public String convertToDatabaseColumn(CardRenewalType value) {
            return value == null ? null : String.valueOf(value.getCode());
        }

        @Override
        public CardRenewalType convertToEntityAttribute(String code) {
            return code == null ? null : CardRenewalType.fromCode(Integer.parseInt(code.trim()));
        }
    }
}
