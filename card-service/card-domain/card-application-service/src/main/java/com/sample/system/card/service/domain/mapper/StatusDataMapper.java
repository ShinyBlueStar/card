package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.CreateStatusCommand;
import com.sample.system.card.service.domain.command.UpdateStatusCommand;
import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.response.CreateStatusResponse;
import com.sample.system.card.service.domain.response.GetStatusResponse;
import com.sample.system.card.service.domain.response.StatusListResponse;
import com.sample.system.card.service.domain.response.UpdateStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class StatusDataMapper {

    public Status createCommandToStatus(CreateStatusCommand command) {
        return new Status(
                command.getPersianDescription(),
                command.getCode(),
                command.getDescription()
        );
    }

    public Status updateCommandToStatus(UpdateStatusCommand command) {
        return new Status(
                command.getPersianDescription(),
                command.getCode(),
                null
        );
    }

    public CreateStatusResponse statusToCreateResponse(Status status, String message) {
        return CreateStatusResponse.builder()
                .statusId(status.getId())
                .message(message)
                .build();
    }

    public UpdateStatusResponse statusToUpdateResponse(Status status, String message) {
        return UpdateStatusResponse.builder()
                .statusId(status.getId())
                .code(status.getCode())
                .persianDescription(status.getPersianDescription())
                .message(message)
                .build();
    }

    public GetStatusResponse statusToGetResponse(Status status) {
        return GetStatusResponse.builder()
                .id(status.getId())
                .code(status.getCode())
                .description(status.getDescription())
                .persianDescription(status.getPersianDescription())
                .message(null)
                .build();
    }

    public StatusListResponse statusesToGetListResponse(Page<Status> statuses) {
        if (statuses == null) return null;

        StatusListResponse statusListResponse = new StatusListResponse();
        statusListResponse.setList(statuses.getContent().stream()
                .map(this::statusToGetResponse)
                .toList());
        statusListResponse.setNumber(statuses.getNumber());
        statusListResponse.setSize(statuses.getSize());
        statusListResponse.setTotalElements(statuses.getTotalElements());
        statusListResponse.setTotalPages(statuses.getTotalPages());
        return statusListResponse;
    }
}
