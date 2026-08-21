package com.sample.system.card.service.dataaccess.card.adapter.externalService.party;

import com.sample.system.card.service.dataaccess.thirdparty.party.PartyResponseParser;
import com.sample.system.card.service.domain.entity.Customer;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.externalService.PartyGateway;
import com.sample.system.card.service.domain.ports.output.PartyServiceRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class PartyRestAdapter implements PartyGateway {

    private final PartyServiceRestClient restClient;
    private final PartyResponseParser parser;

    @Override
    public String findByNationalId(String nationalId) throws CardDomainException {
        return "";
    }

    @Override
    public boolean existsByNationalId(String nationalId) throws CardDomainException {
        try {
            String response = restClient.getIndividualInquiry(nationalId);
            return parser.exists(response, nationalId);

        } catch (CardDomainException e) {
            // already domain-safe → just rethrow
            throw e;

        } catch (Exception e) {
            log.error("Error while validating party existence - nationalId={}", nationalId, e);
            throw new CardDomainException(
                    "خطا در ارتباط با سرویس Party",
                    StatusService.PARTY_SERVICE_ERROR,
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    @Override
    public Customer loadCustomerByNationalId(String nationalId) throws CardDomainException {
        try {
            String response = restClient.getIndividualInquiry(nationalId);
            return parser.parseCustomer(response, nationalId);

        } catch (CardDomainException e) {
            // domain-level exception from parser → pass through
            throw e;

        } catch (Exception e) {
            log.error("Error while loading customer from party-service - nationalId={}", nationalId, e);
            throw new CardDomainException(
                    "خطا در دریافت اطلاعات مشتری از سرویس Party",
                    StatusService.PARTY_SERVICE_ERROR,
                    HttpStatus.BAD_GATEWAY
            );
        }
    }
}

