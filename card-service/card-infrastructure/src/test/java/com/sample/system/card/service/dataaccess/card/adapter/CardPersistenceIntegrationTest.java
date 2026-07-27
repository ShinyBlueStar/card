package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.BankCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.repository.BankJpaRepository;
import com.sample.system.card.service.dataaccess.card.repository.CardCommandJpaRepository;
import com.sample.system.card.service.dataaccess.card.repository.CardProfileJpaRepository;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.enums.CardIssueMethod;
import com.sample.system.card.service.domain.enums.CardRenewalType;
import com.sample.system.card.service.domain.enums.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real entities, converters, mappers and repository adapters against an in-memory H2 database.
 */
@DataJpaTest
@Import({BankRepositoryImpl.class, CardRepositoryImpl.class})
@ComponentScan(basePackages = "com.sample.system.card.service.dataaccess.card.mapper",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*(MapperImpl|MappingsImpl)"))
class CardPersistenceIntegrationTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private BankJpaRepository bankJpaRepository;
    @Autowired
    private CardProfileJpaRepository profileJpaRepository;
    @Autowired
    private CardCommandJpaRepository cardJpaRepository;
    @Autowired
    private BankRepositoryImpl bankRepository;
    @Autowired
    private CardRepositoryImpl cardRepository;

    private CardProfileCommandEntity profile;

    @BeforeEach
    void setUp() {
        BankCommandEntity bank = bankJpaRepository.save(
                BankCommandEntity.builder().binCode("603799").name("Melli").isActive(true).build());
        profile = profileJpaRepository.save(CardProfileCommandEntity.builder()
                .profileName("Gold").issuingBank(bank).isActive(true)
                .cardIssueMethod(CardIssueMethod.VIRTUAL)
                .cardRenewalType(CardRenewalType.RENEW_WITH_PRINT)
                .build());
    }

    private CardCommandEntity card(String pan, CardStatus status) {
        return cardJpaRepository.save(CardCommandEntity.builder()
                .pan(pan).cardStatus(status).cardProfile(profile).build());
    }

    @Test
    void enumsAreStoredAsTheirLegacyCodes() {
        card("6037991200000011", CardStatus.ACTIVE);
        em.flush();

        assertThat(jdbc.queryForObject("SELECT CURRENT_STATUS FROM cards", Integer.class))
                .isEqualTo(CardStatus.ACTIVE.getCode());
        assertThat(jdbc.queryForObject("SELECT ISSUE_METHOD FROM card_profiles", Integer.class))
                .isEqualTo(CardIssueMethod.VIRTUAL.getCode());
        assertThat(jdbc.queryForObject("SELECT CARD_RENEWAL_TYPE FROM card_profiles", String.class))
                .isEqualTo("2");
    }

    @Test
    void storedCodesAreReadBackAsEnums() {
        Long id = card("6037991200000011", CardStatus.HOT).getId();
        em.flush();
        em.clear();

        Card loaded = cardRepository.findById(id).orElseThrow();

        assertThat(loaded.getCardStatus()).isEqualTo(CardStatus.HOT);
        assertThat(loaded.getCardProfileId().getValue()).isEqualTo(profile.getId());
        assertThat(loaded.getMaskedPan()).isEqualTo("603799******0011");
    }

    @Test
    void searchFiltersByStatusCode() {
        card("6037991200000011", CardStatus.ACTIVE);
        card("6037991200000029", CardStatus.HOT);
        card("6037991200000037", CardStatus.ACTIVE);
        em.flush();

        Page<Card> active = cardRepository.findAllCards(Map.of("cardStatus", String.valueOf(CardStatus.ACTIVE.getCode())));
        Page<Card> unknown = cardRepository.findAllCards(Map.of("cardStatus", "999"));

        assertThat(active.getContent()).extracting(Card::getCardStatus).containsOnly(CardStatus.ACTIVE).hasSize(2);
        assertThat(unknown.getContent()).as("an unknown status code must not match anything").isEmpty();
    }

    @Test
    void existsByProfileId() {
        assertThat(cardRepository.existsByProfileId(profile.getId())).isFalse();

        card("6037991200000011", CardStatus.ISSUED);

        assertThat(cardRepository.existsByProfileId(profile.getId())).isTrue();
    }

    @Test
    void bankAdapterSavesAndFindsByBinCode() {
        Bank saved = bankRepository.save(Bank.builder().binCode("610433").name("Mellat").isActive(true).build());
        em.flush();
        em.clear();

        assertThat(saved.getId()).isNotNull();
        assertThat(bankRepository.findByBinCode("610433"))
                .hasValueSatisfying(bank -> assertThat(bank.getName()).isEqualTo("Mellat"));
        assertThat(bankRepository.findByBinCode("000000")).isEmpty();
    }
}
