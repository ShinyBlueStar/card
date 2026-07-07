package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.cardProfile.*;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.event.cardProfile.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardProfileDataMapper;
import com.sample.system.card.service.domain.ports.input.service.*;
import com.sample.system.card.service.domain.response.profile.CreateCardProfileResponse;
import com.sample.system.card.service.domain.response.profile.UpdateCardProfileResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class CardProfileCommandHandler {

    private final CardProfileService cardProfileService;
    private final CardProfileDataMapper  cardProfileDataMapper;

    @Transactional
    public CreateCardProfileResponse createCardProfile(CreateCardProfileCommand command) throws CardDomainException {
        log.info("Starting CardProfile creation service - ProfileName: {}, PatternId: {}, BankId: {}",
                command.getProfileName(), command.getCardNumberPatternId(), command.getIssuingBankId());

        log.debug("Mapping CreateCardProfileCommand to CardProfile domain entity");
        CardProfile profile = cardProfileDataMapper.createCommandToProfile(command);
        log.debug("Command mapping completed successfully");

        CardProfileCreatedEvent profileEvent = cardProfileService.createCardProfile(profile);

        log.debug("Mapping CardProfile to CreateCardProfileResponse");
        return cardProfileDataMapper.profileToCreateResponse(profileEvent.getCardProfile(),
                "success");
    }

    @Transactional
    public UpdateCardProfileResponse updateCardProfile(UpdateCardProfileCommand command) throws CardDomainException {
        log.info("Starting CardProfile update service - ProfileId: {}", command.getCardProfileId());
        log.debug("Mapping UpdateCardProfileCommand to CardProfile domain entity");
        CardProfile profile = cardProfileDataMapper.updateCommandToProfile(command);
        log.debug("Command mapping completed successfully");

        CardProfileUpdatedEvent event = cardProfileService.updateCardProfile(profile);
        log.debug("CardProfile update completed by CommandHandler - ProfileId: {}",
                event.getCardProfile().getId().getValue());

        log.debug("Mapping CardProfile to UpdateCardProfileResponse");
        UpdateCardProfileResponse response = cardProfileDataMapper.cardProfileToUpdateCardProfileResponse(
                event.getCardProfile(), "Card Profile updated successfully!");

        log.info("CardProfile update service completed successfully - ProfileId: {}, ProfileName: {}",
                event.getCardProfile().getId().getValue(), event.getCardProfile().getProfileName());
        return response;
    }

    @Transactional
    public UpdateCardProfileResponse activateCardProfile(ActivateCardProfileCommand command) throws CardDomainException {
        log.info("Starting CardProfile activation service - ProfileId: {}", command.getCardProfileId());
        log.debug("Mapping ActivateCardProfileCommand to CardProfile domain entity");
        CardProfile profile = cardProfileDataMapper.activateCommandToProfile(command);
        log.debug("Command mapping completed successfully");

        CardProfileActivatedEvent event = cardProfileService.activateCardProfile(profile);
        log.info("Mapping CardProfile to UpdateCardProfileResponse");
        UpdateCardProfileResponse response = cardProfileDataMapper.cardProfileToUpdateCardProfileResponse(
                event.getCardProfile(), "success");
        log.info("CardProfile activation service completed successfully - ProfileId: {}, ProfileName: {}",
                event.getCardProfile().getId().getValue(), event.getCardProfile().getProfileName());
        return response;
    }

    @Transactional
    public UpdateCardProfileResponse deactivateCardProfile(DeactivateCardProfileCommand command) throws CardDomainException {
        log.info("Starting CardProfile deactivation service - ProfileId: {}", command.getCardProfileId());
        log.debug("Mapping DeactivateCardProfileCommand to CardProfile domain entity");
        CardProfile profile = cardProfileDataMapper.deactivateCommandToProfile(command);
        log.debug("Command mapping completed successfully");

        CardProfileDeactivatedEvent event = cardProfileService.deactivateCardProfile(profile);
        log.info("CardProfile deactivation completed by CommandHandler - ProfileId: {}",
                event.getCardProfile().getId().getValue());
        UpdateCardProfileResponse response = cardProfileDataMapper.cardProfileToUpdateCardProfileResponse(
                event.getCardProfile(), "Success");
        log.info("CardProfile deactivation service completed successfully - ProfileId: {}, ProfileName: {}",
                event.getCardProfile().getId().getValue(), event.getCardProfile().getProfileName());
        return response;
    }

    @Transactional
    public UpdateCardProfileResponse changeStatus(ChangeCardProfileStatusCommand command) throws CardDomainException {
        log.info("Changing CardProfile status - ProfileId: {}, Active: {}", command.getCardProfileId(), command.getActive());
        UpdateCardProfileResponse response;
        if (Boolean.TRUE.equals(command.getActive())) {
            CardProfile profile = cardProfileDataMapper.activateCommandToProfile(
                    ActivateCardProfileCommand.builder().cardProfileId(command.getCardProfileId()).build());
            CardProfileActivatedEvent event = cardProfileService.activateCardProfile(profile);
            response = cardProfileDataMapper.cardProfileToUpdateCardProfileResponse(
                    event.getCardProfile(), "Card profile activated successfully!");
        } else {
            CardProfile profile = cardProfileDataMapper.deactivateCommandToProfile(
                    DeactivateCardProfileCommand.builder().cardProfileId(command.getCardProfileId()).build());
            CardProfileDeactivatedEvent event = cardProfileService.deactivateCardProfile(profile);
            response = cardProfileDataMapper.cardProfileToUpdateCardProfileResponse(
                    event.getCardProfile(), "Card profile deactivated successfully!");
        }
        log.info("CardProfile status changed successfully - ProfileId: {}, Active: {}", command.getCardProfileId(), command.getActive());
        return response;
    }

}
