package com.sample.system.card.service.dataaccess.card.dto.accessFile;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CustomerPrintRow {
    Long customerId;
    String nationalId;
    String firstName;
    String lastName;
    String address;
}
