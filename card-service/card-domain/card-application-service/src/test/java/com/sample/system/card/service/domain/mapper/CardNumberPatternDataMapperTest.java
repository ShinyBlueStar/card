package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.numberPattern.CreateCardNumberPatternCommand;
import com.sample.system.card.service.domain.command.numberPattern.DeactivateCardNumberPatternCommand;
import com.sample.system.card.service.domain.command.numberPattern.UpdateCardNumberPatternCommand;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.response.number.*;
import com.sample.system.card.service.domain.valueObject.CardNumberPatternId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberPatternDataMapperTest {

    private final CardNumberPatternDataMapper mapper = new CardNumberPatternDataMapper();

    private static CardNumberPattern pattern(Long id) {
        CardNumberPattern pattern = CardNumberPattern.builder().cardNumberFrom("1000000").cardNumberTo("1999999").build();
        if (id != null) {
            pattern.setId(new CardNumberPatternId(id));
        }
        return pattern;
    }

    @Test
    void activeCreateCommandIsMappedToActivePattern() {
        CardNumberPattern pattern = mapper.createCommandToPattern(CreateCardNumberPatternCommand.builder()
                .cardNumberFrom("1000000").cardNumberTo("1999999").productCode("P1").name("n").status(1)
                .cardNumGenerationMethod(2).build());

        assertThat(pattern.getCardNumberFrom()).isEqualTo("1000000");
        assertThat(pattern.getCardNumberTo()).isEqualTo("1999999");
        assertThat(pattern.getProductCode()).isEqualTo("P1");
        assertThat(pattern.getName()).isEqualTo("n");
        assertThat(pattern.getPatternStatus()).isEqualTo(CardNumberPatternStatus.ACTIVE);
        assertThat(pattern.getCardNumGenerationMethod()).isEqualTo(CardNumGenerationMethod.RANGE_BASED);
    }

    @Test
    void inactiveOrMissingStatusAndMethodAreMapped() {
        CardNumberPattern inactive = mapper.createCommandToPattern(CreateCardNumberPatternCommand.builder().status(2).build());
        CardNumberPattern missing = mapper.createCommandToPattern(CreateCardNumberPatternCommand.builder().build());

        assertThat(inactive.getPatternStatus()).isEqualTo(CardNumberPatternStatus.INACTIVE);
        assertThat(missing.getPatternStatus()).isNull();
        assertThat(missing.getCardNumGenerationMethod()).isNull();
    }

    @Test
    void updateCommandIsMappedWithAndWithoutIdAndStatus() {
        CardNumberPattern active = mapper.updateCommandToPattern(UpdateCardNumberPatternCommand.builder()
                .patternId(4L).cardNumberFrom("1").cardNumberTo("2").name("n").status(1).build());
        CardNumberPattern inactive = mapper.updateCommandToPattern(UpdateCardNumberPatternCommand.builder().status(2).build());
        CardNumberPattern blank = mapper.updateCommandToPattern(UpdateCardNumberPatternCommand.builder().build());

        assertThat(active.getId().getValue()).isEqualTo(4L);
        assertThat(active.getPatternStatus()).isEqualTo(CardNumberPatternStatus.ACTIVE);
        assertThat(active.getName()).isEqualTo("n");
        assertThat(inactive.getId()).isNull();
        assertThat(inactive.getPatternStatus()).isEqualTo(CardNumberPatternStatus.INACTIVE);
        assertThat(blank.getPatternStatus()).isNull();
    }

    @Test
    void deactivateCommandCarriesTheId() {
        assertThat(mapper.deactivateCommandToPattern(DeactivateCardNumberPatternCommand.builder().patternId(3L).build())
                .getId().getValue()).isEqualTo(3L);
        assertThat(mapper.deactivateCommandToPattern(DeactivateCardNumberPatternCommand.builder().build()).getId()).isNull();
    }

    @Test
    void responsesCarryTheIdAndMessage() {
        CreateCardNumberPatternResponse created = mapper.patternToCreateResponse(pattern(5L), "ok");
        UpdateCardNumberPatternResponse updated = mapper.patternToUpdateResponse(pattern(6L), "done");

        assertThat(created.getCardNumberPatternId()).isEqualTo(5L);
        assertThat(created.getMessage()).isEqualTo("ok");
        assertThat(updated.getCardNumberPatternId()).isEqualTo(6L);
        assertThat(updated.getMessage()).isEqualTo("done");
        assertThat(mapper.patternToCreateResponse(pattern(null), "m").getCardNumberPatternId()).isNull();
        assertThat(mapper.patternToUpdateResponse(pattern(null), "m").getCardNumberPatternId()).isNull();
    }

    @Test
    void patternIsMappedToGetResponseWithEverythingSet() {
        CardNumberPattern pattern = pattern(7L);
        CardProfile profile = CardProfile.builder().build();
        profile.setId(new CardProfileId(11L));
        pattern.setCardProfile(profile);
        pattern.setProductCode("P1");
        pattern.setName("n");
        pattern.setCardNumGenerationMethod(CardNumGenerationMethod.INCREMENTAL);
        pattern.setPatternStatus(CardNumberPatternStatus.ACTIVE);
        pattern.setFirstCardNumber("0000000");
        pattern.setLastCardNumber("0000003");
        pattern.setNextCardNumber("0000004");
        pattern.setCreatedBy("ali");
        pattern.setCreatedDate(Timestamp.valueOf("2024-03-20 10:15:30"));
        pattern.setLastModifiedBy("reza");
        pattern.setLastModifiedDate(Timestamp.valueOf("2024-04-21 11:00:00"));

        GetCardNumberPatternResponse response = mapper.patternToGetResponse(pattern);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.cardProfileId()).isEqualTo(11L);
        assertThat(response.cardNumGenerationMethod()).isEqualTo(CardNumGenerationMethod.INCREMENTAL.getDescription());
        assertThat(response.patternStatus()).isEqualTo(CardNumberPatternStatus.ACTIVE.getDescription());
        assertThat(response.createdDate()).isNotBlank();
        assertThat(response.lastModifiedDate()).isNotBlank();
        assertThat(response.nextCardNumber()).isEqualTo("0000004");
        assertThat(response.message()).isNotBlank();
    }

    @Test
    void profileIdFallsBackToTheProfileIdFieldOrNull() {
        CardNumberPattern viaId = pattern(1L);
        viaId.setCardProfileId(new CardProfileId(22L));
        CardNumberPattern profileWithoutId = pattern(2L);
        profileWithoutId.setCardProfile(CardProfile.builder().build());
        CardNumberPattern none = pattern(null);

        assertThat(mapper.patternToGetResponse(viaId).cardProfileId()).isEqualTo(22L);
        assertThat(mapper.patternToGetResponse(profileWithoutId).cardProfileId()).isNull();
        GetCardNumberPatternResponse empty = mapper.patternToGetResponse(none);
        assertThat(empty.id()).isNull();
        assertThat(empty.cardProfileId()).isNull();
        assertThat(empty.cardNumGenerationMethod()).isNull();
        assertThat(empty.patternStatus()).isNull();
        assertThat(empty.createdDate()).isNull();
    }

    @Test
    void listsAndPagesAreMapped() {
        assertThat(mapper.patternsToGetResponses(List.of(pattern(1L), pattern(2L)))).hasSize(2);

        CardNumberPatternListResponse page = mapper.patternsToGetListResponse(
                new PageImpl<>(List.of(pattern(1L)), PageRequest.of(1, 1), 3));
        assertThat(page.getList()).hasSize(1);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(mapper.patternsToGetListResponse(null)).isNull();
    }

    @Test
    void rangeAndAvailabilityHelpers() {
        CardNumberPattern pattern = pattern(1L);

        assertThat(mapper.isCardNumberInRange(pattern, "1500000")).isTrue();
        assertThat(mapper.isCardNumberInRange(pattern, "2500000")).isFalse();
        assertThat(mapper.isCardNumberInRange(null, "1500000")).isFalse();
        assertThat(mapper.isPatternAvailable(pattern)).isTrue();
        assertThat(mapper.isPatternAvailable(null)).isFalse();
    }
}
