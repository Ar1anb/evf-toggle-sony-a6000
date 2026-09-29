package com.artec.evftoggle;

/**
 * The settings the app uses and what they mean. No android.* imports, so it is unit-tested with a plain JDK
 * (tools/test.sh). The native watcher (jni/jni.cpp) applies the same rule as {@link #forTrigger}.
 */
final class Display {
    private Display() {}

    /** active display: 01 = Viewfinder, 02 = Monitor. Writing it switches straight away. */
    static final int ID = 0x010708e0;
    /** the custom button's "Deactivate Monitor" state: 01 = LCD off, 00 = LCD on. Flips on every press. */
    static final int TRIGGER_ID = 0x01070b09;
    /** the custom button's function */
    static final int KEY_FUNCTION_ID = 0x01070c71;
    /** "Deactivate Monitor", the function whose state the watcher follows */
    static final int KEY_DEACTIVATE_MONITOR = 0x34;

    static final int VIEWFINDER = 0x01, MONITOR = 0x02;

    /** Viewfinder becomes Monitor; Monitor, or anything unexpected, becomes Viewfinder */
    static int next(int current) {
        return (current & 0xff) == VIEWFINDER ? MONITOR : VIEWFINDER;
    }

    /** what the watcher writes when the trigger changes: LCD off -> Viewfinder, LCD on -> Monitor */
    static int forTrigger(int trigger) {
        return (trigger & 0xff) == 0x01 ? VIEWFINDER : MONITOR;
    }

    /** the name shown on screen */
    static String name(int value) {
        switch (value & 0xff) {
            case VIEWFINDER: return "VIEWFINDER";
            case MONITOR: return "MONITOR";
            default: return "UNKNOWN (0x" + Integer.toHexString(value & 0xff) + ")";
        }
    }
}
