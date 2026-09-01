package com.sample.system.card.service.dataaccess.card.adapter.externalService;

import com.sample.system.card.service.domain.ports.output.externalService.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
    @Override
    public Boolean isInactivableCard(Long cardId) {
        return true;
    }
}
