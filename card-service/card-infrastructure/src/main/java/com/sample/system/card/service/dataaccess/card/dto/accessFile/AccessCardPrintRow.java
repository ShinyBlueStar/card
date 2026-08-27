package com.sample.system.card.service.dataaccess.card.dto.accessFile;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccessCardPrintRow {
    // ID
    Long id;

    // PAN parts (4 segments)
    String panPart1;
    String panPart2;
    String panPart3;
    String panPart4;

    // CVV2
    String cvv2;

    // Tracks
    String track1;
    String track2;
    String track3;

    // Name fields
    String fullName;
    String firstName;
    String lastName;

    // Date fields
    String exDate;  // MM/YY format
    String pan;  // Full PAN
    String firstStatementDate;  // yyyy/MM/dd format

    // Address fields
    String address;
    String address1;
    String address2;
    String address3;
    String zipCode;

    // Additional fields
    String barcode;
    String cellNumber;

    // Print control fields
    Integer printCount;
    String printCommand;

    // Log fields
    String lDate;  // Current date
    String lTime;  // Current time

    // National ID
    String nationalId;
}