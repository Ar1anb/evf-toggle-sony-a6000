package com.artec.evftoggle;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Opening the app starts the button watcher, or, if it is already running, offers to stop it.
 *
 *  - not running: set C1 to Deactivate Monitor, start the watcher, show ON, close after a moment
 *  - running:     show RUNNING; centre button stops it (shows OFF), any other button closes
 *
 * Errors stay on screen until a key is pressed.
 */
public class MainActivity extends Activity {
    private static final long SHOW_MS = 2500;
    /** long enough to read the screen and reach the centre button */
    private static final long RUNNING_SHOW_MS = 8000;
    private static final long IGNORE_KEYS_MS = 500;
    /** Sony scan code of the centre button (com.sony.scalar.sysutil.ScalarInput, as in Recipe Lab) */
    private static final int SCAN_CENTRE = 232;

    private static final int STATE_BUSY = 0, STATE_RUNNING = 1, STATE_DONE = 2;

    private final Handler handler = new Handler();
    private final Runnable close = new Runnable() { public void run() { finish(); } };
    private TextView title, detail;
    private long startedAt;
    private boolean handled;
    private int state = STATE_BUSY;
    private static final int NO_KEY = Integer.MIN_VALUE;
    private int downScan = NO_KEY;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        buildUi();
        startedAt = SystemClock.uptimeMillis();
        handled = saved != null && saved.getBoolean("handled", false);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("handled", handled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handled) { closeIn(SHOW_MS); return; }
        handled = true;
        try {
            if (NativeBackup.watcherRunning()) {
                showRunning();
                return;
            }
            if (NativeBackup.readByte(Display.KEY_FUNCTION_ID) != Display.KEY_DEACTIVATE_MONITOR) {
                NativeBackup.writeByte(Display.KEY_FUNCTION_ID, Display.KEY_DEACTIVATE_MONITOR);
                NativeBackup.sync();
            }
            int r = NativeBackup.startWatcher();
            if (r == 1) { showRunning(); return; }               // started by someone else a moment ago
            if (r < 0) { showError("COULD NOT START", "The button watcher did not start (code " + r + ")."); return; }
            state = STATE_DONE;
            title.setText("ON");
            detail.setText("Press C1 to switch viewfinder / monitor.\n"
                    + "Keeps running through power off and on.\n"
                    + "Open this app again to turn it off.");
            closeIn(SHOW_MS);
        } catch (NativeException e) {
            showError("COULD NOT START", String.valueOf(e.getMessage()));
        } catch (Throwable t) {
            showError("COULD NOT START", String.valueOf(t));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(close);
    }

    private void closeIn(long ms) {
        handler.removeCallbacks(close);
        handler.postDelayed(close, ms);
    }

    private void showRunning() {
        state = STATE_RUNNING;
        title.setText("RUNNING");
        detail.setText("C1 switches viewfinder / monitor.\n\n"
                + "Centre button: turn it off\n"
                + "Any other button: leave it on");
        closeIn(RUNNING_SHOW_MS);
    }

    private void stop() {
        state = STATE_DONE;
        int r = NativeBackup.stopWatcher();
        if (r < 0) { showError("COULD NOT STOP", "The button watcher did not stop (code " + r + ")."); return; }
        title.setText("OFF");
        detail.setText("C1 no longer switches displays.\nOpen this app again to turn it back on.");
        closeIn(SHOW_MS);
    }

    private void showError(String head, String msg) {
        state = STATE_DONE;
        handler.removeCallbacks(close);
        title.setText(head);
        title.setTextColor(Color.rgb(255, 120, 90));
        detail.setText(msg + "\n\nPress any button to close.");
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.BLACK);
        title = new TextView(this);
        title.setTextSize(34);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setText("...");
        detail = new TextView(this);
        detail.setTextSize(15);
        detail.setTextColor(Color.rgb(170, 170, 170));
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(20, 12, 20, 0);
        root.addView(title);
        root.addView(detail);
        setContentView(root);
    }

    private static boolean isCentre(KeyEvent e) {
        return e.getScanCode() == SCAN_CENTRE || e.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                || e.getKeyCode() == KeyEvent.KEYCODE_ENTER;
    }

    /**
     * Act on a key's release, but only for a key whose press also happened in this app. The centre press that
     * launched the app from the Application List is released here too, and must not count as "turn it off".
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            if (e.getRepeatCount() == 0 && SystemClock.uptimeMillis() - startedAt > IGNORE_KEYS_MS) downScan = e.getScanCode();
            return true;
        }
        if (e.getAction() != KeyEvent.ACTION_UP || downScan != e.getScanCode()) return true;
        downScan = NO_KEY;
        if (state == STATE_RUNNING && isCentre(e)) stop();
        else if (state != STATE_BUSY) finish();
        return true;
    }
}
