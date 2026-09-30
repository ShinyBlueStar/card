package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.numberPattern.GetCardNumberPatternQuery;
import com.sample.system.card.service.domain.command.cardProfile.GetAllCardProfilesQuery;
import com.sample.system.card.service.domain.command.cardProfile.GetCardProfileQuery;
import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.*;
import com.sample.system.card.service.domain.ports.output.repository.*;
import com.sample.system.card.service.domain.valueObject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardProfileServiceImplTest {

    @Mock
    private CardTypeCategoryRepository cardTypeCategoryRepository;
    @Mock
    private CardProfileRepository repository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardCategoryService cardCategoryService;
    @Mock
    private CardTypeService cardTypeService;
    @Mock
    private BankService bankService;
    @Mock
    private FeeProfileRepository feeProfileRepository;
    @Mock
    private CardNumberPatternService patternService;

    private CardProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardProfileServiceImpl(cardTypeCategoryRepository, repository, cardRepository, cardCategoryService,
                cardTypeService, bankService, feeProfileRepository, patternService);
    }

    // ---------------------------------------------------------------- fixtures

    private static Bank bank(boolean active) {
        Bank bank = Bank.builder().binCode("603799").name("Melli").isActive(active).build();
        bank.setId(new BankId(1L));
        return bank;
    }

    private static CardCategory category(String name, boolean active) {
        return new CardCategory(new CardCategoryId(2L), 1, name, "d", active);
    }

    private static CardType type(String name, boolean active) {
        return new CardType(new CardTypeId(3L), 1, name, "d", active);
    }

    private static CardTypeCategory typeCategory(String categoryName, String typeName, boolean active) {
        CardTypeCategory relation = CardTypeCategory.builder()
                .cardCategory(category(categoryName, true)).cardType(type(typeName, true)).isActive(active).build();
        relation.setId(new CardTypeCategoryId(4L));
        return relation;
    }

    private static FeeProfile feeProfile(boolean active) {
        FeeProfile fee = new FeeProfile();
        fee.setId(new FeeProfileId(5L));
        fee.setName("Basic");
        fee.setIsActive(active);
        return fee;
    }

    private static CardNumberPattern pattern(Long id, CardNumberPatternStatus status) {
        CardNumberPattern pattern = CardNumberPattern.builder().patternStatus(status).build();
        if (id != null) {
            pattern.setId(new CardNumberPatternId(id));
        }
        return pattern;
    }

    /** A profile request that passes every creation rule for a DEBIT category. */
    private static CardProfile.CardProfileBuilder debitRequest() {
        return CardProfile.builder()
                .profileName("Gold")
                .issuingBankId(1L)
                .cardCategoryId(2L)
                .cardTypeId(3L)
                .salesMethod(SalesMethod.DEFAULT)
                .reloadable(Reloadable.DEFAULT)
                .cardIssueMethod(CardIssueMethod.PHYSICAL);
    }

    private static CardProfile existingProfile(long id) {
        CardProfile profile = CardProfile.builder().profileName("Gold").issuingBankId(1L).cardCategoryId(2L)
                .cardTypeId(3L).feeProfileIdLong(5L).isActive(true).build();
        profile.setId(new CardProfileId(id));
        return profile;
    }

    private static CardProfile updateRequest(long id) {
        CardProfile request = CardProfile.builder().build();
        request.setId(new CardProfileId(id));
        return request;
    }

    private void stubReferenceLookups(String categoryName, String typeName) throws CardDomainException {
        when(bankService.findById(1L)).thenReturn(bank(true));
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L))
                .thenReturn(Optional.of(typeCategory(categoryName, typeName, true)));
    }

    private void assertDomainError(Throwable t, int status) {
        assertThat(t).isInstanceOfSatisfying(CardDomainException.class, e -> {
            assertThat(e.getStatus()).isEqualTo(status);
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        });
    }

    // ---------------------------------------------------------------- create

    @Test
    void createRejectsDuplicateProfileName() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(true);

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().build()))
                .satisfies(t -> assertDomainError(t, StatusService.DUPLICATE_PROFILE_NAME_FOUND));
    }

    @Test
    void createRequiresCategoryAndTypeTogether() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().cardTypeId(null).build()))
                .isInstanceOf(CardDomainException.class);
        assertThatThrownBy(() -> service.createCardProfile(debitRequest().cardCategoryId(null).build()))
                .isInstanceOf(CardDomainException.class);
    }

    @Test
    void createFailsWhenTypeCategoryRelationIsMissing() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        when(bankService.findById(1L)).thenReturn(bank(true));
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().build()))
                .satisfies(t -> assertDomainError(t, StatusService.CARDTYPECATEGORY_NOT_FOUND));
        verify(cardCategoryService).validateCardCategoryExists(2L);
        verify(cardTypeService).validateCardTypeExists(3L);
    }

    @Test
    void createFailsWhenFeeProfileDoesNotExist() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("DEBIT", "NONE");
        when(feeProfileRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCardProfile(
                debitRequest().feeProfileId(new FeeProfileId(5L)).feeProfileIdLong(5L).build()))
                .satisfies(t -> assertDomainError(t, StatusService.FEE_PROFILE_NOT_FOUND));
    }

    @Test
    void createForDebitAndCreditRequiresDefaultSalesMethodAndReloadable() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        when(bankService.findById(1L)).thenReturn(bank(true));
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L))
                .thenReturn(Optional.of(typeCategory("credit", "NONE", true)));

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().salesMethod(SalesMethod.BRANCH).build()))
                .satisfies(t -> assertDomainError(t, StatusService.INVALID_SALES_METHOD));
        assertThatThrownBy(() -> service.createCardProfile(debitRequest().reloadable(Reloadable.RELOADABLE).build()))
                .satisfies(t -> assertDomainError(t, StatusService.INVALID_RELOADABLE_METHOD));
    }

    @Test
    void createForPrepaidRequiresExplicitSalesMethodReloadableAndMaximumAmount() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        when(bankService.findById(1L)).thenReturn(bank(true));
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L))
                .thenReturn(Optional.of(typeCategory("PREPAID", "NONE", true)));

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().build()))
                .satisfies(t -> assertDomainError(t, StatusService.INVALID_SALES_METHOD));
        assertThatThrownBy(() -> service.createCardProfile(debitRequest().salesMethod(SalesMethod.BRANCH).build()))
                .satisfies(t -> assertDomainError(t, StatusService.INVALID_RELOADABLE_METHOD));
        assertThatThrownBy(() -> service.createCardProfile(debitRequest()
                .salesMethod(SalesMethod.BRANCH).reloadable(Reloadable.RELOADABLE).build()))
                .satisfies(t -> assertDomainError(t, StatusService.MAXIMUM_ALLOWED_AMOUNT_SHOULD_NOT_BE_NULL));
    }

    @Test
    void createVirtualCardRequiresCardTypeNone() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        when(bankService.findById(1L)).thenReturn(bank(true));
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L))
                .thenReturn(Optional.of(typeCategory("DEBIT", "GOLD", true)));

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().cardIssueMethod(CardIssueMethod.VIRTUAL).build()))
                .satisfies(t -> assertDomainError(t, StatusService.INVALID_RELATION_BETWEEN_ISSUE_METHOD_AND_CARD_TYPE));
    }

    @Test
    void createRejectsPatternThatIsNotActive() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("DEBIT", "NONE");
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class)))
                .thenReturn(pattern(9L, CardNumberPatternStatus.ASSIGN));

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().NumPatternId(9L).build()))
                .satisfies(t -> assertDomainError(t, StatusService.ACTIVE_PATTERN_DOES_NOT_EXIST));
    }

    @Test
    void createRejectsPatternWithoutId() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("DEBIT", "NONE");
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class)))
                .thenReturn(pattern(null, CardNumberPatternStatus.ACTIVE));

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().NumPatternId(9L).build()))
                .satisfies(t -> assertDomainError(t, StatusService.ACTIVE_PATTERN_DOES_NOT_EXIST));
    }

    @Test
    void createFailsWhenSaveReturnsNull() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("DEBIT", "NONE");
        when(repository.save(any(CardProfile.class))).thenReturn(null);

        assertThatThrownBy(() -> service.createCardProfile(debitRequest().build()))
                .isInstanceOfSatisfying(CardDomainException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(StatusService.CARD_PROFILE_NOT_FOUND);
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.OK);
                });
    }

    @Test
    void createSavesProfileWithResolvedReferences() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("DEBIT", "NONE");
        when(feeProfileRepository.findById(5L)).thenReturn(Optional.of(feeProfile(true)));
        when(repository.save(any(CardProfile.class))).thenAnswer(i -> {
            CardProfile saved = i.getArgument(0);
            saved.setId(new CardProfileId(10L));
            return saved;
        });

        var event = service.createCardProfile(debitRequest()
                .feeProfileId(new FeeProfileId(5L)).feeProfileIdLong(5L).cardIssueMethod(CardIssueMethod.VIRTUAL).build());

        CardProfile saved = event.getCardProfile();
        assertThat(saved.getId().getValue()).isEqualTo(10L);
        assertThat(saved.getBank().getName()).isEqualTo("Melli");
        assertThat(saved.getCardTypeCategory().getId().getValue()).isEqualTo(4L);
        assertThat(saved.getFeeProfile().getName()).isEqualTo("Basic");
        verifyNoInteractions(patternService);
    }

    @Test
    void createWithoutBankSkipsBankLookup() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L))
                .thenReturn(Optional.of(typeCategory("DEBIT", "NONE", true)));
        when(repository.save(any(CardProfile.class))).thenAnswer(i -> {
            CardProfile saved = i.getArgument(0);
            saved.setId(new CardProfileId(11L));
            return saved;
        });

        var event = service.createCardProfile(debitRequest().issuingBankId(null).build());

        assertThat(event.getCardProfile().getBank()).isNull();
        verifyNoInteractions(bankService);
    }

    @Test
    void createForPrepaidWithMaximumAmountSucceeds() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("PREPAID", "NONE");
        when(repository.save(any(CardProfile.class))).thenAnswer(i -> {
            CardProfile saved = i.getArgument(0);
            saved.setId(new CardProfileId(12L));
            return saved;
        });

        var event = service.createCardProfile(debitRequest().salesMethod(SalesMethod.BRANCH)
                .reloadable(Reloadable.RELOADABLE).maximumAllowedAmount(BigDecimal.TEN).build());

        assertThat(event.getCardProfile().getId().getValue()).isEqualTo(12L);
    }

    @Test
    void createAssignsTheRequestedPatternToTheNewProfile() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("DEBIT", "NONE");
        CardNumberPattern pattern = pattern(9L, CardNumberPatternStatus.ACTIVE);
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class))).thenReturn(pattern);
        CardProfile reloaded = existingProfile(10L);
        when(repository.save(any(CardProfile.class))).thenAnswer(i -> {
            CardProfile saved = i.getArgument(0);
            saved.setId(new CardProfileId(10L));
            return saved;
        });
        when(repository.findById(10L)).thenReturn(Optional.of(reloaded));

        var event = service.createCardProfile(debitRequest().NumPatternId(9L).build());

        assertThat(pattern.getPatternStatus()).isEqualTo(CardNumberPatternStatus.ASSIGN);
        assertThat(pattern.getCardProfileId().getValue()).isEqualTo(10L);
        verify(patternService).save(pattern);
        assertThat(event.getCardProfile()).isSameAs(reloaded);
    }

    @Test
    void createKeepsSavedProfileWhenReloadFindsNothing() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("DEBIT", "NONE");
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class)))
                .thenReturn(pattern(9L, CardNumberPatternStatus.ACTIVE));
        when(repository.save(any(CardProfile.class))).thenAnswer(i -> {
            CardProfile saved = i.getArgument(0);
            saved.setId(new CardProfileId(10L));
            return saved;
        });
        when(repository.findById(10L)).thenReturn(Optional.empty());

        var event = service.createCardProfile(debitRequest().NumPatternId(9L).build());

        assertThat(event.getCardProfile().getId().getValue()).isEqualTo(10L);
    }

    // ---------------------------------------------------------------- update: basic checks

    @Test
    void updateRequiresProfileId() {
        assertThatThrownBy(() -> service.updateCardProfile(CardProfile.builder().build()))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID));
    }

    @Test
    void updateOfUnknownProfileFails() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCardProfile(updateRequest(1L)))
                .satisfies(t -> assertDomainError(t, StatusService.CARD_PROFILE_NOT_FOUND));
    }

    @Test
    void updateRejectsRenamingToAnExistingName() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(repository.existsByProfileName("Silver")).thenReturn(true);
        CardProfile request = updateRequest(1L);
        request.setProfileName("Silver");

        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.DUPLICATE_PROFILE_NAME_FOUND));
    }

    @Test
    void updateDoesNotCheckDuplicateWhenNameIsBlankOrUnchanged() throws CardDomainException {
        CardProfile first = existingProfile(1L);
        CardProfile second = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(first)).thenReturn(Optional.of(second));
        when(cardRepository.existsByProfileId(1L)).thenReturn(false);
        when(repository.save(any(CardProfile.class))).thenAnswer(i -> i.getArgument(0));

        CardProfile blank = updateRequest(1L);
        blank.setProfileName(" ");
        service.updateCardProfile(blank);

        CardProfile same = updateRequest(1L);
        same.setProfileName("Gold");
        service.updateCardProfile(same);

        verify(repository, never()).existsByProfileName(any());
    }

    // ---------------------------------------------------------------- update: reference validation

    @Test
    void updateRejectsInactiveBank() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(bankService.findById(1L)).thenReturn(bank(false));
        CardProfile request = updateRequest(1L);
        request.setIssuingBankId(1L);

        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.ACTIVE_BANK_NOT_FOUND));
    }

    @Test
    void updateRejectsInactiveCategory() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(cardCategoryService.getCardCategory(any())).thenReturn(category("DEBIT", false));
        CardProfile request = updateRequest(1L);
        request.setCardCategoryId(2L);

        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.ACTIVE_CARD_CATEGORY_NOT_FOUND));
    }

    @Test
    void updateRejectsInactiveCardType() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(cardTypeService.getCardType(any())).thenReturn(type("NONE", false));
        CardProfile request = updateRequest(1L);
        request.setCardTypeId(3L);

        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.ACTIVE_CARD_TYPE_NOT_FOUND));
    }

    @Test
    void updateRejectsMissingOrInactiveTypeCategoryRelation() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(cardCategoryService.getCardCategory(any())).thenReturn(category("DEBIT", true));
        when(cardTypeService.getCardType(any())).thenReturn(type("NONE", true));
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(typeCategory("DEBIT", "NONE", false)));
        CardProfile request = updateRequest(1L);
        request.setCardCategoryId(2L);
        request.setCardTypeId(3L);

        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.CARDTYPECATEGORY_NOT_FOUND));
        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.CARDTYPECATEGORY_NOT_FOUND));
    }

    @Test
    void updateRejectsMissingOrInactiveFeeProfile() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(feeProfileRepository.findById(5L)).thenReturn(Optional.empty())
                .thenReturn(Optional.of(feeProfile(false)));
        CardProfile request = updateRequest(1L);
        request.setFeeProfileIdLong(5L);

        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.FEE_PROFILE_NOT_FOUND));
        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.ACTIVE_FEE_PROFILE_NOT_FOUND));
    }

    // ---------------------------------------------------------------- update: patterns

    private CardProfile requestWithPattern(long profileId, long patternId) {
        CardProfile request = updateRequest(profileId);
        request.setNumPatternId(patternId);
        return request;
    }

    @Test
    void updateRejectsPatternAlreadyLinkedToTheProfile() {
        CardProfile existing = existingProfile(1L);
        existing.setCardNumberRangeList(new ArrayList<>(List.of(pattern(9L, CardNumberPatternStatus.ASSIGN))));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateCardProfile(requestWithPattern(1L, 9L)))
                .satisfies(t -> assertDomainError(t, StatusService.DUPLICATE_PATTERN));
    }

    @Test
    void updateRejectsUnknownPattern() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class))).thenReturn(null);

        assertThatThrownBy(() -> service.updateCardProfile(requestWithPattern(1L, 9L)))
                .satisfies(t -> assertDomainError(t, StatusService.CARD_NUMBER_PATTERN_NOT_FOUND));
    }

    @Test
    void updateRejectsPatternThatIsNotActive() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class)))
                .thenReturn(pattern(9L, CardNumberPatternStatus.INACTIVE));

        assertThatThrownBy(() -> service.updateCardProfile(requestWithPattern(1L, 9L)))
                .satisfies(t -> assertDomainError(t, StatusService.ACTIVE_PATTERN_DOES_NOT_EXIST));
    }

    @Test
    void updateRejectsPatternAssignedToAnotherProfile() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        CardNumberPattern pattern = pattern(9L, CardNumberPatternStatus.ACTIVE);
        pattern.setCardProfileId(new CardProfileId(77L));
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class))).thenReturn(pattern);

        assertThatThrownBy(() -> service.updateCardProfile(requestWithPattern(1L, 9L)))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID));
    }

    @Test
    void updateAssignsFreePatternToTheProfile() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        existing.setCardNumberRangeList(new ArrayList<>(List.of(pattern(8L, CardNumberPatternStatus.ASSIGN))));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        CardNumberPattern pattern = pattern(9L, CardNumberPatternStatus.ACTIVE);
        pattern.setCardProfileId(new CardProfileId(1L));
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class))).thenReturn(pattern);
        when(cardRepository.existsByProfileId(1L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);

        service.updateCardProfile(requestWithPattern(1L, 9L));

        assertThat(pattern.getPatternStatus()).isEqualTo(CardNumberPatternStatus.ASSIGN);
        assertThat(pattern.getCardProfile()).isSameAs(existing);
        verify(patternService).updateCardNumberPattern(pattern);
    }

    @Test
    void updateIgnoresLinkedPatternsWithoutId() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        existing.setCardNumberRangeList(new ArrayList<>(List.of(pattern(null, CardNumberPatternStatus.ASSIGN))));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class)))
                .thenReturn(pattern(9L, CardNumberPatternStatus.ACTIVE));
        when(cardRepository.existsByProfileId(1L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);

        service.updateCardProfile(requestWithPattern(1L, 9L));

        verify(patternService).updateCardNumberPattern(any(CardNumberPattern.class));
    }

    // ---------------------------------------------------------------- update: apply

    @Test
    void updateWithoutLinkedCardsAppliesEverythingIncludingReferences() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(bankService.findById(1L)).thenReturn(bank(true));
        when(cardCategoryService.getCardCategory(any())).thenReturn(category("DEBIT", true));
        when(cardTypeService.getCardType(any())).thenReturn(type("NONE", true));
        CardTypeCategory relation = typeCategory("DEBIT", "NONE", true);
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L)).thenReturn(Optional.of(relation));
        when(feeProfileRepository.findById(5L)).thenReturn(Optional.of(feeProfile(true)));
        when(cardRepository.existsByProfileId(1L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);

        CardProfile request = updateRequest(1L);
        request.setProfileName("Platinum");
        request.setIssuingBankId(1L);
        request.setCardCategoryId(2L);
        request.setCardTypeId(3L);
        request.setFeeProfileIdLong(5L);
        request.setMinimumPasswordLength(4);
        CardNumberPattern newPattern = pattern(9L, CardNumberPatternStatus.ACTIVE);
        request.setNumPattern(newPattern);
        when(repository.existsByProfileName("Platinum")).thenReturn(false);

        var event = service.updateCardProfile(request);

        CardProfile updated = event.getCardProfile();
        assertThat(updated.getProfileName()).isEqualTo("Platinum");
        assertThat(updated.getMinimumPasswordLength()).isEqualTo(4);
        assertThat(updated.getBank().getName()).isEqualTo("Melli");
        assertThat(updated.getBankId().getValue()).isEqualTo(1L);
        assertThat(updated.getCardTypeCategory()).isSameAs(relation);
        assertThat(updated.getCardTypeCategoryId().getValue()).isEqualTo(4L);
        assertThat(updated.getCardType().getName()).isEqualTo("NONE");
        assertThat(updated.getCardCategory().getName()).isEqualTo("DEBIT");
        assertThat(updated.getFeeProfile().getName()).isEqualTo("Basic");
        assertThat(updated.getNumPattern()).isSameAs(newPattern);
    }

    @Test
    void updateWithLinkedCardsChangesOnlyMutableFields() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(cardRepository.existsByProfileId(1L)).thenReturn(true);
        when(repository.save(existing)).thenReturn(existing);
        CardProfile request = updateRequest(1L);
        request.setMaximumPasswordLength(8);
        CardNumberPattern newPattern = pattern(9L, CardNumberPatternStatus.ACTIVE);
        request.setNumPattern(newPattern);

        var event = service.updateCardProfile(request);

        assertThat(event.getCardProfile().getMaximumPasswordLength()).isEqualTo(8);
        assertThat(event.getCardProfile().getCardNumberRangeList()).containsExactly(newPattern);
    }

    @Test
    void updateWithLinkedCardsRejectsImmutableFieldChangesWithDedicatedCode() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(cardRepository.existsByProfileId(1L)).thenReturn(true);
        Bank otherBank = bank(true);
        otherBank.setId(new BankId(99L));
        when(bankService.findById(99L)).thenReturn(otherBank);
        CardProfile request = updateRequest(1L);
        request.setIssuingBankId(99L);

        assertThatThrownBy(() -> service.updateCardProfile(request))
                .satisfies(t -> assertDomainError(t, StatusService.CARD_PROFILE_HAS_CARDS));
    }

    @Test
    void updateRethrowsOtherDomainErrorsUnchanged() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile(1L)));
        when(cardRepository.existsByProfileId(1L)).thenReturn(false);
        CardProfile invalid = CardProfile.builder().validityPeriod(0).build();
        invalid.setId(new CardProfileId(1L));

        assertThatThrownBy(() -> service.updateCardProfile(invalid))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("bigger than zero");
    }

    // ---------------------------------------------------------------- activate / deactivate / queries

    @Test
    void activateAndDeactivateToggleTheProfile() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.activateCardProfile(updateRequest(1L)).getCardProfile().isActive()).isTrue();
        assertThat(service.deactivateCardProfile(updateRequest(1L)).getCardProfile().isActive()).isFalse();
    }

    @Test
    void activateAndDeactivateRequireAnIdAndAnExistingProfile() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        CardProfile noId = CardProfile.builder().build();

        assertThatThrownBy(() -> service.activateCardProfile(noId))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID));
        assertThatThrownBy(() -> service.deactivateCardProfile(noId))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID));
        assertThatThrownBy(() -> service.activateCardProfile(updateRequest(2L)))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("CardProfile not found: 2");
        assertThatThrownBy(() -> service.deactivateCardProfile(updateRequest(2L)))
                .satisfies(t -> assertDomainError(t, StatusService.ID_NOT_FOUND));
    }

    @Test
    void getReturnsProfileOrFails() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(service.getCardProfile(new GetCardProfileQuery(1L))).isSameAs(existing);
        assertThatThrownBy(() -> service.getCardProfile(new GetCardProfileQuery(2L)))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("2");
    }

    @Test
    void getAllReturnsEveryProfile() throws CardDomainException {
        when(repository.findAll()).thenReturn(List.of(existingProfile(1L), existingProfile(2L)));

        assertThat(service.getAllCardProfiles(new GetAllCardProfilesQuery())).hasSize(2);
    }

    @Test
    void listDelegatesAndWrapsFailures() throws CardDomainException {
        Map<String, String> params = Map.of();
        Page<CardProfile> page = new PageImpl<>(List.of(existingProfile(1L)));
        when(repository.findAllProfiles(params)).thenReturn(page).thenThrow(new IllegalStateException("db"));

        assertThat(service.listCardProfiles(params, "me", "ip")).isSameAs(page);
        assertThatThrownBy(() -> service.listCardProfiles(params, "me", "ip"))
                .isInstanceOfSatisfying(CardDomainException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(StatusService.GENERAL_ERROR);
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Test
    void findByIdReturnsProfileOrNotFound() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(service.findById(1L)).isSameAs(existing);
        assertThatThrownBy(() -> service.findById(2L)).isInstanceOfSatisfying(CardDomainException.class, e -> {
            assertThat(e.getStatus()).isEqualTo(StatusService.CARD_PROFILE_NOT_FOUND);
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    @Test
    void assignedPatternIsSavedOncePerCreation() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        stubReferenceLookups("DEBIT", "NONE");
        when(patternService.getCardNumberPattern(any(GetCardNumberPatternQuery.class)))
                .thenReturn(pattern(9L, CardNumberPatternStatus.ACTIVE));
        when(repository.save(any(CardProfile.class))).thenAnswer(i -> {
            CardProfile saved = i.getArgument(0);
            saved.setId(new CardProfileId(10L));
            return saved;
        });
        when(repository.findById(10L)).thenReturn(Optional.empty());

        service.createCardProfile(debitRequest().NumPatternId(9L).build());

        ArgumentCaptor<CardNumberPattern> captor = ArgumentCaptor.forClass(CardNumberPattern.class);
        verify(patternService, times(1)).save(captor.capture());
        assertThat(captor.getValue().getPatternStatus()).isEqualTo(CardNumberPatternStatus.ASSIGN);
    }

    // ---------------------------------------------------------------- extra branches

    @Test
    void createWithoutCategoryAndTypeCannotResolveTheTypeCategoryRelation() throws CardDomainException {
        when(repository.existsByProfileName("Gold")).thenReturn(false);
        when(bankService.findById(1L)).thenReturn(bank(true));

        // With neither id the relation stays null; the service dereferences it (assert / NPE in production)
        assertThatThrownBy(() -> service.createCardProfile(debitRequest().cardCategoryId(null).cardTypeId(null).build()))
                .isInstanceOfAny(AssertionError.class, NullPointerException.class);
        verifyNoInteractions(cardCategoryService, cardTypeService, cardTypeCategoryRepository);
    }

    @Test
    void updateWithOnlyTheCategoryChecksTheCategoryButNotTheRelation() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(cardCategoryService.getCardCategory(any())).thenReturn(category("DEBIT", true));
        when(cardRepository.existsByProfileId(1L)).thenReturn(true);
        when(repository.save(existing)).thenReturn(existing);
        CardProfile request = updateRequest(1L);
        request.setCardCategoryId(2L);

        service.updateCardProfile(request);

        verify(cardCategoryService).getCardCategory(any());
        verify(repository).save(existing);
        verifyNoInteractions(cardTypeCategoryRepository);
    }

    @Test
    void updateWithOnlyTheTypeChecksTheTypeButNotTheRelation() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(cardTypeService.getCardType(any())).thenReturn(type("NONE", true));
        when(cardRepository.existsByProfileId(1L)).thenReturn(true);
        when(repository.save(existing)).thenReturn(existing);
        CardProfile request = updateRequest(1L);
        request.setCardTypeId(3L);

        service.updateCardProfile(request);

        verify(cardTypeService).getCardType(any());
        verify(repository).save(existing);
        verifyNoInteractions(cardTypeCategoryRepository);
    }

    @Test
    void updateKeepsCategoryAndTypeWhenTheRelationCarriesOnlyIds() throws CardDomainException {
        CardProfile existing = existingProfile(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(cardCategoryService.getCardCategory(any())).thenReturn(category("DEBIT", true));
        when(cardTypeService.getCardType(any())).thenReturn(type("NONE", true));
        CardTypeCategory bareRelation = CardTypeCategory.builder().isActive(true).build();
        bareRelation.setId(new CardTypeCategoryId(4L));
        when(cardTypeCategoryRepository.findByCardTypeAndCardCategory(3L, 2L)).thenReturn(Optional.of(bareRelation));
        when(cardRepository.existsByProfileId(1L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);
        CardProfile request = updateRequest(1L);
        request.setCardCategoryId(2L);
        request.setCardTypeId(3L);

        var event = service.updateCardProfile(request);

        assertThat(event.getCardProfile().getCardTypeCategory()).isSameAs(bareRelation);
        assertThat(event.getCardProfile().getCardType().getName()).isEqualTo("NONE");
        assertThat(event.getCardProfile().getCardCategory().getName()).isEqualTo("DEBIT");
    }

    @Test
    void updateRethrowsDomainErrorsThatHaveNoMessage() throws CardDomainException {
        CardProfile spied = spy(existingProfile(1L));
        when(repository.findById(1L)).thenReturn(Optional.of(spied));
        when(cardRepository.existsByProfileId(1L)).thenReturn(true);
        CardDomainException withoutMessage = new CardDomainException((String) null);
        doThrow(withoutMessage).when(spied).updateWithLinkedCards(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.updateCardProfile(updateRequest(1L))).isSameAs(withoutMessage);
    }

    @Test
    void attachingAPatternWithoutARequestedIdDoesNothing() throws Exception {
        var method = CardProfileServiceImpl.class.getDeclaredMethod("attachRequestedNumberPattern", CardProfile.class, CardProfile.class);
        method.setAccessible(true);

        method.invoke(service, existingProfile(1L), updateRequest(1L));

        verifyNoInteractions(patternService);
    }
}
