package com.sample.system.card.service.domain.command;

import com.sample.system.card.service.domain.command.bank.CreateBankCommand;
import com.sample.system.card.service.domain.command.card.ChangeCardStatusCommand;
import com.sample.system.card.service.domain.command.numberPattern.CreateCardNumberPatternCommand;
import com.sample.system.card.service.domain.enums.CardStatusAction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommandValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validBankCommandHasNoViolations() {
        assertThat(validator.validate(new CreateBankCommand("603799", "Melli", true))).isEmpty();
    }

    @Test
    void bankBinMustBeSixDigitsAndNameRequired() {
        Set<ConstraintViolation<CreateBankCommand>> violations =
                validator.validate(new CreateBankCommand("60379", " ", true));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("binCode", "name");
    }

    @ParameterizedTest
    @EnumSource(value = CardStatusAction.class, names = {"BLOCK", "DEACTIVATE", "UNBLOCK"})
    void reasonIsRequiredForRestrictiveActions(CardStatusAction action) {
        assertThat(validator.validate(new ChangeCardStatusCommand(1L, action, null))).hasSize(1);
        assertThat(validator.validate(new ChangeCardStatusCommand(1L, action, 3L))).isEmpty();
    }

    @Test
    void activationDoesNotNeedReason() {
        assertThat(validator.validate(new ChangeCardStatusCommand(1L, CardStatusAction.ACTIVATE, null))).isEmpty();
    }

    @Test
    void incrementalPatternMustNotHaveRange() {
        CreateCardNumberPatternCommand withRange = CreateCardNumberPatternCommand.builder()
                .productCode("12").cardNumGenerationMethod(1).name("P1").status(1)
                .cardNumberFrom("1000000").cardNumberTo("1999999").build();
        CreateCardNumberPatternCommand withoutRange = CreateCardNumberPatternCommand.builder()
                .productCode("12").cardNumGenerationMethod(1).name("P1").status(1).build();

        assertThat(validator.validate(withRange)).hasSize(1);
        assertThat(validator.validate(withoutRange)).isEmpty();
    }

    @Test
    void rangeBasedPatternNeedsSevenDigitBoundsAndValidStatus() {
        CreateCardNumberPatternCommand command = CreateCardNumberPatternCommand.builder()
                .productCode("12").cardNumGenerationMethod(2).name("P2").status(3)
                .cardNumberFrom("123").build();

        assertThat(validator.validate(command)).hasSize(3);
    }
}
