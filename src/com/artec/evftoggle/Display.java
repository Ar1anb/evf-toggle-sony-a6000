package com.artec.evftoggle;

/**
 * What the display setting means and what the toggle does to it. No android.* imports, so it is unit-tested with a
 * plain JDK (tools/test.sh).
 *
 * The setting is 0x010708e0 in the camera's settings store, one byte: 01 = Viewfinder, 02 = Monitor. Writing it
 * switches the display straight away. Auto (eye sensor) is NOT stored here; see README.
 */
final class Display {
    private Display() {}

    /** the settings-store ID of the active display */
    static final int ID = 0x010708e0;

    static final int VIEWFINDER = 0x01, MONITOR = 0x02;

    /** Viewfinder becomes Monitor; Monitor, or anything unexpected, becomes Viewfinder */
    static int next(int current) {
        return (current & 0xff) == VIEWFINDER ? MONITOR : VIEWFINDER;
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
