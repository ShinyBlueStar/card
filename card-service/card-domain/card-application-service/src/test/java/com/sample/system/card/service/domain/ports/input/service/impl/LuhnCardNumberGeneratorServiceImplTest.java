package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.CardNumberPatternService;
import com.sample.system.card.service.domain.ports.output.repository.CardPanCacheRepository;
import com.sample.system.card.service.domain.valueObject.CardNumberDto;
import com.sample.system.card.service.domain.valueObject.CardNumberPatternId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LuhnCardNumberGeneratorServiceImplTest {

    @Mock
    private CardNumberPatternService patternService;
    @Mock
    private CardPanCacheRepository cache;
    @InjectMocks
    private LuhnCardNumberGeneratorServiceImpl generator;

    private CardProfile profile;
    private CardNumberPattern pattern;

    @BeforeEach
    void setUp() {
        profile = CardProfile.builder().bank(Bank.builder().binCode("603799").build()).build();
        profile.setId(new CardProfileId(1L));
        pattern = CardNumberPattern.builder().productCode("12").build();
        pattern.setId(new CardNumberPatternId(10L));
    }

    @Test
    void generatesSixteenDigitPanThatPassesLuhnCheck() throws CardDomainException {
        when(patternService.getActivePatternForProfile(profile)).thenReturn(pattern);
        when(patternService.getNextAndIncrement(pattern)).thenReturn("0000001");
        when(cache.exists(anyString())).thenReturn(false);

        String pan = generator.process(new CardNumberDto(profile));

        assertThat(pan).hasSize(16).startsWith("603799" + "12" + "0000001");
        assertThat(isLuhnValid(pan)).isTrue();
        verify(patternService).save(pattern);
    }

    @Test
    void retriesWhenGeneratedPanAlreadyExists() throws CardDomainException {
        when(patternService.getActivePatternForProfile(profile)).thenReturn(pattern);
        when(patternService.getNextAndIncrement(pattern)).thenReturn("0000001", "0000002");
        when(cache.exists(anyString())).thenReturn(true, false);

        String pan = generator.process(new CardNumberDto(profile));

        assertThat(pan).startsWith("603799120000002");
        verify(patternService, times(2)).getNextAndIncrement(pattern);
    }

    @Test
    void failsAfterMaxAttemptsWithoutSaving() throws CardDomainException {
        when(patternService.getActivePatternForProfile(profile)).thenReturn(pattern);
        when(patternService.getNextAndIncrement(pattern)).thenReturn("0000001");
        when(cache.exists(anyString())).thenReturn(true);

        assertThatThrownBy(() -> generator.process(new CardNumberDto(profile)))
                .isInstanceOf(CardDomainException.class);
        verify(patternService, times(5)).getNextAndIncrement(pattern);
        verify(patternService, never()).save(pattern);
    }

    @Test
    void rejectsInvalidBin() {
        profile.setBank(Bank.builder().binCode("12AB").build());

        assertThatThrownBy(() -> generator.process(new CardNumberDto(profile)))
                .isInstanceOf(CardDomainException.class)
                .hasMessage("Invalid BIN");
    }

    @Test
    void rejectsInvalidProductCode() throws CardDomainException {
        pattern.setProductCode("123");
        when(patternService.getActivePatternForProfile(profile)).thenReturn(pattern);

        assertThatThrownBy(() -> generator.process(new CardNumberDto(profile)))
                .isInstanceOf(CardDomainException.class)
                .hasMessage("Invalid ProductCode");
    }

    private static boolean isLuhnValid(String number) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
