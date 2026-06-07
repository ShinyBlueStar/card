package com.sample.system.card.service.dataaccess.card.entity.converter;

import com.sample.system.card.service.domain.enums.CardRenewalType;
import com.sample.system.card.service.domain.enums.CardStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumConvertersTest {

    private final EnumConverters.CardStatusConverter statusConverter = new EnumConverters.CardStatusConverter();
    private final EnumConverters.CardRenewalTypeConverter renewalConverter = new EnumConverters.CardRenewalTypeConverter();

    @ParameterizedTest
    @EnumSource(CardStatus.class)
    void storesTheEnumCodeAndReadsItBack(CardStatus status) {
        Integer column = statusConverter.convertToDatabaseColumn(status);

        assertThat(column).isEqualTo(status.getCode());
        assertThat(statusConverter.convertToEntityAttribute(column)).isEqualTo(status);
    }

    @Test
    void nullStaysNull() {
        assertThat(statusConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(statusConverter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void unknownCodeInDatabaseFailsLoudly() {
        assertThatThrownBy(() -> statusConverter.convertToEntityAttribute(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CardStatus");
    }

    @Test
    void renewalTypeIsStoredAsTextCode() {
        assertThat(renewalConverter.convertToDatabaseColumn(CardRenewalType.RENEW_WITH_PRINT)).isEqualTo("2");
        assertThat(renewalConverter.convertToEntityAttribute(" 1 ")).isEqualTo(CardRenewalType.RENEW_WITHOUT_PRINT);
        assertThat(renewalConverter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void findLooksUpByCode() {
        assertThat(EnumCodeConverter.find(CardStatus.class, 4)).contains(CardStatus.HOT);
        assertThat(EnumCodeConverter.find(CardStatus.class, 77)).isEmpty();
    }
}
