package com.sample.system.card.service.dataaccess.utility;

/**
 * Utility for caller class/method name (used in WebCallUtils etc. without depending on domain Utility).
 */
public final class CallerUtil {

    private CallerUtil() {}

    public static String getCallerClassAndMethodName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // [0]=getStackTrace, [1]=getCallerClassAndMethodName, [2]=caller
        if (stack.length > 2) {
            StackTraceElement caller = stack[2];
            return caller.getClassName() + '.' + caller.getMethodName();
        }
        return "unknown";
    }
}
