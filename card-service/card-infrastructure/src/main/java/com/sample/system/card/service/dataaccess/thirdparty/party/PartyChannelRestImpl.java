package com.sample.system.card.service.dataaccess.thirdparty.party;

import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;
import com.sample.system.card.service.dataaccess.utility.CallerUtil;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.sample.system.card.service.dataaccess.thirdparty.WebCallUtils.sendRequest;

/**
 * پیاده‌سازی PartyChannel با RestTemplate
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class PartyChannelRestImpl implements PartyChannel {

    @Value("${party.timeout-seconds:30}")
    private String timeout;

    @Value("${party.base.url:}")
    private String baseUrl;

    @Value("${party.individual.url:/api/v1/party/individual/info/}")
    private String individualUrl;

    @Value("${party.business.url:/api/v1/party/business/info/}")
    private String businessUrl;

    @Override
    public String getPartyIndividual(String nationalCode) throws ThirdPartyException {
        log.info("Calling Party Individual - NationalCode: {}", nationalCode);
        log.info("Base URL: {}", baseUrl);
        log.info("Individual URL: {}", individualUrl);
        log.info("Timeout: {} seconds", timeout);

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/vnd.api.v1+json");
            headers.put("Accept", "application/vnd.api.v1+json");

            Map<String, String> params = new HashMap<>();

            String fullUrl = baseUrl + individualUrl + nationalCode;
            log.info("   Full URL: {}", fullUrl);
            URIBuilder builder = new URIBuilder(fullUrl);

            int timeoutMs = Integer.parseInt(timeout) * 1000;
            String response = sendRequest(
                    CallerUtil.getCallerClassAndMethodName(),
                    HttpMethod.GET,
                    builder,
                    timeoutMs,
                    headers,
                    params,
                    null,
                    new int[]{HttpStatus.OK.value()}
            );

            log.info("Party Individual API call successful");
            log.debug("   Response: {}", response);

            return response;

        } catch (ThirdPartyException e) {
            log.error("ThirdParty exception: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception calling Party Individual", e);
            throw new ThirdPartyException(
                    "Error calling Party Individual",
                    StatusService.PARTY_SERVICE_ERROR,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error in calling service: " + e.getMessage()
            );
        }
    }

    @Override
    public String getPartyBusiness(String nationalCode) throws ThirdPartyException {
        log.info(" Calling Party Business - NationalCode: {}", nationalCode);

        try {

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/vnd.api.v1+json");
            headers.put("Accept", "application/vnd.api.v1+json");
            Map<String, String> params = new HashMap<>();

            String fullUrl = baseUrl + businessUrl + nationalCode;
            log.info("   Full URL: {}", fullUrl);
            URIBuilder builder = new URIBuilder(fullUrl);

            int timeoutMs = Integer.parseInt(timeout) * 1000;

            String response = sendRequest(
                    CallerUtil.getCallerClassAndMethodName(),
                    HttpMethod.GET,
                    builder,
                    timeoutMs,
                    headers,
                    params,
                    null,
                    new int[]{HttpStatus.OK.value()}
            );

            log.info("Party Business API call successful");
            log.debug("Response: {}", response);

            return response;
        } catch (ThirdPartyException e) {
            log.error("ThirdParty exception: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception calling Party Business", e);
            throw new ThirdPartyException(
                    "Error calling Party Business",
                    StatusService.PARTY_SERVICE_ERROR,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error in calling service: " + e.getMessage()
            );
        }
    }

    @Override
    public String getPartyInfo(String nationalCode, String type) throws ThirdPartyException {
        log.info("Calling Party Info - NationalCode: {}, Type: {}", nationalCode, type);

        if ("business".equalsIgnoreCase(type)) {
            return getPartyBusiness(nationalCode);
        } else {
            return getPartyIndividual(nationalCode);
        }
    }
}