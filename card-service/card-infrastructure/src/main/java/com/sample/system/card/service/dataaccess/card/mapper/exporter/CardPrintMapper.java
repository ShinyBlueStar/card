package com.sample.system.card.service.dataaccess.card.mapper.exporter;

import com.sample.system.card.service.dataaccess.card.dto.accessFile.AccessCardPrintRow;
import com.sample.system.card.service.dataaccess.card.dto.accessFile.CardPrintRow;
import com.sample.system.card.service.dataaccess.card.dto.accessFile.CombinedCardPrintRow;
import com.sample.system.card.service.dataaccess.card.dto.accessFile.CustomerPrintRow;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.entity.Customer;
import com.sample.system.card.service.domain.utility.date.DateUtils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Mapper for converting Domain entities to Print DTOs
 * Part of Anti-Corruption Layer - isolates domain from infrastructure
 */
@Component
public class CardPrintMapper {

    /**
     * Convert domain Card entities to CardPrintRow DTOs
     */
    public List<CardPrintRow> toCardPrintRows(List<Card> cards) {
        if (cards == null) {
            return List.of();
        }

        return cards.stream()
                .map(this::toCardPrintRow)
                .toList();
    }

    public CardPrintRow toCardPrintRow(Card card) {
        if (card == null) {
            return null;
        }

        return CardPrintRow.builder()
                .cardId(card.getId() != null ? card.getId().getValue() : null)
                .pan(card.getPan())
                .maskedPan(card.getMaskedPan())
                .status(card.getCardStatus() != null ? card.getCardStatus().name() : null)
                .profileId(card.getCardProfileId() != null ? card.getCardProfileId().getValue() : null)
                .customerNationalId(card.getCustomer() != null ? card.getCustomer().getNationalId() : null)
                .issueDate(convertLocalDateTimeToString(card.getIssueDate()))
                .expDate(convertExpDateToString(card.getExpDate()))
                .unitCode(card.getUnitCode())
                .unitName(card.getUnitName())
                .build();
    }

    public List<CustomerPrintRow> toCustomerPrintRows(List<Card> cards) {
        if (cards == null) {
            return List.of();
        }

        Set<String> exportedNationalIds = new java.util.HashSet<>();

        return cards.stream()
                .map(Card::getCustomer)
                .filter(customer -> customer != null && customer.getNationalId() != null)
                .filter(customer -> exportedNationalIds.add(customer.getNationalId()))
                .map(this::toCustomerPrintRow)
                .toList();
    }

    /**
     * Convert domain Customer entity to CustomerPrintRow DTO
     */
    public CustomerPrintRow toCustomerPrintRow(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerPrintRow.builder()
                .customerId(customer.getId())
                .nationalId(customer.getNationalId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .address(customer.getAddress())
                .build();
    }

    public CombinedCardPrintRow toCombinedCardPrintRow(Card card) {
        if (card == null) {
            return null;
        }

        Customer customer = card.getCustomer();

        return CombinedCardPrintRow.builder()
                // Card fields
                .cardId(card.getId() != null ? card.getId().getValue() : null)
                .pan(card.getPan())
                .maskedPan(card.getMaskedPan())
                .status(card.getCardStatus() != null ? card.getCardStatus().name() : null)
                .profileId(card.getCardProfileId() != null ? card.getCardProfileId().getValue() : null)
                .issueDate(convertLocalDateTimeToString(card.getIssueDate()))
                .expDate(convertExpDateToString(card.getExpDate()))
                .unitCode(card.getUnitCode())
                .unitName(card.getUnitName())
                // Track fields
                .track1(card.getTrack().getTrack1())
                .track2(card.getTrack().getTrack2())
                .track3(card.getTrack().getTrack3())
                .pin1(card.getTrack().getPin1())
                .pin2(card.getTrack().getPin2())
                // Customer fields
                .customerId(customer != null ? customer.getId() : null)
                .customerNationalId(customer != null ? customer.getNationalId() : null)
                .firstName(customer != null ? customer.getFirstName() : null)
                .lastName(customer != null ? customer.getLastName() : null)
                .address(customer != null ? customer.getAddress() : null)
                .build();
    }

    private String convertLocalDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    private String convertExpDateToString(String expDate) {
        if (expDate == null || expDate.isBlank()) {
            return null;
        }
        return expDate.trim();
    }

    /**
     * Convert domain Card entities to AccessCardPrintRow DTOs
     * Maps to the exact Access schema required by the printing system
     * All transformation logic lives here (Anti-Corruption Layer)
     */
    public List<AccessCardPrintRow> toAccessRows(List<Card> cards) {
        if (cards == null) {
            return List.of();
        }

        return cards.stream()
                .map(this::toAccessRow)
                .toList();
    }

    /**
     * Convert domain Card entity to AccessCardPrintRow DTO
     * Implements all mapping rules from Card/Track/Customer to Access schema
     */
    private AccessCardPrintRow toAccessRow(Card card) {
        if (card == null) {
            return null;
        }

        Customer customer = card.getCustomer();
        String pan = card.getPan();
        String[] panParts = splitPan(pan);

        // Get current date/time for log fields
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // Format dates
        String exDate = formatExpDate(card.getExpDate());
        String firstStatementDate = formatFirstStatementDate(card.getIssueDate());

        // Split address
        String[] addressParts = splitAddress(customer != null ? customer.getAddress() : null);

        // Build full name
        String firstName = customer != null ? customer.getFirstName() : null;
        String lastName = customer != null ? customer.getLastName() : null;
        String fullName = buildFullName(firstName, lastName);

        return AccessCardPrintRow.builder()
                .id(card.getId() != null ? card.getId().getValue() : null)
                .panPart1(panParts[0])
                .panPart2(panParts[1])
                .panPart3(panParts[2])
                .panPart4(panParts[3])
                .cvv2(card.getCvv2())
                .track1(card.getTrack().getTrack1())
                .track2(card.getTrack().getTrack2())
                .track3(card.getTrack().getTrack3())
                .fullName(fullName)
                .firstName(firstName)
                .lastName(lastName)
                .exDate(exDate)
                .pan(pan)
                .firstStatementDate(firstStatementDate)
                .address(customer != null ? customer.getAddress() : null)
                .address1(addressParts[0])
                .address2(addressParts[1])
                .address3(addressParts[2])
                .zipCode(null) // Not available in Customer domain - safe default
                .barcode(generateBarcode(pan)) // Derived from PAN
                .cellNumber(null) // Not available in Customer domain - safe default
                .printCount(null) // Default value
                .printCommand(null) // Default value
                .lDate(null)
                .lTime(null)
                .nationalId(customer != null ? customer.getNationalId() : null)
                .build();
    }

    /**
     * Split PAN into 4 parts (4 digits each for 16-digit PAN)
     * Handles edge cases with safe defaults
     */
    private String[] splitPan(String pan) {
        if (pan == null || pan.isBlank()) {
            return new String[]{"", "", "", ""};
        }

        String cleanPan = pan.trim().replaceAll("\\s+", "");

        // Standard 16-digit PAN: split into 4 parts of 4 digits each
        if (cleanPan.length() >= 16) {
            return new String[]{
                cleanPan.substring(0, Math.min(4, cleanPan.length())),
                cleanPan.substring(4, Math.min(8, cleanPan.length())),
                cleanPan.substring(8, Math.min(12, cleanPan.length())),
                cleanPan.substring(12, Math.min(16, cleanPan.length()))
            };
        }

        // Handle shorter PANs with padding
        String[] parts = new String[4];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            if (index < cleanPan.length()) {
                int endIndex = Math.min(index + 4, cleanPan.length());
                parts[i] = cleanPan.substring(index, endIndex);
                index = endIndex;
            } else {
                parts[i] = "";
            }
        }
        return parts;
    }

    private String formatExpDate(String expDate) {
        if (expDate == null || expDate.isBlank()) {
            return null;
        }
        return expDate.substring(2,4) + "/" + expDate.substring(5,7);
    }

    private String formatFirstStatementDate(LocalDateTime issueDate) {
        if (issueDate == null) {
            return null;
        }
        Date date = Date.from(issueDate.atZone(ZoneId.systemDefault()).toInstant());
        return DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, date, "yyyy/MM/dd", false);
    }

    /**
     * Split address into 3 parts (Address1, Address2, Address3)
     * Uses comma or newline as delimiter
     */
    private String[] splitAddress(String address) {
        if (address == null || address.isBlank()) {
            return new String[]{null, null, null};
        }

        // Split by comma or newline
        String[] parts = address.split("[,;\\n]");

        String[] result = new String[3];
        for (int i = 0; i < 3; i++) {
            if (i < parts.length) {
                String part = parts[i].trim();
                result[i] = part.isEmpty() ? null : part;
            } else {
                result[i] = null;
            }
        }

        return result;
    }

    /**
     * Build full name from first and last name
     */
    private String buildFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Generate barcode from PAN (placeholder implementation)
     * In production, this might use a barcode generation library
     */
    private String generateBarcode(String pan) {
        if (pan == null || pan.isBlank()) {
            return null;
        }
        // Simple implementation: use PAN as barcode
        // In production, this might generate a proper barcode string
        return pan.trim().replaceAll("\\s+", "");
    }
}
