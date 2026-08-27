package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.FileDownloadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class FileDownloadQueryHandler {

    private final FileDownloadService fileDownloadService;

    public Resource downloadFile(String hashId, String token, String accessLevel) throws CardDomainException {
        log.info("Processing file download request - HashId: {}", hashId);
        Resource newRes = fileDownloadService.downloadFile(hashId, token, accessLevel);
        return newRes;
    }
}

