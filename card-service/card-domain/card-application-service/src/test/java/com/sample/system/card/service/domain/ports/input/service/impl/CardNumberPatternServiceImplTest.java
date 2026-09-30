package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.CardDomainService;
import com.sample.system.card.service.domain.command.numberPattern.GetAllCardNumberPatternsQuery;
import com.sample.system.card.service.domain.command.numberPattern.GetCardNumberPatternQuery;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.CardNumberPatternRepository;
import com.sample.system.card.service.domain.valueObject.CardNumberPatternId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardNumberPatternServiceImplTest {

    @Mock
    private CardNumberPatternRepository repository;
    @Mock
    private CardDomainService cardDomainService;

    private CardNumberPatternServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardNumberPatternServiceImpl(repository, cardDomainService);
    }

    private static CardNumberPattern pattern(Long id) {
        CardNumberPattern pattern = CardNumberPattern.builder().cardNumberFrom("1000000").cardNumberTo("1999999")
                .productCode("P1").name("n").cardNumGenerationMethod(CardNumGenerationMethod.RANGE_BASED).build();
        if (id != null) {
            pattern.setId(new CardNumberPatternId(id));
        }
        return pattern;
    }

    private void assertDomainError(Throwable t, int status, HttpStatus http) {
        assertThat(t).isInstanceOfSatisfying(CardDomainException.class, e -> {
            assertThat(e.getStatus()).isEqualTo(status);
            assertThat(e.getHttpStatus()).isEqualTo(http);
        });
    }

    // ------------------------------------------------------------ create

    @Test
    void createRejectsPatternThatAlreadyBelongsToAProfile() {
        CardNumberPattern withProfile = pattern(null);
        CardProfile profile = CardProfile.builder().build();
        profile.setId(new CardProfileId(1L));
        withProfile.setCardProfile(profile);
        CardNumberPattern withProfileId = pattern(null);
        withProfileId.setCardProfileId(new CardProfileId(2L));

        assertThatThrownBy(() -> service.createCardNumberPattern(withProfile))
                .satisfies(t -> assertDomainError(t, StatusService.DUPLICATE_PATTERN, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.createCardNumberPattern(withProfileId))
                .satisfies(t -> assertDomainError(t, StatusService.DUPLICATE_PATTERN, HttpStatus.BAD_REQUEST));
    }

    @Test
    void createRejectsConflictingGenerationMethodForTheSameProductCode() {
        CardNumberPattern existing = pattern(1L);
        existing.setCardNumGenerationMethod(CardNumGenerationMethod.INCREMENTAL);
        when(repository.findByProductCode("P1")).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.createCardNumberPattern(pattern(null)))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
    }

    @Test
    void createRejectsSameGenerationMethodForTheSameProductCode() {
        when(repository.findByProductCode("P1")).thenReturn(List.of(pattern(1L)));

        assertThatThrownBy(() -> service.createCardNumberPattern(pattern(null)))
                .satisfies(t -> assertDomainError(t, StatusService.DUPLICATE_PATTERN, HttpStatus.BAD_REQUEST));
    }

    @Test
    void createIgnoresExistingPatternsWithoutGenerationMethodOrWhenNewOneHasNone() throws CardDomainException {
        CardNumberPattern legacy = pattern(1L);
        legacy.setCardNumGenerationMethod(null);
        CardNumberPattern methodless = pattern(null);
        methodless.setCardNumGenerationMethod(null);
        when(repository.findByProductCode("P1")).thenReturn(List.of(legacy));
        when(repository.findOverlappingPatternsByProductCode(null, "P1", "1000000", "1999999")).thenReturn(null);
        when(repository.save(any(CardNumberPattern.class))).thenAnswer(i -> {
            CardNumberPattern saved = i.getArgument(0);
            saved.setId(new CardNumberPatternId(9L));
            return saved;
        });

        // existing pattern has no method -> nothing to compare against
        assertThat(service.createCardNumberPattern(pattern(null)).getCardNumberPattern().getPatternStatus())
                .isEqualTo(CardNumberPatternStatus.ACTIVE);
        // the new pattern has no method -> comparison is skipped
        assertThat(service.createCardNumberPattern(methodless).getCardNumberPattern().getPatternStatus())
                .isEqualTo(CardNumberPatternStatus.ACTIVE);
    }

    @Test
    void createAllowsSameGenerationMethodWhenTheExistingPatternHasAnotherProductCode() throws CardDomainException {
        CardNumberPattern other = pattern(1L);
        other.setProductCode("OTHER");
        when(repository.findByProductCode("P1")).thenReturn(List.of(other));
        when(repository.findOverlappingPatternsByProductCode(null, "P1", "1000000", "1999999")).thenReturn(List.of());
        when(repository.save(any(CardNumberPattern.class))).thenAnswer(i -> {
            CardNumberPattern saved = i.getArgument(0);
            saved.setId(new CardNumberPatternId(9L));
            return saved;
        });

        assertThat(service.createCardNumberPattern(pattern(null)).getCardNumberPattern().getId().getValue()).isEqualTo(9L);
    }

    @Test
    void createRejectsOverlappingRanges() {
        when(repository.findByProductCode("P1")).thenReturn(List.of());
        when(repository.findOverlappingPatternsByProductCode(null, "P1", "1000000", "1999999"))
                .thenReturn(List.of(pattern(5L)));

        assertThatThrownBy(() -> service.createCardNumberPattern(pattern(null)))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        verifyNoInteractions(cardDomainService);
    }

    @Test
    void createSavesAnActivatedPattern() throws CardDomainException {
        when(repository.findByProductCode("P1")).thenReturn(List.of());
        when(repository.findOverlappingPatternsByProductCode(null, "P1", "1000000", "1999999")).thenReturn(List.of());
        when(repository.save(any(CardNumberPattern.class))).thenAnswer(i -> {
            CardNumberPattern saved = i.getArgument(0);
            saved.setId(new CardNumberPatternId(9L));
            return saved;
        });
        CardNumberPattern input = pattern(null);

        var event = service.createCardNumberPattern(input);

        verify(cardDomainService).validateAndInitiateCardNumberPattern(input);
        assertThat(event.getCardNumberPattern().getId().getValue()).isEqualTo(9L);
        assertThat(event.getCardNumberPattern().getPatternStatus()).isEqualTo(CardNumberPatternStatus.ACTIVE);
    }

    @Test
    void createFailsWhenRepositoryReturnsNull() {
        when(repository.findByProductCode("P1")).thenReturn(List.of());
        when(repository.findOverlappingPatternsByProductCode(null, "P1", "1000000", "1999999")).thenReturn(List.of());
        when(repository.save(any(CardNumberPattern.class))).thenReturn(null);

        assertThatThrownBy(() -> service.createCardNumberPattern(pattern(null)))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void createWrapsUnexpectedErrors() {
        when(repository.findByProductCode("P1")).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.createCardNumberPattern(pattern(null)))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
                .hasMessageContaining("db down");
    }

    // ------------------------------------------------------------ update / deactivate

    @Test
    void updateRequiresAnId() {
        assertThatThrownBy(() -> service.updateCardNumberPattern(pattern(null)))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
    }

    @Test
    void updateOfUnknownPatternIsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCardNumberPattern(pattern(1L)))
                .satisfies(t -> assertDomainError(t, StatusService.CARD_NUMBER_PATTERN_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Test
    void updateCopiesEveryProvidedField() throws CardDomainException {
        CardNumberPattern current = pattern(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(current));
        when(repository.save(current)).thenReturn(current);
        CardNumberPattern changes = CardNumberPattern.builder().cardNumberFrom("2000000").cardNumberTo("2999999")
                .name("renamed").patternStatus(CardNumberPatternStatus.INACTIVE)
                .cardProfile(CardProfile.builder().profileName("Gold").build()).build();
        changes.setId(new CardNumberPatternId(1L));

        var event = service.updateCardNumberPattern(changes);

        CardNumberPattern updated = event.getCardNumberPattern();
        assertThat(updated.getCardNumberFrom()).isEqualTo("2000000");
        assertThat(updated.getCardNumberTo()).isEqualTo("2999999");
        assertThat(updated.getName()).isEqualTo("renamed");
        assertThat(updated.getPatternStatus()).isEqualTo(CardNumberPatternStatus.INACTIVE);
        assertThat(updated.getCardProfile().getProfileName()).isEqualTo("Gold");
    }

    @Test
    void updateKeepsFieldsThatAreNotProvided() throws CardDomainException {
        CardNumberPattern current = pattern(1L);
        current.setPatternStatus(CardNumberPatternStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(current));
        when(repository.save(current)).thenReturn(current);
        CardNumberPattern changes = CardNumberPattern.builder().build();
        changes.setId(new CardNumberPatternId(1L));

        CardNumberPattern updated = service.updateCardNumberPattern(changes).getCardNumberPattern();

        assertThat(updated.getCardNumberFrom()).isEqualTo("1000000");
        assertThat(updated.getName()).isEqualTo("n");
        assertThat(updated.getPatternStatus()).isEqualTo(CardNumberPatternStatus.ACTIVE);
        assertThat(updated.getCardProfile()).isNull();
    }

    @Test
    void updateRethrowsRepositoryFailures() {
        when(repository.findById(1L)).thenThrow(new IllegalStateException("db"));

        assertThatThrownBy(() -> service.updateCardNumberPattern(pattern(1L))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deactivateMarksPatternInactive() throws CardDomainException {
        CardNumberPattern current = pattern(1L);
        current.setPatternStatus(CardNumberPatternStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(current));
        when(repository.save(current)).thenReturn(current);

        assertThat(service.deactivateCardNumberPattern(pattern(1L)).getCardNumberPattern().getPatternStatus())
                .isEqualTo(CardNumberPatternStatus.INACTIVE);
    }

    @Test
    void deactivateValidatesInputAndRethrowsFailures() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        when(repository.findById(3L)).thenThrow(new IllegalStateException("db"));

        assertThatThrownBy(() -> service.deactivateCardNumberPattern(pattern(null)))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.deactivateCardNumberPattern(pattern(2L)))
                .satisfies(t -> assertDomainError(t, StatusService.CARD_NUMBER_PATTERN_NOT_FOUND, HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> service.deactivateCardNumberPattern(pattern(3L))).isInstanceOf(IllegalStateException.class);
    }

    // ------------------------------------------------------------ queries

    @Test
    void getReturnsPatternOrBadRequest() throws CardDomainException {
        CardNumberPattern found = pattern(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(found));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(service.getCardNumberPattern(new GetCardNumberPatternQuery(1L))).isSameAs(found);
        assertThatThrownBy(() -> service.getCardNumberPattern(new GetCardNumberPatternQuery(2L)))
                .satisfies(t -> assertDomainError(t, StatusService.CARD_NUMBER_PATTERN_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }

    @Test
    void getAllBuildsSearchCriteriaFromTheQuery() throws CardDomainException {
        when(repository.findAll(anyMap())).thenReturn(new PageImpl<>(List.of(pattern(1L))));

        var result = service.getAllCardNumberPatterns(new GetAllCardNumberPatternsQuery("ACTIVE", 7L, null, null, null));

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).findAll(captor.capture());
        assertThat(captor.getValue()).containsEntry("status", "ACTIVE").containsEntry("cardProfileId", "7")
                .containsEntry("size", "10000");
        assertThat(result).hasSize(1);
    }

    @Test
    void getAllSkipsEmptyCriteriaAndRethrowsFailures() throws CardDomainException {
        when(repository.findAll(anyMap())).thenReturn(new PageImpl<>(List.of())).thenThrow(new IllegalStateException("db"));

        service.getAllCardNumberPatterns(new GetAllCardNumberPatternsQuery("", null, null, null, null));
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).findAll(captor.capture());
        assertThat(captor.getValue()).containsOnlyKeys("size");

        assertThatThrownBy(() -> service.getAllCardNumberPatterns(new GetAllCardNumberPatternsQuery(null, null, null, null, null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void listDelegatesAndRethrowsFailures() throws CardDomainException {
        Map<String, String> params = Map.of("a", "b");
        Page<CardNumberPattern> page = new PageImpl<>(List.of(pattern(1L)));
        when(repository.findAll(params)).thenReturn(page).thenThrow(new IllegalStateException("db"));

        assertThat(service.listCardNumberPatterns(params, "me", "ip")).isSameAs(page);
        assertThatThrownBy(() -> service.listCardNumberPatterns(params, "me", "ip")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void patternsContainingACardNumberAreFiltered() throws CardDomainException {
        CardNumberPattern inRange = pattern(1L);
        CardNumberPattern outOfRange = pattern(2L);
        outOfRange.setCardNumberFrom("5000000");
        outOfRange.setCardNumberTo("5999999");
        when(repository.findAll(anyMap())).thenReturn(new PageImpl<>(List.of(inRange, outOfRange)))
                .thenThrow(new IllegalStateException("db"));

        assertThat(service.getPatternsContainingCardNumber("1500000")).containsExactly(inRange);
        assertThatThrownBy(() -> service.getPatternsContainingCardNumber("1500000")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bulkCreateValidatesEachPatternThenSavesAll() throws CardDomainException {
        List<CardNumberPattern> patterns = List.of(pattern(null), pattern(null));
        when(repository.saveAll(patterns)).thenReturn(patterns).thenThrow(new IllegalStateException("db"));

        assertThat(service.createCardNumberPatterns(patterns).getCardNumberPatterns()).hasSize(2);
        verify(cardDomainService, times(2)).validateAndInitiateCardNumberPattern(any());
        assertThatThrownBy(() -> service.createCardNumberPatterns(patterns)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void existByStatusChecksThePatternStatus() {
        CardNumberPattern assigned = pattern(1L);
        assigned.setPatternStatus(CardNumberPatternStatus.ASSIGN);
        when(repository.findById(1L)).thenReturn(Optional.of(assigned));
        when(repository.findById(2L)).thenReturn(Optional.empty());
        when(repository.findById(3L)).thenThrow(new IllegalStateException("db"));

        assertThat(service.existByStatus(null, CardNumberPatternStatus.ASSIGN)).isFalse();
        assertThat(service.existByStatus(1L, null)).isFalse();
        assertThat(service.existByStatus(1L, CardNumberPatternStatus.ASSIGN)).isTrue();
        assertThat(service.existByStatus(1L, CardNumberPatternStatus.ACTIVE)).isFalse();
        assertThat(service.existByStatus(2L, CardNumberPatternStatus.ASSIGN)).isFalse();
        assertThat(service.existByStatus(3L, CardNumberPatternStatus.ASSIGN)).isFalse();
    }

    @Test
    void saveValidatesInputAndWrapsFailures() throws CardDomainException {
        CardNumberPattern pattern = pattern(1L);
        when(repository.save(pattern)).thenReturn(pattern).thenThrow(new IllegalStateException("db"));

        assertThatThrownBy(() -> service.save(null))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThat(service.save(pattern)).isSameAs(pattern);
        assertThatThrownBy(() -> service.save(pattern))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void findByProfileValidatesInputAndWrapsFailures() throws CardDomainException {
        when(repository.findByCardProfileId(1L)).thenReturn(List.of(pattern(1L)));
        when(repository.findByCardProfileId(2L)).thenThrow(new IllegalStateException("db"));

        assertThatThrownBy(() -> service.findByCardProfileId(null))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThat(service.findByCardProfileId(1L)).hasSize(1);
        assertThatThrownBy(() -> service.findByCardProfileId(2L))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void findAllVariantsDelegateAndWrapFailures() throws CardDomainException {
        Map<String, String> criteria = new HashMap<>();
        Page<CardNumberPattern> page = new PageImpl<>(List.of(pattern(1L)));
        when(repository.findAll()).thenReturn(List.of(pattern(1L))).thenThrow(new IllegalStateException("db"));
        when(repository.findAll(criteria)).thenReturn(page).thenThrow(new IllegalStateException("db"));

        assertThat(service.findAll()).hasSize(1);
        assertThatThrownBy(() -> service.findAll())
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(service.findAll(criteria)).isSameAs(page);
        assertThatThrownBy(() -> service.findAll(criteria))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // ------------------------------------------------------------ profile patterns and number generation

    @Test
    void activePatternForProfileIsTheAssignedOne() throws CardDomainException {
        CardNumberPattern inactive = pattern(1L);
        inactive.setPatternStatus(CardNumberPatternStatus.INACTIVE);
        CardNumberPattern assigned = pattern(2L);
        assigned.setPatternStatus(CardNumberPatternStatus.ASSIGN);
        CardProfile withAssigned = CardProfile.builder().cardNumberRangeList(List.of(inactive, assigned)).build();
        CardProfile withoutAssigned = CardProfile.builder().cardNumberRangeList(List.of(inactive)).build();

        assertThat(service.getActivePatternForProfile(withAssigned)).isSameAs(assigned);
        assertThatThrownBy(() -> service.getActivePatternForProfile(withoutAssigned))
                .satisfies(t -> assertDomainError(t, StatusService.ASSIGN_PATTERN_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST));
    }

    @Test
    void rangeBasedNumbersStartAtTheRangeStartAndIncrease() throws CardDomainException {
        CardNumberPattern pattern = pattern(1L);

        assertThat(service.getNextAndIncrement(pattern)).isEqualTo("1000000");
        assertThat(pattern.getFirstCardNumber()).isEqualTo("1000000");
        assertThat(service.getNextAndIncrement(pattern)).isEqualTo("1000001");
        assertThat(pattern.getLastCardNumber()).isEqualTo("1000001");
        assertThat(service.getNextAndIncrement(pattern)).isEqualTo("1000002");
    }

    @Test
    void rangeBasedPatternIsMarkedExhaustedPastTheEnd() {
        CardNumberPattern pattern = pattern(1L);
        pattern.setCardNumberFrom("0000005");
        pattern.setCardNumberTo("0000005");
        pattern.setFirstCardNumber("5");
        when(repository.save(pattern)).thenReturn(pattern);

        assertThatThrownBy(() -> service.getNextAndIncrement(pattern))
                .satisfies(t -> assertDomainError(t, StatusService.CARD_NUMBER_PATTERN_EXHAUSTED, HttpStatus.BAD_REQUEST));
        assertThat(pattern.getPatternStatus()).isEqualTo(CardNumberPatternStatus.EXHAUSTED);
        verify(repository).save(pattern);
    }

    @Test
    void incrementalNumbersStartAtZeroAndIncrease() throws CardDomainException {
        CardNumberPattern pattern = pattern(1L);
        pattern.setCardNumGenerationMethod(CardNumGenerationMethod.INCREMENTAL);

        assertThat(service.getNextAndIncrement(pattern)).isEqualTo("0000000");
        assertThat(pattern.getFirstCardNumber()).isEqualTo("0000000");
        assertThat(service.getNextAndIncrement(pattern)).isEqualTo("0000001");
        assertThat(service.getNextAndIncrement(pattern)).isEqualTo("0000002");
        assertThat(pattern.getLastCardNumber()).isEqualTo("2");
    }

    @Test
    void incrementalPatternIsMarkedExhaustedAfterTheLastNumber() {
        CardNumberPattern pattern = pattern(1L);
        pattern.setCardNumGenerationMethod(CardNumGenerationMethod.INCREMENTAL);
        pattern.setFirstCardNumber("0000000");
        pattern.setLastCardNumber("9999999");
        when(repository.save(pattern)).thenReturn(pattern);

        assertThatThrownBy(() -> service.getNextAndIncrement(pattern))
                .satisfies(t -> assertDomainError(t, StatusService.CARD_NUMBER_PATTERN_EXHAUSTED, HttpStatus.BAD_REQUEST));
        assertThat(pattern.getPatternStatus()).isEqualTo(CardNumberPatternStatus.EXHAUSTED);
    }

    @Test
    void rangeBasedNumberContinuesFromFirstWhenLastIsMissingAndFromLastOtherwise() throws CardDomainException {
        CardNumberPattern onlyFirst = pattern(1L);
        onlyFirst.setFirstCardNumber("1000000");
        CardNumberPattern withLast = pattern(2L);
        withLast.setFirstCardNumber("1000000");
        withLast.setLastCardNumber("1000010");
        CardNumberPattern incrementalOnlyFirst = pattern(3L);
        incrementalOnlyFirst.setCardNumGenerationMethod(CardNumGenerationMethod.INCREMENTAL);
        incrementalOnlyFirst.setFirstCardNumber("0000004");

        assertThat(service.getNextAndIncrement(onlyFirst)).isEqualTo("1000001");
        assertThat(service.getNextAndIncrement(withLast)).isEqualTo("1000011");
        assertThat(service.getNextAndIncrement(incrementalOnlyFirst)).isEqualTo("0000005");
    }
}
