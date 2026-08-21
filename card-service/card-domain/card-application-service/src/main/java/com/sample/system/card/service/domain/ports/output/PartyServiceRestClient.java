package com.sample.system.card.service.domain.ports.output;

/**
 * REST client port for calling party-service
 * Following Hexagonal Architecture - Output Port
 */
public interface PartyServiceRestClient {

    /**
     * Get individual party inquiry by national code
     * @param nationalCode National code of the party
     * @return JSON string response from party-service
     * @throws Exception if REST call fails
     */
    String getIndividualInquiry(String nationalCode) throws Exception;

    /**
     * Get business party inquiry by national code
     * @param nationalCode National code of the party
     * @return JSON string response from party-service
     * @throws Exception if REST call fails
     */
    String getBusinessInquiry(String nationalCode) throws Exception;
}

