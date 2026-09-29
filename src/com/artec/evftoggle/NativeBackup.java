package com.artec.evftoggle;

/** JNI binding to the camera settings store and the button watcher (libevftoggle.so). */
public class NativeBackup {
    static { System.loadLibrary("evftoggle"); }

    public static native byte[] read(int id) throws NativeException;
    public static native void write(int id, byte[] data) throws NativeException;
    public static native void sync();

    /** start the background button watcher: 0 = started, 1 = already running, negative = failed */
    public static native int startWatcher();
    public static native boolean watcherRunning();

    public static int readByte(int id) throws NativeException {
        byte[] b = read(id);
        return b != null && b.length > 0 ? b[0] & 0xff : 0;
    }

    public static void writeByte(int id, int value) throws NativeException {
        write(id, new byte[] { (byte) value });
    }
}
