package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.BankCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardRequestCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CustomerCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.FeeCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.ReasonCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.TrackCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.IssueCardRequestEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.ReplacementCardRequestEntity;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.entity.Customer;
import com.sample.system.card.service.domain.entity.Fee;
import com.sample.system.card.service.domain.entity.FileDownload;
import com.sample.system.card.service.domain.entity.Track;
import com.sample.system.card.service.domain.enums.CardPhysicalStatus;
import com.sample.system.card.service.domain.enums.CardRenewalType;
import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.domain.enums.CardRequestType;
import com.sample.system.card.service.domain.enums.CardStatus;
import com.sample.system.card.service.domain.enums.FeePeriod;
import com.sample.system.card.service.domain.valueObject.BankId;
import com.sample.system.card.service.domain.valueObject.CardId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import com.sample.system.card.service.domain.valueObject.CardRequestId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringJUnitConfig(DataAccessMapperTest.MapperConfig.class)
class DataAccessMapperTest {

    @Configuration
    @ComponentScan(basePackages = "com.sample.system.card.service.dataaccess.card.mapper",
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*(MapperImpl|MappingsImpl)"))
    static class MapperConfig {
    }

    @Autowired
    private BankDataAccessMapper bankMapper;
    @Autowired
    private CardDataAccessMapper cardMapper;
    @Autowired
    private CardProfileDataAccessMapper profileMapper;
    @Autowired
    private CardRequestDataAccessMapper requestMapper;
    @Autowired
    private FeeDataAccessMapper feeMapper;
    @Autowired
    private FileDownloadDataAccessMapper fileDownloadMapper;

    @Nested
    class BankMapping {

        @Test
        void entityToDomainKeepsIdAndFields() {
            BankCommandEntity entity = BankCommandEntity.builder().id(3L).binCode("603799").name("Melli").isActive(true).build();

            Bank bank = bankMapper.toDomain(entity);

            assertThat(bank.getId().getValue()).isEqualTo(3L);
            assertThat(bank.getBinCode()).isEqualTo("603799");
            assertThat(bank.getIsActive()).isTrue();
        }

        @Test
        void domainToEntityDoesNotCopyAuditFields() {
            Bank bank = Bank.builder().binCode("603799").name("Melli").createdBy("someone").build();
            bank.setId(new BankId(3L));

            BankCommandEntity entity = bankMapper.toEntity(bank);

            assertThat(entity.getId()).isEqualTo(3L);
            assertThat(entity.getCreatedBy()).isNull();
        }

        @Test
        void nullInNullOut() {
            assertThat(bankMapper.toDomain(null)).isNull();
            assertThat(bankMapper.toEntity(null)).isNull();
        }
    }

    @Nested
    class CardMapping {

        @Test
        void toDomainAppliesDefaultsAndDerivedValues() {
            CardProfileCommandEntity profile = CardProfileCommandEntity.builder().id(11L).build();
            CardCommandEntity entity = CardCommandEntity.builder()
                    .id(1L).pan("6037991234567890").cardStatus(CardStatus.ACTIVE)
                    .physicalStatus(CardPhysicalStatus.PRINTED).cardProfile(profile)
                    .track(TrackCommandEntity.builder().track1("unsaved").build())
                    .customer(CustomerCommandEntity.builder().id(5L).nationalId("0012345678").build())
                    .build();

            Card card = cardMapper.toDomain(entity);

            assertThat(card.getId().getValue()).isEqualTo(1L);
            assertThat(card.getCardStatus()).isEqualTo(CardStatus.ACTIVE);
            assertThat(card.getCardPhysicalStatus()).isEqualTo(CardPhysicalStatus.PRINTED);
            assertThat(card.getCardProfileId().getValue()).isEqualTo(11L);
            assertThat(card.getMaskedPan()).isEqualTo("603799******7890");
            assertThat(card.getCreditLimit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(card.getTrack()).as("track without id is not mapped").isNull();
            assertThat(card.getCustomer().getNationalId()).isEqualTo("0012345678");
        }

        @Test
        void toDomainWithoutProfileHasNoProfileId() {
            Card card = cardMapper.toDomain(CardCommandEntity.builder().id(1L).pan("6037991234567890").build());

            assertThat(card.getCardProfileId()).isNull();
            assertThat(card.getCardProfile()).isNull();
        }

        @Test
        void toEntityLinksTrackAndUsesProfileReference() {
            Card card = Card.builder()
                    .pan("6037991234567890").cardStatus(CardStatus.ISSUED)
                    .cardProfileId(new CardProfileId(11L))
                    .track(Track.builder().track1("T1").build())
                    .customer(Customer.builder().firstName("no national id").build())
                    .createdBy("someone")
                    .build();
            card.setId(new CardId(1L));

            CardCommandEntity entity = cardMapper.toEntity(card);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getCardProfile().getId()).isEqualTo(11L);
            assertThat(entity.getTrack().getCard()).isSameAs(entity);
            assertThat(entity.getCustomer()).as("customer without national id is skipped").isNull();
            assertThat(entity.getMaskedPan()).isEqualTo("603799******7890");
            assertThat(entity.getCreatedBy()).isNull();
            assertThat(entity.getRequests()).isNotNull().isEmpty();
        }
    }

    @Nested
    class CardProfileMapping {

        @Test
        void toDomainDerivesRelatedIdsAndSkipsDomainValidation() {
            CardProfileCommandEntity entity = CardProfileCommandEntity.builder()
                    .id(2L).profileName("Gold").validityPeriod(0)
                    .cardRenewalType(CardRenewalType.RENEW_WITH_PRINT)
                    .issuingBank(BankCommandEntity.builder().id(9L).binCode("603799").build())
                    .build();

            assertThatCode(() -> profileMapper.toDomain(entity))
                    .as("loading stored data must not run validating setters")
                    .doesNotThrowAnyException();
            CardProfile profile = profileMapper.toDomain(entity);

            assertThat(profile.getId().getValue()).isEqualTo(2L);
            assertThat(profile.getBankId().getValue()).isEqualTo(9L);
            assertThat(profile.getBank().getBinCode()).isEqualTo("603799");
            assertThat(profile.getCardRenewalType()).isEqualTo(CardRenewalType.RENEW_WITH_PRINT);
            assertThat(profile.getFeeProfileId()).isNull();
        }
    }

    @Nested
    class CardRequestMapping {

        @Test
        void issueRequestKeepsTypeSpecificFields() {
            IssueCardRequestEntity entity = new IssueCardRequestEntity();
            entity.setId(4L);
            entity.setRequestStatus(CardRequestStatus.APPROVED);
            entity.setCaseNumber("CASE-1");
            entity.setCardProfile(CardProfileCommandEntity.builder().id(2L).build());

            CardRequest request = requestMapper.toDomain(entity);

            assertThat(request.getId().getValue()).isEqualTo(4L);
            assertThat(request.getRequestType()).isEqualTo(CardRequestType.CARD_ISSUANCE);
            assertThat(request.getRequestStatus()).isEqualTo(CardRequestStatus.APPROVED);
            assertThat(request.getCaseNumber()).isEqualTo("CASE-1");
            assertThat(request.getCardProfileId().getValue()).isEqualTo(2L);
        }

        @Test
        void replacementRequestKeepsReason() {
            ReplacementCardRequestEntity entity = new ReplacementCardRequestEntity();
            entity.setId(5L);
            entity.setOldCardId(77L);
            entity.setReason(ReasonCommandEntity.builder().id(8L).title("Lost").build());

            CardRequest request = requestMapper.toDomain(entity);

            assertThat(request.getRequestType()).isEqualTo(CardRequestType.CARD_REPLACEMENT);
            assertThat(request.getReasonId()).isEqualTo(8L);
            assertThat(request.getReason().getId().getValue()).isEqualTo(8L);
            assertThat(request.getOldCardId()).isEqualTo(77L);
        }

        @Test
        void baseEntityOnlyCarriesIdAndStatus() {
            CardRequest request = CardRequest.builder()
                    .requestStatus(CardRequestStatus.INITIAL).nationalId("0012345678").build();
            request.setId(new CardRequestId(6L));

            CardRequestCommandEntity entity = requestMapper.toEntity(request);

            assertThat(entity.getId()).isEqualTo(6L);
            assertThat(entity.getRequestStatus()).isEqualTo(CardRequestStatus.INITIAL);
            assertThat(entity.getNationalId()).isNull();
        }

        @Test
        void cardIdTakesPrecedenceOverCardObject() {
            Card card = Card.builder().build();
            card.setId(new CardId(1L));
            CardRequest request = CardRequest.builder().cardId(new CardId(2L)).card(card).build();

            IssueCardRequestEntity entity = requestMapper.toIssueCardRequestEntity(request);

            assertThat(entity.getCard().getId()).isEqualTo(2L);
        }
    }

    @Nested
    class SmallMappings {

        @Test
        void feePeriodIsCodeInDomainAndEnumInEntity() {
            FeeCommandEntity entity = new FeeCommandEntity();
            entity.setFeePeriod(FeePeriod.MONTHLY);

            Fee fee = feeMapper.toDomain(entity);
            assertThat(fee.getFeePeriod()).isEqualTo(FeePeriod.MONTHLY.getCode());

            assertThat(feeMapper.toEntity(fee).getFeePeriod()).isEqualTo(FeePeriod.MONTHLY);
        }

        @Test
        void fileDownloadIsUsedDefaultsToFalse() {
            FileDownload download = FileDownload.builder().hashId("abc").build();

            assertThat(fileDownloadMapper.toEntity(download).getIsUsed()).isFalse();
        }
    }
}
