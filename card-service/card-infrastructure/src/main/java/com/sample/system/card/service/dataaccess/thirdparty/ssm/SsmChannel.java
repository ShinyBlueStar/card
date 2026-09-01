package com.sample.system.card.service.dataaccess.thirdparty.ssm;

import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.exception.CardDomainException;

import java.util.UUID;

/**
 * Interface برای ارتباط با SSM Service (REST) – session، generate و validate.
 * تنها نقطهٔ فراخوانی HTTP به SSM؛ همهٔ adapterها از این کانال استفاده می‌کنند.
 */
public interface SsmChannel {
    UUID createSession(UUID cardId) throws ThirdPartyException, CardDomainException;

    void invalidateSession(UUID sessionId) throws ThirdPartyException, CardDomainException;

    String callGenerate(CardSecret cardSecret) throws ThirdPartyException, CardDomainException;

    String callValidate(CardSecret cardSecret) throws ThirdPartyException, CardDomainException;
}
