package com.sample.system.card.service.dataaccess.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import java.time.LocalDateTime;

/**
 * Custom Revision Entity for Card Service Audit Trail
 * Extends DefaultRevisionEntity to add custom audit information
 * Following Hibernate Envers best practices
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@RevisionEntity(CardServiceRevisionListener.class)
@Table(name = "REVINFO")
public class CardServiceRevisionEntity extends DefaultRevisionEntity {

    /**
     * User who made the change
     */
    private String modifiedBy;

    /**
     * Timestamp when the change was made
     */
    private LocalDateTime modifiedAt;

    /**
     * IP address of the user
     */
    private String modifiedFromIp;

    /**
     * Application that made the change
     */
    private String applicationName;

    /**
     * Session ID for tracking user sessions
     */
    private String sessionId;

    /**
     * Additional context information
     */
    private String changeContext;

    /**
     * Business operation type (CREATE, UPDATE, DELETE, etc.)
     */
    private String operationType;

    /**
     * Additional metadata in JSON format
     */
    private String metadata;
}
