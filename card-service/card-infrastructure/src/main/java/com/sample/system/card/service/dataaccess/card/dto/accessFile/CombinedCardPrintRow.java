package com.sample.system.card.service.dataaccess.card.dto.accessFile;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CombinedCardPrintRow {
    // Card columns
    Long cardId;
    String pan;
    String maskedPan;
    String status;
    Long profileId;
    String issueDate;
    String expDate;
    String unitCode;
    String unitName;

    // Track columns
    String track1;
    String track2;
    String track3;
    String pin1;
    String pin2;

    // Customer columns
    Long customerId;
    String customerNationalId;
    String firstName;
    String lastName;
    String address;
}
