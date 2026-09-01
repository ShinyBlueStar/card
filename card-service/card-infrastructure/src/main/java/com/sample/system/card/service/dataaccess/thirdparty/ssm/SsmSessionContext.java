package com.sample.system.card.service.dataaccess.thirdparty.ssm;

import java.util.UUID;

/**
 * Thread-local context for current SSM sessionId so that callGenerate/callValidate
 * do not need sessionId as an explicit parameter; the channel reads it from here.
 */
public final class SsmSessionContext {

    private static final ThreadLocal<UUID> CURRENT_SESSION_ID = new ThreadLocal<>();

    public static void setCurrentSessionId(UUID sessionId) {
        CURRENT_SESSION_ID.set(sessionId);
    }

    public static UUID getCurrentSessionId() {
        return CURRENT_SESSION_ID.get();
    }

    public static void clear() {
        CURRENT_SESSION_ID.remove();
    }

    private SsmSessionContext() {
    }
}
