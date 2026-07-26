package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.reason.CreateReasonCommand;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.response.reason.CreateReasonResponse;
import com.sample.system.card.service.domain.response.reason.GetReasonResponse;
import com.sample.system.card.service.domain.response.reason.ReasonListResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReasonDataMapper {

    public Reason createCommandToReason(CreateReasonCommand command) {
        return Reason.builder()
                .code(command.getCode())
                .title(command.getTitle())
                .reason(command.getReason())
                .groupId(command.getGroupId())
                .build();
    }

    public CreateReasonResponse reasonToCreateResponse(Reason reason, String message) {
        return CreateReasonResponse.builder()
                .reasonId(reason.getId() != null ? reason.getId().getValue() : null)
                .code(reason.getCode())
                .title(reason.getTitle())
                .reason(reason.getReason())
                .groupId(reason.getGroupId())
                .message(message)
                .build();
    }

    public GetReasonResponse reasonToGetResponse(Reason reason) {
        return new GetReasonResponse(
                reason.getId() != null ? reason.getId().getValue() : null,
                reason.getCode(),
                reason.getTitle(),
                reason.getReason(),
                reason.getGroupId());
    }

    public ReasonListResponse reasonsToGetResponses(Page<Reason> reasons) {
        if (reasons == null) return null;

        ReasonListResponse reasonListResponse = new ReasonListResponse();
        reasonListResponse.setList(reasons.getContent().stream()
                .map(this::reasonToGetResponse).toList());
        reasonListResponse.setNumber(reasons.getNumber());
        reasonListResponse.setSize(reasons.getSize());
        reasonListResponse.setTotalElements(reasons.getTotalElements());
        reasonListResponse.setTotalPages(reasons.getTotalPages());
        return reasonListResponse;
    }

    public ReasonListResponse reasonListToGetResponses(List<Reason> reasons) {
        if (reasons == null) return null;

        ReasonListResponse reasonListResponse = new ReasonListResponse();
        reasonListResponse.setList(reasons.stream()
                .map(this::reasonToGetResponse).toList());
        int size = reasons.size();
        reasonListResponse.setNumber(0);
        reasonListResponse.setSize(size);
        reasonListResponse.setTotalElements((long) size);
        reasonListResponse.setTotalPages(size > 0 ? 1 : 0);
        return reasonListResponse;
    }
}

