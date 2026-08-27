package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.exception.CardDomainException;
import org.springframework.core.io.Resource;

public interface FileDownloadService extends BaseService {

    Resource downloadFile(String hashId, String token, String accessLevel) throws CardDomainException;
    void deleteFile(String hashId) throws CardDomainException;
}

