package com.sample.system.card.service.dataaccess.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Revision Listener for Card Service Audit Trail
 * Automatically populates audit information when changes are made
 * Following Hibernate Envers best practices
 */
@Slf4j
public class CardServiceRevisionListener implements RevisionListener {

    private static final String APPLICATION_NAME = "Card Service";

    @Override
    public void newRevision(Object revisionEntity) {
        CardServiceRevisionEntity cardServiceRevisionEntity = (CardServiceRevisionEntity) revisionEntity;

        // Set timestamp
        cardServiceRevisionEntity.setModifiedAt(LocalDateTime.now());

        // Set application name
        cardServiceRevisionEntity.setApplicationName(APPLICATION_NAME);

        // Try to get request information
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Get IP address
                String ipAddress = getClientIpAddress(request);
                cardServiceRevisionEntity.setModifiedFromIp(ipAddress);

                // Get session ID
                String sessionId = request.getSession(false) != null ?
                        request.getSession().getId() : UUID.randomUUID().toString();
                cardServiceRevisionEntity.setSessionId(sessionId);

                // Get additional context
                String context = String.format("URI: %s, Method: %s",
                        request.getRequestURI(), request.getMethod());
                cardServiceRevisionEntity.setChangeContext(context);

                // Get user agent
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    cardServiceRevisionEntity.setMetadata(String.format("{\"userAgent\":\"%s\"}", userAgent));
                }
            } else {
                cardServiceRevisionEntity.setModifiedFromIp("UNKNOWN");
                cardServiceRevisionEntity.setSessionId(UUID.randomUUID().toString());
                cardServiceRevisionEntity.setChangeContext("Background Process");
            }
        } catch (Exception e) {
            log.warn("Could not get request information: {}", e.getMessage());
            cardServiceRevisionEntity.setModifiedFromIp("UNKNOWN");
            cardServiceRevisionEntity.setSessionId(UUID.randomUUID().toString());
            cardServiceRevisionEntity.setChangeContext("Background Process");
        }

        // Set operation type based on revision type
        cardServiceRevisionEntity.setOperationType("AUDIT");

        log.debug("Created revision entity with user: {}, IP: {}, context: {}",
                cardServiceRevisionEntity.getModifiedBy(),
                cardServiceRevisionEntity.getModifiedFromIp(),
                cardServiceRevisionEntity.getChangeContext());
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
