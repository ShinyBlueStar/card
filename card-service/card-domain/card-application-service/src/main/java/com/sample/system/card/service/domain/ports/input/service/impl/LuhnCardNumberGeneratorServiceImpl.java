package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.CardNumberGeneratorService;
import com.sample.system.card.service.domain.ports.input.service.CardNumberPatternService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.CardPanCacheRepository;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import com.sample.system.card.service.domain.valueObject.CardNumberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LuhnCardNumberGeneratorServiceImpl implements CardNumberGeneratorService {

    private final CardNumberPatternService patternService;
    private final CardPanCacheRepository cache;

    private static final int MAX_ATTEMPTS = 5;

    @Override
    @Transactional
    public String process(CardNumberDto dto) throws CardDomainException{
        CardProfile profile = dto.getCardProfile();
        log.info("Processing Luhn Algorithm for generating unique Pan for profileId : {}",
                profile.getId().getValue());
        String bin = profile.getBank().getBinCode();
        if (bin == null || !bin.matches("\\d{6}"))
            throw new CardDomainException("Invalid BIN");
        CardNumberPattern patt = patternService.getActivePatternForProfile(profile);
        log.info("pattern has founded for profile with Id : {}",patt.getId().getValue());
        String prod = validateProductCode(patt.getProductCode());
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            String seq = patternService.getNextAndIncrement(patt);
            String pan = generatePAN(bin, prod, seq);
            log.info("check pan: {} exist in cache or not", PanMaskingUtil.maskPan(pan));
            if (!cache.exists(pan)) {
                log.info("Generated PAN {} at attempt {}", PanMaskingUtil.maskPan(pan), i);
                patternService.save(patt);
                return pan;
            }
            log.warn("Duplicate PAN {}. Retrying…", PanMaskingUtil.maskPan(pan));
        }
        throw new CardDomainException(
                "Failed to generate unique PAN after retries",
                StatusService.INVALID_CARD_NUMBER,
                HttpStatus.BAD_REQUEST
        );
    }

    private String validateProductCode(String pc) throws CardDomainException{
        if (pc == null || !pc.matches("\\d{2}"))
            throw new CardDomainException("Invalid ProductCode");
        return pc;
    }

    private String generatePAN(String bin, String prod, String seq) {
        String prefix = bin + prod + seq;
        return prefix + luhn(prefix);
    }

    private char luhn(String s) {
        int sum = 0; boolean alt = true;

        for (int i = s.length() - 1; i >= 0; i--) {
            int d = s.charAt(i) - '0';
            if (alt) {
                d *= 2;
                if (d > 9) d = d - 9;
            }
            sum += d;
            alt = !alt;
        }

        return (char) ('0' + ((10 - (sum % 10)) % 10));
    }
}
