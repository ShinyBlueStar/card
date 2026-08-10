package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum CardRequestStatus implements CodedEnum {

    INITIAL(1, "در انتظار تایید"),
    APPROVED(2, "در انتظار صدور"),
//    UNPROCESSED(3, "پردازش نشده"),
    FINISHED(3, "پایان یافته"),
    ISSUED(4, "صادر شده"),
    PRINT_EXPECT(5, "در انتظار چاپ"),
    PRINTED(6, "چاپ شده"),
    CANCELED(7, "رد شده");

    private final Integer code;
    private final String description;

    public static CardRequestStatus fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("CardRequestStatus", "null", "Code cannot be null");
        }
        for (CardRequestStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new InvalidInputParameterException("CardIssueMethod", code.toString(), "Invalid Card Issue Method code");
    }

    public static List<CardRequestStatus> busyStatuses(){
        return List.of(CardRequestStatus.INITIAL,
                CardRequestStatus.APPROVED,
                CardRequestStatus.ISSUED,
                CardRequestStatus.PRINT_EXPECT,
                CardRequestStatus.PRINTED);
    }

    public static List<ActionRequestNextStatus> resolveNextStatuses(
            CardRequestStatus currentStatus,
            CardProfile profile) {

        if (currentStatus == null) {
            throw new IllegalArgumentException("currentStatus cannot be null");
        }

        return switch (currentStatus) {

            case INITIAL -> List.of(
                    ActionRequestNextStatus.APPROVED,
                    ActionRequestNextStatus.CANCELED
            );

            case APPROVED -> List.of(
                    ActionRequestNextStatus.ISSUED
            );

            case ISSUED -> {
                if (profile != null
                        && CardIssueMethod.PHYSICAL.equals(profile.getCardIssueMethod())) {
                    yield List.of(ActionRequestNextStatus.PRINT_EXPECT);
                }else
                    yield List.of();
            }

            case PRINT_EXPECT -> List.of(
                    ActionRequestNextStatus.PRINTED
            );

            case PRINTED,
                 FINISHED,
                 CANCELED -> List.of(); // terminal states

            default -> throw new IllegalStateException(
                    "Unsupported CardRequestStatus: " + currentStatus
            );
        };
    }
}
