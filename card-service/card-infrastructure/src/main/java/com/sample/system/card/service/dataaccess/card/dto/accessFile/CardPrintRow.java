package com.sample.system.card.service.dataaccess.card.dto.accessFile;

import lombok.Builder;
import lombok.Value;
@Value
@Builder
public class CardPrintRow {
    Long cardId;
    String pan;
    String maskedPan;
    String status;
    Long profileId;
    String customerNationalId;
    String issueDate;
    String expDate;
    String unitCode;
    String unitName;
}
