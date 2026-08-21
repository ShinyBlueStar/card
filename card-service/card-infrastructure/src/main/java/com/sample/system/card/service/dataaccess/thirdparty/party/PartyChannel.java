package com.sample.system.card.service.dataaccess.thirdparty.party;

import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;

/**
 * Interface برای ارتباط با Party Service (REST)
 */
public interface PartyChannel {

    /**
     * دریافت اطلاعات Party حقیقی
     * GET /api/v1/party/individual/info/{nationalCode}
     */
    String getPartyIndividual(String nationalCode) throws ThirdPartyException;

    /**
     * دریافت اطلاعات Party حقوقی
     * GET /api/v1/party/business/info/{nationalCode}
     */
    String getPartyBusiness(String nationalCode) throws ThirdPartyException;

    /**
     * دریافت اطلاعات Party بر اساس type
     */
    String getPartyInfo(String nationalCode, String type) throws ThirdPartyException;

}