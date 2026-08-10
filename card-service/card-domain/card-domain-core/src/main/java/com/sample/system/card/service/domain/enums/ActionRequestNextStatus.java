package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActionRequestNextStatus implements CodedEnum {

    APPROVED(2, "تایید درخواست"),
    ISSUED(4, "صدور کارت"),
    PRINT_EXPECT(5, "ارسال برای چاپ"),
    PRINTED(6, "چاپ"),
    CANCELED(7, "رد درخواست"),
    FIle_DOWNLOAD(8, "دانلود فایل چاپ کارت");

    private final Integer code;
    private final String description;

    public static ActionRequestNextStatus fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("CardRequestStatus", "null", "Code cannot be null");
        }
        for (ActionRequestNextStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new InvalidInputParameterException("CardIssueMethod", code.toString(), "Invalid Card Issue Method code");
    }

}
