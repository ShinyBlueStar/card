package com.sample.system.card.service.domain.ports.output.externalService;

import jakarta.validation.constraints.NotBlank;

/**
 * Service interface for Credit module GRPC communication
 * Following Hexagonal Architecture - Input Port
 */
public interface CreditService {

    /**
     * Get Macna code from Credit module
     * @param nationalCode National code of the customer
     * @return Macna code
     */
    String getMacnaCode(@NotBlank String nationalCode);
}

