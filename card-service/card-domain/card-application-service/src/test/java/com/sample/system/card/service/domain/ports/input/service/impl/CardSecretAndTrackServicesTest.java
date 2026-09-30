package com.sample.system.card.service.domain.ports.input.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.SecretType;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.externalService.SSMService;
import com.sample.system.card.service.domain.ports.output.repository.CardRepository;
import com.sample.system.card.service.domain.ports.output.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardSecretAndTrackServicesTest {

    private static final String PAN = "6037991200000011";
    private static final UUID CARD_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private CardRepository cardRepository;
    @Mock
    private SSMService ssmService;
    @Mock
    private CustomerRepository customerRepository;

    private CardSecretServiceImpl secretService;
    private TrackGenerationServiceImpl trackService;
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        secretService = new CardSecretServiceImpl(cardRepository, ssmService, new ObjectMapper());
        trackService = new TrackGenerationServiceImpl(ssmService);
        customerService = new CustomerServiceImpl(customerRepository);
    }

    private static Card card(UUID uuid, Customer customer) {
        return Card.builder().pan(PAN).cardUuid(uuid).customer(customer).build();
    }

    private static CardSecret secret(SecretType type) {
        return CardSecret.builder().pan(PAN).secretType(type).build();
    }

    private void assertSecretError(Throwable t, int status, HttpStatus http) {
        assertThat(t).isInstanceOfSatisfying(CardDomainException.class, e -> {
            assertThat(e.getStatus()).isEqualTo(status);
            assertThat(e.getHttpStatus()).isEqualTo(http);
        });
    }

    // ------------------------------------------------------------ card secret: card resolution

    @Test
    void generateFailsWhenTheCardIsUnknown() {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> secretService.generate(secret(SecretType.PIN)))
                .satisfies(t -> assertSecretError(t, StatusService.CARD_NOT_FOUND, HttpStatus.NOT_FOUND))
                .hasMessageNotContaining(PAN);
    }

    @Test
    void generateFailsWhenTheCardHasNoUuid() {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(null, null)));

        assertThatThrownBy(() -> secretService.generate(secret(SecretType.PIN)))
                .satisfies(t -> assertSecretError(t, StatusService.CARD_ID_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    // ------------------------------------------------------------ card secret: generate

    @Test
    void generateStoresTheIssuedValueAndResolvedCardData() throws Exception {
        Customer customer = Customer.builder().nationalId("0012345678").build();
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(CARD_UUID, customer)));
        CardSecret request = secret(SecretType.PIN);
        when(ssmService.callGenerate(request)).thenReturn("1234");

        CardSecret result = secretService.generate(request);

        assertThat(result.getValue()).isEqualTo("1234");
        assertThat(result.getCardId()).isEqualTo(CARD_UUID);
        assertThat(result.getNationalId()).isEqualTo("0012345678");
    }

    @Test
    void generateWorksForCardsWithoutCustomerOrNationalId() throws Exception {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(CARD_UUID, null)))
                .thenReturn(Optional.of(card(CARD_UUID, Customer.builder().build())));
        when(ssmService.callGenerate(any())).thenReturn("9999");

        CardSecret first = secretService.generate(secret(SecretType.OTP));
        CardSecret second = secretService.generate(secret(SecretType.OTP));

        assertThat(first.getNationalId()).isNull();
        assertThat(second.getNationalId()).isNull();
    }

    @Test
    void generateKeepsDomainErrorsFromTheSecretManager() throws Exception {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(CARD_UUID, null)));
        CardDomainException failure = new CardDomainException("ssm says no", StatusService.SSM_SERVICE_ERROR, HttpStatus.BAD_GATEWAY);
        when(ssmService.callGenerate(any())).thenThrow(failure);

        assertThatThrownBy(() -> secretService.generate(secret(SecretType.PIN))).isSameAs(failure);
    }

    @Test
    void generateWrapsUnexpectedFailures() throws Exception {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(CARD_UUID, null)));
        when(ssmService.callGenerate(any())).thenThrow(new IllegalStateException("socket closed"));

        assertThatThrownBy(() -> secretService.generate(secret(SecretType.PIN)))
                .satisfies(t -> assertSecretError(t, StatusService.SSM_SERVICE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
                .hasMessageContaining("SSM generate failed").hasMessageContaining("socket closed");
    }

    // ------------------------------------------------------------ card secret: validate

    @Test
    void validateReadsTheVerdictAndMessageFromTheResponse() throws Exception {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(CARD_UUID, null)));
        when(ssmService.callValidate(any())).thenReturn("{\"data\":{\"valid\":true,\"message\":\"all good\"}}");

        CardSecret result = secretService.validate(secret(SecretType.PIN));

        assertThat(result.getValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("all good");
    }

    @Test
    void validateFillsDefaultMessages() throws Exception {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(CARD_UUID, null)));
        when(ssmService.callValidate(any())).thenReturn("{\"data\":{\"valid\":true}}")
                .thenReturn("{\"data\":{\"valid\":false}}").thenReturn("{\"data\":{}}");

        CardSecret valid = secretService.validate(secret(SecretType.PIN));
        CardSecret invalid = secretService.validate(secret(SecretType.PIN));
        CardSecret unknown = secretService.validate(secret(SecretType.PIN));

        assertThat(valid.getMessage()).isEqualTo("Verification succeeded");
        assertThat(invalid.getValid()).isFalse();
        assertThat(invalid.getMessage()).isEqualTo("Verification failed");
        assertThat(unknown.getValid()).isFalse();
        assertThat(unknown.getMessage()).isEqualTo("Verification failed");
    }

    @Test
    void validateRejectsResponsesWithoutData() throws Exception {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(CARD_UUID, null)));
        when(ssmService.callValidate(any())).thenReturn("{\"status\":\"ok\"}").thenReturn("null");

        assertThatThrownBy(() -> secretService.validate(secret(SecretType.PIN)))
                .satisfies(t -> assertSecretError(t, StatusService.SSM_SERVICE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
                .hasMessageContaining("invalid response");
        assertThatThrownBy(() -> secretService.validate(secret(SecretType.PIN)))
                .satisfies(t -> assertSecretError(t, StatusService.SSM_SERVICE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void validateWrapsUnparseableResponsesAndKeepsDomainErrors() throws Exception {
        when(cardRepository.findByPan(PAN)).thenReturn(Optional.of(card(CARD_UUID, null)));
        CardDomainException failure = new CardDomainException("ssm says no", StatusService.SSM_SERVICE_ERROR, HttpStatus.BAD_GATEWAY);
        when(ssmService.callValidate(any())).thenReturn("not json").thenThrow(failure);

        assertThatThrownBy(() -> secretService.validate(secret(SecretType.OTP)))
                .satisfies(t -> assertSecretError(t, StatusService.SSM_SERVICE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
                .hasMessageContaining("SSM validate failed");
        assertThatThrownBy(() -> secretService.validate(secret(SecretType.OTP))).isSameAs(failure);
    }

    // ------------------------------------------------------------ tracks

    @Test
    void track1ContainsPanSeparatorsExpiryAndServiceCode() {
        assertThat(trackService.generateTrack1(PAN, "2912")).isEqualTo("B" + PAN + "^/^2912120");
    }

    @Test
    void track1RejectsInvalidPanAndExpiry() {
        assertThatThrownBy(() -> trackService.generateTrack1(null, "2912")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> trackService.generateTrack1("123", "2912")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> trackService.generateTrack1(PAN, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> trackService.generateTrack1(PAN, "291")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void track2RejectsInvalidPanAndExpiryBeforeCallingTheSecretManager() {
        assertThatThrownBy(() -> trackService.generateTrack2(CARD_UUID, null, "2912")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> trackService.generateTrack2(CARD_UUID, "12", "2912")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> trackService.generateTrack2(CARD_UUID, PAN, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> trackService.generateTrack2(CARD_UUID, PAN, "29")).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(ssmService);
    }

    @Test
    void track2CurrentlyFailsBecauseTheCvvLengthIsUnboxedFromNull() {
        // Known defect: generateTrack2 passes (Integer) null to SSMService.getCvv2(int ...), which cannot be unboxed.
        // Once the call is fixed this characterization test has to be replaced by real track-2 assertions.
        assertThatThrownBy(() -> trackService.generateTrack2(CARD_UUID, PAN, "2912"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void track3UsesTheDefaultOrTheSuppliedAdditionalData() {
        assertThat(trackService.generateTrack3(PAN, null)).isEqualTo("01" + PAN + "=" + "==0000000000000000000=0000000=1==1=");
        assertThat(trackService.generateTrack3(PAN, "  ")).isEqualTo("01" + PAN + "=" + "==0000000000000000000=0000000=1==1=");
        assertThat(trackService.generateTrack3(PAN, "EXTRA")).isEqualTo("01" + PAN + "=EXTRA");
    }

    @Test
    void track3IsTruncatedToTheIsoMaximumLength() {
        String track = trackService.generateTrack3(PAN, "X".repeat(200));

        assertThat(track).hasSize(107).endsWith("?");
    }

    @Test
    void track3RejectsInvalidPan() {
        assertThatThrownBy(() -> trackService.generateTrack3("1234", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> trackService.generateTrack3(null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------ customers

    @Test
    void existingCustomerIsReusedWithoutSaving() {
        Customer existing = Customer.builder().nationalId("0012345678").build();
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.of(existing));

        assertThat(customerService.checkOrSave(Customer.builder().nationalId("0012345678").build())).isSameAs(existing);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void unknownCustomerIsSaved() {
        Customer incoming = Customer.builder().nationalId("0012345678").build();
        Customer saved = Customer.builder().nationalId("0012345678").firstName("Ali").build();
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.empty());
        when(customerRepository.save(incoming)).thenReturn(saved);

        assertThat(customerService.checkOrSave(incoming)).isSameAs(saved);
    }

    @Test
    void customerIsCreatedFromACardRequest() {
        CardRequest request = CardRequest.builder().nationalId("0012345678").firstName("Ali").lastName("Rezaei").build();
        when(customerRepository.findByNationalId("0012345678")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer customer = customerService.createOrGetCustomerFromRequest(request);

        assertThat(customer.getNationalId()).isEqualTo("0012345678");
        assertThat(customer.getFirstName()).isEqualTo("Ali");
        assertThat(customer.getLastName()).isEqualTo("Rezaei");
    }
}
