package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.card.GetAllCardsQuery;
import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.*;
import com.sample.system.card.service.domain.ports.output.exporter.CardPrintExporterRepository;
import com.sample.system.card.service.domain.ports.output.externalService.CreditService;
import com.sample.system.card.service.domain.ports.output.externalService.PartyGateway;
import com.sample.system.card.service.domain.ports.output.externalService.ProductService;
import com.sample.system.card.service.domain.ports.output.externalService.SSMService;
import com.sample.system.card.service.domain.ports.output.repository.*;
import com.sample.system.card.service.domain.response.accessfile.AccessFileResponse;
import com.sample.system.card.service.domain.response.accessfile.result.AccessFileResult;
import com.sample.system.card.service.domain.valueObject.BankId;
import com.sample.system.card.service.domain.valueObject.CardId;
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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    private static final String PAN = "6037991200000011";

    @Mock
    private CardRepository repository;
    @Mock
    private CardNumberGeneratorService numberGenerator;
    @Mock
    private CardProfileRepository profileRepository;
    @Mock
    private PartyGateway partyGateway;
    @Mock
    private CreditService creditService;
    @Mock
    private SSMService ssmService;
    @Mock
    private TrackGenerationService trackService;
    @Mock
    private ReasonService reasonService;
    @Mock
    private ProductService productService;
    @Mock
    private CardPanCacheRepository panCache;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CardPrintExporterRepository exporter;
    @Mock
    private CustomerService customerService;

    private CardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardServiceImpl(repository, numberGenerator, profileRepository, partyGateway, creditService,
                ssmService, trackService, reasonService, productService, panCache, customerRepository, exporter,
                customerService);
    }

    // ------------------------------------------------------------------ fixtures

    private static CardProfile profile(Integer validityPeriod, Integer cvv2Length, boolean initialActive) {
        Bank bank = Bank.builder().binCode("603799").name("Melli").isActive(true).build();
        bank.setId(new BankId(1L));
        return CardProfile.builder().profileName("Gold").bank(bank).validityPeriod(validityPeriod).cvv2Length(cvv2Length)
                .initialCardStatus(initialActive).allowedPinAttempts(3).maximumAllowedAmount(BigDecimal.TEN)
                .cardIssueMethod(CardIssueMethod.PHYSICAL).build();
    }

    private static Customer customer(String nationalId) {
        return Customer.builder().nationalId(nationalId).firstName("Ali").lastName("Rezaei").build();
    }

    private static Card newCardRequest(String nationalId) {
        return Card.builder().cardProfileId(new CardProfileId(1L)).customer(customer(nationalId)).caseNumber("C-1")
                .unitCode("U1").unitName("Unit").issuerPersonId("p1").build();
    }

    private static Card existing(long id, CardStatus status) {
        Card card = Card.builder().pan(PAN).cardStatus(status).cardProfileId(new CardProfileId(1L))
                .customer(customer("0012345678")).expDate(LocalDate.now().plusMonths(1).toString())
                .caseNumber("C-1").unitCode("U1").unitName("Unit").macnaCode("M1").crn("1").issuingBank("Melli")
                .isVirtualCard(false).creditLimit(BigDecimal.ONE).build();
        card.setId(new CardId(id));
        return card;
    }

    private static Reason reason() {
        return Reason.builder().code(1).title("t").reason("lost card").groupId(ReasonGroup.BLOCK_CARD).build();
    }

    private void assertError(Throwable t, int status, HttpStatus http) {
        assertThat(t).isInstanceOfSatisfying(CardDomainException.class, e -> {
            assertThat(e.getStatus()).isEqualTo(status);
            assertThat(e.getHttpStatus()).isEqualTo(http);
        });
    }

    private void stubSave() {
        when(repository.save(any(Card.class))).thenAnswer(i -> {
            Card saved = i.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(new CardId(99L));
            }
            return saved;
        });
    }

    /** Stubs everything createCard needs for a valid request. */
    private void stubCreateDependencies(CardProfile profile, String cvv2) throws CardDomainException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(numberGenerator.process(any())).thenReturn(PAN);
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), anyInt(), anyString(), eq("120"))).thenReturn(cvv2);
    }

    private void stubCreateRest() throws CardDomainException {
        when(creditService.getMacnaCode("0012345678")).thenReturn("M1");
        when(ssmService.getPin1(any(UUID.class), eq("0012345678"), eq(PAN))).thenReturn("1111");
        when(trackService.generateTrack1(eq(PAN), anyString())).thenReturn("t1");
        when(trackService.generateTrack2(any(UUID.class), eq(PAN), anyString())).thenReturn("t2");
        when(trackService.generateTrack3(PAN, null)).thenReturn("t3");
    }

    // ------------------------------------------------------------------ createCard

    @Test
    void createCardBuildsAnActiveCardWithTracksAndCaches() throws CardDomainException {
        stubCreateDependencies(profile(12, 3, true), "123");
        stubCreateRest();
        stubSave();

        Card saved = service.createCard(newCardRequest("0012345678")).getCard();

        assertThat(saved.getPan()).isEqualTo(PAN);
        assertThat(saved.getMaskedPan()).isEqualTo("603799******0011");
        assertThat(saved.getCardStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(saved.getCvv2()).isEqualTo("123");
        assertThat(saved.getIssuingBank()).isEqualTo("Melli");
        assertThat(saved.getMacnaCode()).isEqualTo("M1");
        assertThat(saved.getCifNo()).isEqualTo("0012345678");
        assertThat(saved.getCrn()).isEqualTo("1");
        assertThat(saved.getIsVirtualCard()).isFalse();
        assertThat(saved.getCreditLimit()).isEqualByComparingTo("10");
        assertThat(saved.getWrongPassRetryCount()).isEqualTo(3);
        assertThat(saved.getExpDate()).isNotBlank();
        assertThat(saved.getTrack().getTrack1()).isEqualTo("t1");
        assertThat(saved.getTrack().getTrack2()).isEqualTo("t2");
        assertThat(saved.getTrack().getTrack3()).isEqualTo("t3");
        assertThat(saved.getTrack().getPin1()).isEqualTo("1111");
        verify(customerService).checkOrSave(any(Customer.class));
        verify(panCache).addPan(PAN);
    }

    @Test
    void createCardWithoutInitialActivationIsIssued() throws CardDomainException {
        stubCreateDependencies(profile(null, null, false), "1234");
        stubCreateRest();
        stubSave();

        assertThat(service.createCard(newCardRequest("0012345678")).getCard().getCardStatus()).isEqualTo(CardStatus.ISSUED);
    }

    @Test
    void createCardIgnoresCacheFailures() throws CardDomainException {
        stubCreateDependencies(profile(12, 3, true), "123");
        stubCreateRest();
        stubSave();
        doThrow(new IllegalStateException("redis down")).when(panCache).addPan(PAN);

        assertThat(service.createCard(newCardRequest("0012345678")).getCard().getPan()).isEqualTo(PAN);
    }

    @Test
    void createCardSkipsTheCacheWhenSavedCardHasNoPan() throws CardDomainException {
        stubCreateDependencies(profile(12, 3, true), "123");
        stubCreateRest();
        when(repository.save(any(Card.class))).thenReturn(Card.builder().pan(" ").build());

        assertThat(service.createCard(newCardRequest("0012345678")).getCard().getId()).isNull();
        verifyNoInteractions(panCache);
    }

    @Test
    void createCardFailsWhenProfileIsUnknown() {
        when(profileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCard(newCardRequest("0012345678")))
                .satisfies(t -> assertError(t, StatusService.CARD_PROFILE_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }

    @Test
    void createCardFailsWhenGeneratedPanIsNotSixteenDigits() throws CardDomainException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));
        when(numberGenerator.process(any())).thenReturn("123");

        assertThatThrownBy(() -> service.createCard(newCardRequest("0012345678")))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("not 16 digits");
    }

    @Test
    void createCardRequiresANationalId() throws CardDomainException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));
        when(numberGenerator.process(any())).thenReturn(PAN);

        assertThatThrownBy(() -> service.createCard(newCardRequest(null))).hasMessageContaining("NationalId is required");
        assertThatThrownBy(() -> service.createCard(newCardRequest("  "))).hasMessageContaining("NationalId is required");
    }

    @Test
    void createCardValidatesTheGeneratedCvv2() throws CardDomainException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));
        when(numberGenerator.process(any())).thenReturn(PAN);
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), eq(3), anyString(), eq("120"))).thenReturn(null).thenReturn("12");

        assertThatThrownBy(() -> service.createCard(newCardRequest("0012345678"))).hasMessageContaining("returned null");
        assertThatThrownBy(() -> service.createCard(newCardRequest("0012345678")))
                .satisfies(t -> assertError(t, StatusService.INVALID_CVV2_LENGTH, HttpStatus.OK));
    }

    @Test
    void createCardFailsWhenTheCardCannotBeSaved() throws CardDomainException {
        stubCreateDependencies(profile(12, 3, true), "123");
        stubCreateRest();
        when(repository.save(any(Card.class))).thenReturn(null);

        assertThatThrownBy(() -> service.createCard(newCardRequest("0012345678"))).hasMessageContaining("Could not save Card");
    }

    // ------------------------------------------------------------------ queries

    @Test
    void getCardReturnsCardOrReportsNotFound() throws CardDomainException {
        Card found = existing(1L, CardStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(found));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(service.getCard(1L)).isSameAs(found);
        assertThatThrownBy(() -> service.getCard(2L))
                .satisfies(t -> assertError(t, StatusService.CARD_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }

    @Test
    void getAllCardsReturnsEveryCard() throws CardDomainException {
        when(repository.findAll()).thenReturn(List.of(existing(1L, CardStatus.ACTIVE), existing(2L, CardStatus.HOT)));

        assertThat(service.getAllCards(GetAllCardsQuery.builder().activeOnly(true).build())).hasSize(2);
        assertThat(service.getAllCards(GetAllCardsQuery.builder().activeOnly(null).build())).hasSize(2);
    }

    @Test
    void getAllCardsRethrowsRepositoryFailures() {
        when(repository.findAll()).thenThrow(new IllegalStateException("db"));

        assertThatThrownBy(() -> service.getAllCards(GetAllCardsQuery.builder().build()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void listCardsDelegatesAndRethrowsFailures() throws CardDomainException {
        Map<String, String> params = Map.of();
        Page<Card> page = new PageImpl<>(List.of(existing(1L, CardStatus.ACTIVE)));
        when(repository.findAllCards(params)).thenReturn(page).thenReturn(null).thenThrow(new IllegalStateException("db"));

        assertThat(service.listCards(params, "me", "ip")).isSameAs(page);
        assertThat(service.listCards(params, "me", "ip")).isNull();
        assertThatThrownBy(() -> service.listCards(params, "me", "ip")).isInstanceOf(IllegalStateException.class);
    }

    // ------------------------------------------------------------------ block / unblock

    @Test
    void blockRequiresACardWithId() {
        assertThatThrownBy(() -> service.blockCard(null, 1L, "me")).hasMessageContaining("Card id is required to block");
        assertThatThrownBy(() -> service.blockCard(Card.builder().build(), 1L, "me")).hasMessageContaining("Card id is required");
    }

    @Test
    void blockMovesAnActiveCardToHot() throws CardDomainException {
        Card card = existing(1L, CardStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(reasonService.findById(5L)).thenReturn(reason());
        when(repository.save(card)).thenReturn(card);

        var event = service.blockCard(card, 5L, "operator");

        assertThat(event.getCard().getCardStatus()).isEqualTo(CardStatus.HOT);
        assertThat(event.getCard().getChangeStateDescription()).contains("lost card").contains("operator");
        assertThat(event.getBlockedAt()).isNotNull();
    }

    @Test
    void blockRejectsCardsThatAreNotActive() throws CardDomainException {
        Card hot = existing(1L, CardStatus.HOT);
        Card inactive = existing(2L, CardStatus.INACTIVE);
        Card statusless = existing(3L, null);
        when(repository.findById(1L)).thenReturn(Optional.of(hot));
        when(repository.findById(2L)).thenReturn(Optional.of(inactive));
        when(repository.findById(3L)).thenReturn(Optional.of(statusless));
        when(reasonService.findById(5L)).thenReturn(reason());

        assertThatThrownBy(() -> service.blockCard(hot, 5L, "me"))
                .satisfies(t -> assertError(t, StatusService.CARD_STATUS_IS_HOT, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.blockCard(inactive, 5L, "me"))
                .satisfies(t -> assertError(t, StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.blockCard(statusless, 5L, "me"))
                .satisfies(t -> assertError(t, StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST));
    }

    @Test
    void blockFailsWhenSaveReturnsNull() throws CardDomainException {
        Card card = existing(1L, CardStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(reasonService.findById(5L)).thenReturn(reason());
        when(repository.save(card)).thenReturn(null);

        assertThatThrownBy(() -> service.blockCard(card, 5L, "me")).hasMessageContaining("Could not save blocked Card");
    }

    @Test
    void unblockMovesAHotCardBackToActive() throws CardDomainException {
        Card card = existing(1L, CardStatus.HOT);
        when(reasonService.findById(5L)).thenReturn(reason());
        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(repository.save(card)).thenReturn(card);

        var event = service.unblockCard(card, 5L, "operator");

        assertThat(event.getCard().getCardStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(event.getCard().getChangeStateDescription()).contains("lost card").contains("operator");
    }

    @Test
    void unblockValidatesTheCardAndItsStatus() throws CardDomainException {
        Card active = existing(1L, CardStatus.ACTIVE);
        when(reasonService.findById(5L)).thenReturn(reason());
        when(repository.findById(1L)).thenReturn(Optional.of(active));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.unblockCard(null, 5L, "me")).hasMessageContaining("Card id is required to unblock");
        assertThatThrownBy(() -> service.unblockCard(active, 5L, "me"))
                .satisfies(t -> assertError(t, StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST));
        Card missing = existing(2L, CardStatus.HOT);
        assertThatThrownBy(() -> service.unblockCard(missing, 5L, "me")).hasMessageContaining("Card not found with id: 2");
    }

    @Test
    void unblockFailsWhenSaveReturnsNull() throws CardDomainException {
        Card card = existing(1L, CardStatus.HOT);
        when(reasonService.findById(5L)).thenReturn(reason());
        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(repository.save(card)).thenReturn(null);

        assertThatThrownBy(() -> service.unblockCard(card, 5L, "me")).hasMessageContaining("Could not save unblocked Card");
    }

    // ------------------------------------------------------------------ activate / deactivate

    @Test
    void activateVirtualCardBecomesActive() throws CardDomainException {
        Card card = existing(1L, CardStatus.ISSUED);
        card.setIsVirtualCard(true);
        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(repository.save(card)).thenReturn(card);

        assertThat(service.activateCard(1L, 5L).getCard().getCardStatus()).isEqualTo(CardStatus.ACTIVE);
    }

    @Test
    void activatePhysicalCardOnlyAfterDelivery() throws CardDomainException {
        Card delivered = existing(1L, CardStatus.ISSUED);
        delivered.setCardPhysicalStatus(CardPhysicalStatus.DELIVERED);
        Card printed = existing(2L, CardStatus.ISSUED);
        printed.setCardPhysicalStatus(CardPhysicalStatus.PRINTED);
        when(repository.findById(1L)).thenReturn(Optional.of(delivered));
        when(repository.findById(2L)).thenReturn(Optional.of(printed));
        when(repository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.activateCard(1L, 5L).getCard().getCardStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(service.activateCard(2L, 5L).getCard().getCardStatus()).isEqualTo(CardStatus.ISSUED);
    }

    @Test
    void activateRejectsCardsThatCannotBeActivated() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existing(1L, CardStatus.ACTIVE)));
        when(repository.findById(2L)).thenReturn(Optional.of(existing(2L, CardStatus.EXPIRED)));
        when(repository.findById(3L)).thenReturn(Optional.of(existing(3L, CardStatus.INACTIVE)));

        assertThatThrownBy(() -> service.activateCard(1L, 5L))
                .satisfies(t -> assertError(t, StatusService.CARD_STATUS_IS_ACTIVE, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.activateCard(2L, 5L))
                .satisfies(t -> assertError(t, StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.activateCard(3L, 5L))
                .satisfies(t -> assertError(t, StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST));
    }

    @Test
    void activateFailsWhenSaveReturnsNullOrThrows() throws CardDomainException {
        Card first = existing(1L, CardStatus.ISSUED);
        first.setIsVirtualCard(true);
        Card second = existing(1L, CardStatus.ISSUED);
        second.setIsVirtualCard(true);
        when(repository.findById(1L)).thenReturn(Optional.of(first)).thenReturn(Optional.of(second));
        when(repository.save(any(Card.class))).thenReturn(null).thenThrow(new IllegalStateException("db"));
        when(repository.findById(9L)).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.activateCard(1L, 5L)).hasMessageContaining("Could not save activated Card");
        assertThatThrownBy(() -> service.activateCard(1L, 5L)).hasMessageContaining("Failed to activate card: db");
        assertThatThrownBy(() -> service.activateCard(9L, 5L)).hasMessageContaining("Failed to activate card");
    }

    @Test
    void deactivateMovesCardToInactive() throws CardDomainException {
        Card card = existing(1L, CardStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(productService.isInactivableCard(1L)).thenReturn(true);
        when(reasonService.findById(5L)).thenReturn(reason());
        when(repository.save(card)).thenReturn(card);

        var event = service.deactivateCard(1L, 5L, "operator");

        assertThat(event.getCard().getCardStatus()).isEqualTo(CardStatus.INACTIVE);
        assertThat(event.getDeactivatedBy()).isEqualTo("operator");
        assertThat(event.getReason()).isEqualTo("lost card");
    }

    @Test
    void deactivateValidatesReasonAndStatus() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existing(1L, CardStatus.INACTIVE)));
        when(repository.findById(2L)).thenReturn(Optional.of(existing(2L, CardStatus.EXPIRED)));

        assertThatThrownBy(() -> service.deactivateCard(1L, null, "me")).hasMessageContaining("Reason is needed");
        assertThatThrownBy(() -> service.deactivateCard(1L, 5L, "me"))
                .satisfies(t -> assertError(t, StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.deactivateCard(2L, 5L, "me"))
                .satisfies(t -> assertError(t, StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST));
    }

    @Test
    void deactivateRejectsCardsThatTheProductCannotInactivate() throws CardDomainException {
        when(repository.findById(1L)).thenReturn(Optional.of(existing(1L, CardStatus.ACTIVE)));
        when(productService.isInactivableCard(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.deactivateCard(1L, 5L, "me"))
                .satisfies(t -> assertError(t, StatusService.CARD_INQUIRY_ERROR, HttpStatus.BAD_REQUEST));
    }

    @Test
    void deactivateFailsWhenSaveReturnsNull() throws CardDomainException {
        Card card = existing(1L, CardStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(productService.isInactivableCard(1L)).thenReturn(true);
        when(reasonService.findById(5L)).thenReturn(reason());
        when(repository.save(card)).thenReturn(null);

        assertThatThrownBy(() -> service.deactivateCard(1L, 5L, "me")).hasMessageContaining("Could not save deactivated Card");
    }

    // ------------------------------------------------------------------ replacement

    @Test
    void replacementStatusMustBeHotOrInactive() throws CardDomainException {
        assertThatThrownBy(() -> service.validateCardStatusForReplacement(null))
                .satisfies(t -> assertError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.validateCardStatusForReplacement(existing(1L, null)))
                .satisfies(t -> assertError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.validateCardStatusForReplacement(existing(1L, CardStatus.ACTIVE)))
                .satisfies(t -> assertError(t, StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST));
        Card noId = Card.builder().cardStatus(CardStatus.ACTIVE).build();
        assertThatThrownBy(() -> service.validateCardStatusForReplacement(noId)).hasMessageContaining("null");

        service.validateCardStatusForReplacement(existing(1L, CardStatus.HOT));
        service.validateCardStatusForReplacement(existing(1L, CardStatus.INACTIVE));
    }

    private void stubReplacementDependencies(Card card) throws CardDomainException {
        when(repository.findById(card.getId().getValue())).thenReturn(Optional.of(card));
        Customer knownCustomer = customer("0012345678");
        knownCustomer.setId(5L);
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.of(knownCustomer));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), eq(3), anyString(), eq("120"))).thenReturn("321");
        when(trackService.generateTrack1(eq(PAN), anyString())).thenReturn("t1");
        when(trackService.generateTrack2(any(UUID.class), eq(PAN), anyString())).thenReturn("t2");
        when(trackService.generateTrack3(PAN, null)).thenReturn("t3");
    }

    @Test
    void replacementDeactivatesTheOldCardAndIssuesANewOne() throws CardDomainException {
        Card old = existing(1L, CardStatus.HOT);
        stubReplacementDependencies(old);
        when(repository.save(any(Card.class))).thenAnswer(i -> {
            Card saved = i.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(new CardId(2L));
            }
            return saved;
        });

        Card replacement = service.replaceCard(1L, "operator").getCard();

        assertThat(old.getCardStatus()).isEqualTo(CardStatus.INACTIVE);
        assertThat(old.getChangeStateDescription()).startsWith("Deactivated for replacement");
        assertThat(replacement.getCardStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(replacement.getOldCardId()).isEqualTo(1L);
        assertThat(replacement.getCvv2()).isEqualTo("321");
        assertThat(replacement.getPan()).isEqualTo(PAN);
        assertThat(replacement.getIssuerPersonId()).isEqualTo("operator");
        assertThat(replacement.getMaskedPan()).isEqualTo("603799******0011");
        assertThat(replacement.getWrongPassRetryCount()).isEqualTo(3);
        assertThat(replacement.getCustomer().getId()).isEqualTo(5L);
        assertThat(replacement.getMacnaCode()).isEqualTo("M1");
    }

    @Test
    void replacementKeepsAnExistingMaskedPan() throws CardDomainException {
        Card old = existing(1L, CardStatus.INACTIVE);
        old.setMaskedPan("MASKED");
        stubReplacementDependencies(old);
        when(repository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.replaceCard(1L, "operator").getCard().getMaskedPan()).isEqualTo("MASKED");
    }

    @Test
    void replacementRequiresAProfile() throws CardDomainException {
        Card old = existing(1L, CardStatus.HOT);
        old.setCardProfileId(null);
        when(repository.findById(1L)).thenReturn(Optional.of(old));

        assertThatThrownBy(() -> service.replaceCard(1L, "operator"))
                .satisfies(t -> assertError(t, StatusService.CARD_PROFILE_NOT_FOUND, HttpStatus.OK));
    }

    @Test
    void replacementLoadsUnknownCustomersFromPartyAndFailsWhenTheyCannotBeSaved() throws CardDomainException {
        Card old = existing(1L, CardStatus.HOT);
        when(repository.findById(1L)).thenReturn(Optional.of(old));
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.empty());
        when(partyGateway.loadCustomerByNationalId("0012345678")).thenReturn(customer("0012345678"));
        when(customerRepository.save(any(Customer.class))).thenReturn(null);

        assertThatThrownBy(() -> service.replaceCard(1L, "operator"))
                .satisfies(t -> assertError(t, StatusService.PARTY_SERVICE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void replacementTreatsACustomerWithoutIdAsUnknown() throws CardDomainException {
        Card old = existing(1L, CardStatus.HOT);
        when(repository.findById(1L)).thenReturn(Optional.of(old));
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.of(customer("0012345678")));
        Customer fromParty = customer("0012345678");
        when(partyGateway.loadCustomerByNationalId("0012345678")).thenReturn(fromParty);
        Customer savedWithoutId = customer("0012345678");
        when(customerRepository.save(fromParty)).thenReturn(savedWithoutId);

        assertThatThrownBy(() -> service.replaceCard(1L, "operator")).hasMessageContaining("Failed to save customer");
    }

    @Test
    void replacementSavesLoadedCustomerBeforeIssuing() throws CardDomainException {
        Card old = existing(1L, CardStatus.HOT);
        stubReplacementDependencies(old);
        Customer saved = customer("0012345678");
        saved.setId(8L);
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.empty());
        when(partyGateway.loadCustomerByNationalId("0012345678")).thenReturn(customer("0012345678"));
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);
        when(repository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.replaceCard(1L, "operator").getCard().getCustomer().getId()).isEqualTo(8L);
    }

    @Test
    void replacementValidatesTheGeneratedCvv2() throws CardDomainException {
        Card old = existing(1L, CardStatus.HOT);
        when(repository.findById(1L)).thenReturn(Optional.of(old));
        Customer known = customer("0012345678");
        known.setId(5L);
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.of(known));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(null, null, true)));
        when(repository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), eq(4), anyString(), eq("120"))).thenReturn("12");

        assertThatThrownBy(() -> service.replaceCard(1L, "operator"))
                .satisfies(t -> assertError(t, StatusService.INVALID_CVV2_LENGTH, HttpStatus.OK));
    }

    // ------------------------------------------------------------------ reissue

    @Test
    void reissueCreatesAFreshCardWithNewCvv2() throws CardDomainException {
        Card old = existing(1L, CardStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(old));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), eq(3), anyString(), eq("120"))).thenReturn("777");
        when(trackService.generateTrack1(eq(PAN), anyString())).thenReturn("t1");
        when(trackService.generateTrack2(any(UUID.class), eq(PAN), anyString())).thenReturn("t2");
        when(trackService.generateTrack3(PAN, null)).thenReturn("t3");
        stubSave();

        Card issued = service.reissueCard(old).getCard();

        assertThat(issued.getCvv2()).isEqualTo("777");
        assertThat(issued.getCardStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(issued.getWrongPassRetryCount()).isEqualTo(3);
        assertThat(issued.getMaskedPan()).isEqualTo("603799******0011");
    }

    @Test
    void reissueKeepsTheExistingMaskedPanAndHandlesNullNames() throws CardDomainException {
        Card old = existing(1L, CardStatus.ACTIVE);
        old.setMaskedPan("MASKED");
        old.setCustomer(Customer.builder().nationalId("0012345678").build());
        when(repository.findById(1L)).thenReturn(Optional.of(old));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), eq(3), anyString(), eq("120"))).thenReturn("777");
        stubSave();

        assertThat(service.reissueCard(old).getCard().getMaskedPan()).isEqualTo("MASKED");
    }

    @Test
    void reissueValidatesCardData() throws CardDomainException {
        Card noPan = existing(1L, CardStatus.ACTIVE);
        noPan.setPan(null);
        Card noExpiry = existing(2L, CardStatus.ACTIVE);
        noExpiry.setExpDate(null);
        Card badExpiry = existing(3L, CardStatus.ACTIVE);
        badExpiry.setExpDate("not-a-date");
        Card noProfile = existing(4L, CardStatus.ACTIVE);
        noProfile.setCardProfileId(null);
        Card unknownProfile = existing(5L, CardStatus.ACTIVE);
        for (Card card : List.of(noPan, noExpiry, noProfile, unknownProfile)) {
            when(repository.findById(card.getId().getValue())).thenReturn(Optional.of(card));
        }
        when(profileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reissueCard(null)).hasMessageContaining("Card id is required to reissue");
        assertThatThrownBy(() -> service.reissueCard(noPan)).hasMessageContaining("PAN and expiration date are required");
        assertThatThrownBy(() -> service.reissueCard(noExpiry)).hasMessageContaining("PAN and expiration date are required");
        assertThatThrownBy(() -> service.reissueCard(noProfile)).hasMessageContaining("Card profile is required");
        assertThatThrownBy(() -> service.reissueCard(unknownProfile)).hasMessageContaining("CardProfile not found: 1");
    }

    @Test
    void reissueRejectsAnInvalidExpirationDateFormat() throws CardDomainException {
        Card badExpiry = existing(3L, CardStatus.ACTIVE);
        badExpiry.setExpDate("not-a-date");
        when(repository.findById(3L)).thenReturn(Optional.of(badExpiry));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));

        assertThatThrownBy(() -> service.reissueCard(badExpiry)).hasMessageContaining("Invalid expiration date format");
    }

    @Test
    void reissueValidatesTheGeneratedCvv2() throws CardDomainException {
        Card old = existing(1L, CardStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(old));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 0, true)));
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), eq(4), anyString(), eq("120"))).thenReturn("12345");

        assertThatThrownBy(() -> service.reissueCard(old))
                .satisfies(t -> assertError(t, StatusService.INVALID_CVV2_LENGTH, HttpStatus.OK));
    }

    // ------------------------------------------------------------------ renew

    private void stubRenewDependencies(Card card, CardProfile profile) throws CardDomainException {
        when(repository.findById(card.getId().getValue())).thenReturn(Optional.of(card));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), anyInt(), anyString(), eq("120"))).thenReturn("123");
        when(ssmService.getPin1(any(UUID.class), eq("0012345678"), eq(PAN))).thenReturn("1111");
        when(ssmService.getPin2(any(UUID.class), eq("0012345678"), eq(PAN))).thenReturn("2222");
        when(trackService.generateTrack1(eq(PAN), anyString())).thenReturn("t1");
        when(trackService.generateTrack2(any(UUID.class), eq(PAN), anyString())).thenReturn("t2");
        when(trackService.generateTrack3(PAN, null)).thenReturn("t3");
        Customer known = customer("0012345678");
        known.setId(5L);
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.of(known));
        stubSave();
    }

    @Test
    void renewExtendsFromTheCurrentExpiryWhenItIsStillInTheFuture() throws CardDomainException {
        Card old = existing(1L, CardStatus.ACTIVE);
        LocalDate expiry = LocalDate.now().plusMonths(2);
        old.setExpDate(expiry.toString());
        stubRenewDependencies(old, profile(12, 3, true));

        Card renewed = service.renewCard(old).getCard();

        assertThat(renewed.getExpDate()).isEqualTo(expiry.plusMonths(12).toString());
        assertThat(renewed.getCardStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(renewed.getCustomer().getId()).isEqualTo(5L);
        assertThat(renewed.getMaskedPan()).isEqualTo("603799******0011");
    }

    @Test
    void renewExtendsFromTodayWhenTheCardAlreadyExpired() throws CardDomainException {
        Card old = existing(1L, CardStatus.EXPIRED);
        old.setExpDate(LocalDate.now().minusMonths(2).toString());
        old.setMaskedPan("MASKED");
        old.setCustomer(Customer.builder().nationalId("0012345678").build());
        stubRenewDependencies(old, profile(null, 3, true));

        Card renewed = service.renewCard(old).getCard();

        assertThat(renewed.getExpDate()).isEqualTo(LocalDate.now().plusMonths(36).toString());
        assertThat(renewed.getMaskedPan()).isEqualTo("MASKED");
    }

    @Test
    void renewValidatesCardStateAndExpiry() throws CardDomainException {
        Card hot = existing(1L, CardStatus.HOT);
        Card blankExpiry = existing(2L, CardStatus.ACTIVE);
        blankExpiry.setExpDate(" ");
        Card nullExpiry = existing(3L, CardStatus.ACTIVE);
        nullExpiry.setExpDate(null);
        Card unparsable = existing(4L, CardStatus.ACTIVE);
        unparsable.setExpDate("31/12/2030");
        Card farFuture = existing(5L, CardStatus.ACTIVE);
        farFuture.setExpDate(LocalDate.now().plusYears(2).toString());
        for (Card card : List.of(hot, blankExpiry, nullExpiry, unparsable, farFuture)) {
            when(repository.findById(card.getId().getValue())).thenReturn(Optional.of(card));
        }
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));

        assertThatThrownBy(() -> service.renewCard(null)).hasMessageContaining("Card id is required to renew");
        assertThatThrownBy(() -> service.renewCard(hot)).hasMessageContaining("Hot cards cannot be renewed");
        assertThatThrownBy(() -> service.renewCard(blankExpiry)).hasMessageContaining("expiration date is invalid");
        assertThatThrownBy(() -> service.renewCard(nullExpiry)).hasMessageContaining("expiration date is invalid");
        assertThatThrownBy(() -> service.renewCard(unparsable)).hasMessageContaining("expiration date is invalid");
        assertThatThrownBy(() -> service.renewCard(farFuture)).hasMessageContaining("more than 3 months");
    }

    @Test
    void renewValidatesTheGeneratedCvv2() throws CardDomainException {
        Card old = existing(1L, CardStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(old));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile(12, 3, true)));
        when(ssmService.getCvv2(any(UUID.class), eq(PAN), eq(3), anyString(), eq("120"))).thenReturn("1234");

        assertThatThrownBy(() -> service.renewCard(old))
                .satisfies(t -> assertError(t, StatusService.INVALID_CVV2_LENGTH, HttpStatus.OK));
    }

    // ------------------------------------------------------------------ access file and checks

    @Test
    void accessFileRequiresPrintableCards() {
        when(repository.findPrintableCards()).thenReturn(null).thenReturn(List.of());

        assertThatThrownBy(() -> service.generateAccessFile())
                .satisfies(t -> assertError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.generateAccessFile())
                .satisfies(t -> assertError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
    }

    @Test
    void accessFileIsExportedForPrintableCards() throws CardDomainException {
        List<Card> cards = List.of(existing(1L, CardStatus.ISSUED));
        when(repository.findPrintableCards()).thenReturn(cards);
        when(exporter.export(cards)).thenReturn(AccessFileResult.builder().filePath(Path.of("/tmp/a.csv")).fileName("a.csv")
                .downloadUrl("/files/a.csv").recordCount(1).trackCount(3).customerCount(1).build());

        AccessFileResponse response = service.generateAccessFile();

        assertThat(response.getFileName()).isEqualTo("a.csv");
        assertThat(response.getDownloadUrl()).isEqualTo("/files/a.csv");
        assertThat(response.getRecordCount()).isEqualTo(1);
        assertThat(response.getTrackCount()).isEqualTo(3);
        assertThat(response.getCustomerCount()).isEqualTo(1);
    }

    @Test
    void existenceChecksReturnTrueOrReportMissingCards() throws CardDomainException {
        when(repository.existsByCardNumber(PAN)).thenReturn(true);
        when(repository.existsByCardNumber("0000")).thenReturn(false);
        when(repository.existsByProfileId(1L)).thenReturn(true);
        when(repository.existsByProfileId(2L)).thenReturn(false);

        assertThat(service.checkExistCardByCardNumber(PAN)).isTrue();
        assertThat(service.existsByProfileId(1L)).isTrue();
        assertThatThrownBy(() -> service.checkExistCardByCardNumber("0000"))
                .satisfies(t -> assertError(t, StatusService.CARD_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.existsByProfileId(2L))
                .satisfies(t -> assertError(t, StatusService.CARD_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST));
    }

    @Test
    void captorsAreUsableForSavedCards() throws CardDomainException {
        stubCreateDependencies(profile(12, 3, true), "123");
        stubCreateRest();
        stubSave();

        service.createCard(newCardRequest("0012345678"));

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCardUuid()).isNotNull();
        assertThat(captor.getValue().getCustomer().getNationalId()).isEqualTo("0012345678");
    }
}
