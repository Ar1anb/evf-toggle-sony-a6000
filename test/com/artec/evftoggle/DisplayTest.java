package com.artec.evftoggle;

/** Plain-JDK checks of the toggle logic. Run with tools/test.sh. */
public class DisplayTest {
    private static int failures = 0;

    private static void eq(String what, Object want, Object got) {
        if (!want.equals(got)) { failures++; System.out.println("FAIL " + what + ": want " + want + ", got " + got); }
    }

    public static void main(String[] args) {
        eq("viewfinder -> monitor", Display.MONITOR, Display.next(Display.VIEWFINDER));
        eq("monitor -> viewfinder", Display.VIEWFINDER, Display.next(Display.MONITOR));
        eq("unknown 0 -> viewfinder", Display.VIEWFINDER, Display.next(0));
        eq("unknown 3 -> viewfinder", Display.VIEWFINDER, Display.next(3));
        eq("signed byte 0x01", Display.MONITOR, Display.next((byte) 0x01));
        eq("store ID", 0x010708e0, Display.ID);
        eq("name vf", "VIEWFINDER", Display.name(1));
        eq("name mon", "MONITOR", Display.name(2));
        eq("name other", "UNKNOWN (0x7)", Display.name(7));
        if (failures > 0) { System.out.println(failures + " test(s) failed"); System.exit(1); }
        System.out.println("all tests passed");
    }
}
