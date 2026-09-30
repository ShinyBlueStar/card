package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.cardProfile.*;
import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import com.sample.system.card.service.domain.response.profile.*;
import com.sample.system.card.service.domain.valueObject.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardProfileDataMapperTest {

    private final CardProfileDataMapper mapper = new CardProfileDataMapper(new CardNumberPatternDataMapper());

    private static CardProfile profile(long id) {
        CardProfile profile = CardProfile.builder().profileName("Gold").isActive(true).build();
        profile.setId(new CardProfileId(id));
        return profile;
    }

    private static CardType type(String name, String description) {
        return new CardType(new CardTypeId(3L), 7, name, description, true);
    }

    private static CardCategory category() {
        return new CardCategory(new CardCategoryId(2L), 5, "DEBIT", "debit", true);
    }

    // ------------------------------------------------------------ create

    @Test
    void createCommandIsMappedWithEveryField() {
        CreateCardProfileCommand command = CreateCardProfileCommand.builder()
                .profileName("Gold").issuingBankId(1L).feeProfileId(5L).cardCategoryId(2L).cardTypeId(3L)
                .minimumPasswordLength(4).maximumPasswordLength(6).minimumSecondaryPasswordLength(5)
                .maximumSecondaryPasswordLength(8).allowedPinAttempts(3).allowedPin2Attempts(2).cvv2Length(3)
                .validityPeriod(36).validityType("M").cardIssueMethod(1).cardNumberPatternId(9L).initialCardStatus(true)
                .changeCvv2OnExpirationDateRenewal(true).isActive(true).additionalInfoIsRequired(false).pin2GenMethod(2)
                .reloadable(2).salesMethod(1).maxNumCreditCard(3).maximumAllowedAmount(BigDecimal.TEN)
                .cardRenewalType(2).build();

        CardProfile profile = mapper.createCommandToProfile(command);

        assertThat(profile.getProfileName()).isEqualTo("Gold");
        assertThat(profile.getIssuingBankId()).isEqualTo(1L);
        assertThat(profile.getBankId().getValue()).isEqualTo(1L);
        assertThat(profile.getFeeProfileId().getValue()).isEqualTo(5L);
        assertThat(profile.getFeeProfileIdLong()).isEqualTo(5L);
        assertThat(profile.getCardCategoryId()).isEqualTo(2L);
        assertThat(profile.getCardTypeId()).isEqualTo(3L);
        assertThat(profile.getValidityPeriod()).isEqualTo(36);
        assertThat(profile.getReloadable()).isEqualTo(Reloadable.RELOADABLE);
        assertThat(profile.getSalesMethod()).isEqualTo(SalesMethod.BRANCH);
        assertThat(profile.getCardIssueMethod()).isEqualTo(CardIssueMethod.VIRTUAL);
        assertThat(profile.getCardRenewalType()).isEqualTo(CardRenewalType.RENEW_WITH_PRINT);
        assertThat(profile.getPin2GenMethod()).isEqualTo(Pin2GenMethod.DYNAMIC);
        assertThat(profile.getNumPatternId()).isEqualTo(9L);
        assertThat(profile.getMaximumAllowedAmount()).isEqualByComparingTo("10");
        assertThat(profile.getInitialCardStatus()).isTrue();
        assertThat(profile.getAdditionalInfoIsRequired()).isFalse();
    }

    @Test
    void createCommandWithoutOptionalValuesUsesDefaults() {
        CardProfile profile = mapper.createCommandToProfile(CreateCardProfileCommand.builder().profileName("Basic").build());

        assertThat(profile.getBankId()).isNull();
        assertThat(profile.getFeeProfileId()).isNull();
        assertThat(profile.getReloadable()).isEqualTo(Reloadable.DEFAULT);
        assertThat(profile.getSalesMethod()).isEqualTo(SalesMethod.DEFAULT);
        assertThat(profile.getCardIssueMethod()).isNull();
        assertThat(profile.getCardRenewalType()).isNull();
        assertThat(profile.getPin2GenMethod()).isNull();
    }

    @Test
    void createCommandWithUnknownCodesIsRejected() {
        assertThatThrownBy(() -> mapper.createCommandToProfile(
                CreateCardProfileCommand.builder().reloadable(99).build()))
                .isInstanceOf(InvalidInputParameterException.class);
    }

    @Test
    void profileIsMappedToCreateAndUpdateResponses() {
        assertThat(mapper.profileToCreateResponse(profile(4L), "ok").cardProfileId()).isEqualTo(4L);
        UpdateCardProfileResponse update = mapper.cardProfileToUpdateCardProfileResponse(profile(5L), "done");
        assertThat(update.cardProfileId()).isEqualTo(5L);
        assertThat(update.message()).isEqualTo("done");
        assertThat(update.isActive()).isTrue();
    }

    // ------------------------------------------------------------ get response

    @Test
    void fullProfileIsMappedToGetResponse() {
        Bank bank = Bank.builder().binCode("603799").name("Melli").isActive(true).build();
        bank.setId(new BankId(1L));
        FeeProfile fee = new FeeProfile();
        fee.setId(new FeeProfileId(5L));
        fee.setName("Basic");
        fee.setIsActive(true);
        CardTypeCategory relation = CardTypeCategory.builder().cardType(type("Gold", "gold")).cardCategory(category()).isActive(true).build();
        CardNumberPattern pattern = CardNumberPattern.builder().cardNumberFrom("1000000").cardNumberTo("1999999").build();
        pattern.setId(new CardNumberPatternId(9L));
        CardProfile profile = profile(10L);
        profile.setBank(bank);
        profile.setFeeProfile(fee);
        profile.setCardTypeCategory(relation);
        profile.setCardNumberRangeList(List.of(pattern));
        profile.setReloadable(Reloadable.RELOADABLE);
        profile.setSalesMethod(SalesMethod.BRANCH);
        profile.setPin2GenMethod(Pin2GenMethod.CONSTANT);
        profile.setCardRenewalType(CardRenewalType.RENEW_WITH_PRINT);
        profile.setCardIssueMethod(CardIssueMethod.PHYSICAL);
        profile.setInitialCardStatus(true);
        profile.setCreatedBy("ali");
        profile.setCreatedDate(Timestamp.valueOf("2024-03-20 10:15:30"));
        profile.setLastModifiedBy("reza");
        profile.setLastModifiedDate(Timestamp.valueOf("2024-04-21 11:00:00"));

        GetCardProfileResponse response = mapper.profileToGetResponse(profile);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.profileName()).isEqualTo("Gold");
        assertThat(response.cardType()).isEqualTo("Gold");
        assertThat(response.cardTypeResponse().id()).isEqualTo(3L);
        assertThat(response.cardTypeResponse().code()).isEqualTo("7");
        assertThat(response.cardCategoryResponse().name()).isEqualTo("DEBIT");
        assertThat(response.issuingBank().binCode()).isEqualTo("603799");
        assertThat(response.feeProfile().name()).isEqualTo("Basic");
        assertThat(response.cardNumberRangeList()).hasSize(1);
        assertThat(response.reloadable().code()).isEqualTo(Reloadable.RELOADABLE.getCode());
        assertThat(response.salesMethod().code()).isEqualTo(SalesMethod.BRANCH.getCode());
        assertThat(response.pin2GenMethod().code()).isEqualTo(Pin2GenMethod.CONSTANT.getCode());
        assertThat(response.cardRenewalType().code()).isEqualTo(CardRenewalType.RENEW_WITH_PRINT.getCode());
        assertThat(response.issueMethod().code()).isEqualTo(CardIssueMethod.PHYSICAL.getCode());
        assertThat(response.initialCardStatus()).isTrue();
        assertThat(response.createdBy()).isEqualTo("ali");
        assertThat(response.createdDate()).isNotBlank();
        assertThat(response.lastModifiedDate()).isNotBlank();
        assertThat(response.message()).isNull();
    }

    @Test
    void emptyProfileIsMappedWithSafeDefaults() {
        GetCardProfileResponse response = mapper.profileToGetResponse(profile(1L));

        assertThat(response.cardType()).isNull();
        assertThat(response.cardTypeResponse()).isNull();
        assertThat(response.cardCategoryResponse()).isNull();
        assertThat(response.issuingBank()).isNull();
        assertThat(response.feeProfile()).isNull();
        assertThat(response.cardNumberRangeList()).isEmpty();
        assertThat(response.initialCardStatus()).isFalse();
        assertThat(response.createdDate()).isNull();
        assertThat(response.reloadable()).isNull();
    }

    @Test
    void typeAndCategoryFallBackToTheProfileEntitiesAndIds() {
        CardProfile withEntities = profile(1L);
        withEntities.setCardType(type("Plain", "plain"));
        withEntities.setCardCategory(category());
        GetCardProfileResponse fromEntities = mapper.profileToGetResponse(withEntities);
        assertThat(fromEntities.cardTypeResponse().name()).isEqualTo("Plain");
        assertThat(fromEntities.cardCategoryResponse().name()).isEqualTo("DEBIT");

        CardProfile withIds = profile(2L);
        withIds.setCardTypeId(33L);
        withIds.setCardCategoryId(22L);
        GetCardProfileResponse fromIds = mapper.profileToGetResponse(withIds);
        assertThat(fromIds.cardTypeResponse().id()).isEqualTo(33L);
        assertThat(fromIds.cardTypeResponse().name()).isNull();
        assertThat(fromIds.cardCategoryResponse().id()).isEqualTo(22L);
    }

    @Test
    void typeAndCategoryWithoutIdOrCodeAreStillMapped() {
        CardProfile profile = profile(1L);
        profile.setCardType(new CardType(null, null, "NoId", null, null));
        profile.setCardCategory(new CardCategory(null, null, "NoId", null, null));

        GetCardProfileResponse response = mapper.profileToGetResponse(profile);

        assertThat(response.cardTypeResponse().id()).isNull();
        assertThat(response.cardTypeResponse().code()).isNull();
        assertThat(response.cardTypeResponse().name()).isEqualTo("NoId");
        assertThat(response.cardCategoryResponse().code()).isNull();
    }

    @Test
    void completelyEmptyTypeAndCategoryAreOmitted() {
        CardProfile profile = profile(1L);
        profile.setCardType(new CardType());
        profile.setCardCategory(new CardCategory());

        GetCardProfileResponse response = mapper.profileToGetResponse(profile);

        assertThat(response.cardTypeResponse()).isNull();
        assertThat(response.cardCategoryResponse()).isNull();
    }

    @Test
    void cardTypeDisplayUsesDescriptionWhenNameIsBlank() {
        CardProfile blankName = profile(1L);
        blankName.setCardType(type(" ", "described"));
        CardProfile nullName = profile(2L);
        nullName.setCardType(type(null, "only description"));

        assertThat(mapper.profileToGetResponse(blankName).cardType()).isEqualTo("described");
        assertThat(mapper.profileToGetResponse(nullName).cardType()).isEqualTo("only description");
    }

    @Test
    void bankAndFeeProfileWithoutIdAreOmitted() {
        CardProfile profile = profile(1L);
        profile.setBank(Bank.builder().name("No id").build());
        profile.setFeeProfile(new FeeProfile());

        GetCardProfileResponse response = mapper.profileToGetResponse(profile);

        assertThat(response.issuingBank()).isNull();
        assertThat(response.feeProfile()).isNull();
    }

    @Test
    void profileListsAndPagesAreMapped() {
        assertThat(mapper.profilesToGetResponses(List.of(profile(1L), profile(2L)))).hasSize(2);

        CardProfileListResponse page = mapper.profilesToGetListResponse(
                new PageImpl<>(List.of(profile(1L)), PageRequest.of(1, 1), 3));
        assertThat(page.getList()).hasSize(1);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(mapper.profilesToGetListResponse(null)).isNull();
    }

    // ------------------------------------------------------------ update / activate / deactivate

    @Test
    void updateCommandIsMappedWithEveryProvidedField() throws CardDomainException {
        UpdateCardProfileCommand command = UpdateCardProfileCommand.builder()
                .cardProfileId(10L).profileName("Platinum").issuingBankId(1L).cardNumberPatternId(9L).feeProfileId(5L)
                .cardCategoryId(2L).cardTypeId(3L).minimumPasswordLength(4).maximumPasswordLength(6)
                .minimumSecondaryPasswordLength(5).maximumSecondaryPasswordLength(8).allowedPinAttempts(3)
                .validityPeriod(24).validityType("M").initialCardStatus(true).isActive(false).cvv2Length(3)
                .changeCvv2OnExpirationDateRenewal(true).additionalInfoIsRequired(true).Pin2GenMethod(3).reloadable(1)
                .allowedPin2Attempts(2).salesMethod(2).maxNumCreditCard(4).maximumAllowedAmount(BigDecimal.ONE)
                .IssueMethod(0).cardRenewalType(1).build();

        CardProfile profile = mapper.updateCommandToProfile(command);

        assertThat(profile.getId().getValue()).isEqualTo(10L);
        assertThat(profile.getProfileName()).isEqualTo("Platinum");
        assertThat(profile.getBankId().getValue()).isEqualTo(1L);
        assertThat(profile.getFeeProfileId().getValue()).isEqualTo(5L);
        assertThat(profile.getNumPatternId()).isEqualTo(9L);
        assertThat(profile.getValidityPeriod()).isEqualTo(24);
        assertThat(profile.getReloadable()).isEqualTo(Reloadable.NON_RELOADABLE);
        assertThat(profile.getSalesMethod()).isEqualTo(SalesMethod.UNLIMITED);
        assertThat(profile.getPin2GenMethod()).isEqualTo(Pin2GenMethod.COMPOSITE);
        assertThat(profile.getCardIssueMethod()).isEqualTo(CardIssueMethod.PHYSICAL);
        assertThat(profile.getCardRenewalType()).isEqualTo(CardRenewalType.RENEW_WITHOUT_PRINT);
        assertThat(profile.getIsActive()).isFalse();
        assertThat(profile.getInitialCardStatus()).isTrue();
        assertThat(profile.getMaximumAllowedAmount()).isEqualByComparingTo("1");
        assertThat(profile.getMaxNumCreditCard()).isEqualTo(4);
        assertThat(profile.getAllowedPin2Attempts()).isEqualTo(2);
        assertThat(profile.getCvv2Length()).isEqualTo(3);
        assertThat(profile.getMinimumSecondaryPasswordLength()).isEqualTo(5);
        assertThat(profile.getMaximumSecondaryPasswordLength()).isEqualTo(8);
        assertThat(profile.getMinimumPasswordLength()).isEqualTo(4);
        assertThat(profile.getMaximumPasswordLength()).isEqualTo(6);
        assertThat(profile.getAllowedPinAttempts()).isEqualTo(3);
        assertThat(profile.getValidityType()).isEqualTo("M");
        assertThat(profile.getChangeCvv2OnExpirationDateRenewal()).isTrue();
        assertThat(profile.getAdditionalInfoIsRequired()).isTrue();
    }

    @Test
    void updateCommandWithOnlyAnIdLeavesEverythingElseUnset() throws CardDomainException {
        CardProfile profile = mapper.updateCommandToProfile(UpdateCardProfileCommand.builder().cardProfileId(10L).build());

        assertThat(profile.getId().getValue()).isEqualTo(10L);
        assertThat(profile.getBankId()).isNull();
        assertThat(profile.getFeeProfileId()).isNull();
        assertThat(profile.getNumPatternId()).isNull();
        assertThat(profile.getReloadable()).isNull();
        assertThat(profile.getSalesMethod()).isNull();
        assertThat(profile.getPin2GenMethod()).isNull();
        assertThat(profile.getCardIssueMethod()).isNull();
        assertThat(profile.getIsActive()).isNull();
    }

    @Test
    void updateCommandWithInvalidValidityPeriodIsRejected() {
        assertThatThrownBy(() -> mapper.updateCommandToProfile(
                UpdateCardProfileCommand.builder().cardProfileId(10L).validityPeriod(0).build()))
                .isInstanceOf(CardDomainException.class);
    }

    @Test
    void activateAndDeactivateCommandsCarryTheRequestedState() {
        CardProfile activated = mapper.activateCommandToProfile(ActivateCardProfileCommand.builder().cardProfileId(1L).build());
        CardProfile deactivated = mapper.deactivateCommandToProfile(DeactivateCardProfileCommand.builder().cardProfileId(2L).build());

        assertThat(activated.getId().getValue()).isEqualTo(1L);
        assertThat(activated.getIsActive()).isTrue();
        assertThat(deactivated.getId().getValue()).isEqualTo(2L);
        assertThat(deactivated.getIsActive()).isFalse();
    }

    // ------------------------------------------------------------ parsing helpers

    @Test
    void salesMethodIsParsedByNameOrDescription() {
        assertThat(mapper.parseSalesMethod((String) null)).isNull();
        assertThat(mapper.parseSalesMethod("branch")).isEqualTo(SalesMethod.BRANCH);
        assertThat(mapper.parseSalesMethod(SalesMethod.UNLIMITED.getDescription())).isEqualTo(SalesMethod.UNLIMITED);
        assertThat(mapper.parseSalesMethod("unknown")).isNull();
    }

    @Test
    void salesMethodAndPin2MethodAreParsedByCode() {
        assertThat(mapper.parseSalesMethod((Integer) null)).isNull();
        assertThat(mapper.parseSalesMethod(Integer.valueOf(2))).isEqualTo(SalesMethod.UNLIMITED);
        assertThat(mapper.parsePin2GenMethod(null)).isNull();
        assertThat(mapper.parsePin2GenMethod(1)).isEqualTo(Pin2GenMethod.CONSTANT);
    }

    @Test
    void typeAndCategoryCarryingASingleAttributeAreStillMapped() {
        CardProfile onlyDescription = profile(1L);
        onlyDescription.setCardType(new CardType(null, null, null, "only description", null));
        onlyDescription.setCardCategory(new CardCategory(null, null, null, "only description", null));
        CardProfile onlyActiveFlag = profile(2L);
        onlyActiveFlag.setCardType(new CardType(null, null, null, null, false));
        onlyActiveFlag.setCardCategory(new CardCategory(null, null, null, null, false));

        GetCardProfileResponse described = mapper.profileToGetResponse(onlyDescription);
        GetCardProfileResponse flagged = mapper.profileToGetResponse(onlyActiveFlag);

        assertThat(described.cardTypeResponse().description()).isEqualTo("only description");
        assertThat(described.cardCategoryResponse().description()).isEqualTo("only description");
        assertThat(flagged.cardTypeResponse().isActive()).isFalse();
        assertThat(flagged.cardCategoryResponse().isActive()).isFalse();
    }

    @Test
    void privateResponseBuildersTolerateAMissingProfile() throws Exception {
        for (String name : List.of("mapCardTypeToResponse", "mapCardCategoryToResponse")) {
            var method = CardProfileDataMapper.class.getDeclaredMethod(name, CardProfile.class);
            method.setAccessible(true);
            assertThat(method.invoke(mapper, (Object) null)).isNull();
        }
    }
}
