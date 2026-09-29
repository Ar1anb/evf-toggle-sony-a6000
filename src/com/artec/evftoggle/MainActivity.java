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
 * Opening the app flips the camera between Viewfinder and Monitor, shows which one is now active, and closes itself.
 * Any key closes it sooner. If something goes wrong the message stays on screen until a key is pressed.
 */
public class MainActivity extends Activity {
    /** how long the result stays on screen before the app closes on its own */
    private static final long SHOW_MS = 1500;
    /** key releases this soon after launch belong to the press that opened the app: ignore them */
    private static final long IGNORE_KEYS_MS = 500;

    private final Handler handler = new Handler();
    private final Runnable close = new Runnable() { public void run() { finish(); } };
    private TextView title, detail;
    private long startedAt;
    private boolean toggled;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        buildUi();
        startedAt = SystemClock.uptimeMillis();
        toggled = saved != null && saved.getBoolean("toggled", false);   // never flip twice for one launch
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("toggled", toggled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toggled) { handler.postDelayed(close, SHOW_MS); return; }
        toggled = true;
        try {
            int before = NativeBackup.readByte(Display.ID);
            int after = Display.next(before);
            NativeBackup.writeByte(Display.ID, after);
            NativeBackup.sync();                                   // commit to the store so it survives a power-off
            int check = NativeBackup.readByte(Display.ID);
            if (check != after) {
                showError("Wrote " + Display.name(after) + " but read back " + Display.name(check) + ".");
                return;
            }
            title.setText(Display.name(after));
            detail.setText("was " + Display.name(before));
            handler.postDelayed(close, SHOW_MS);
        } catch (NativeException e) {
            showError(String.valueOf(e.getMessage()));
        } catch (Throwable t) {
            showError(String.valueOf(t));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(close);
    }

    private void showError(String msg) {
        title.setText("COULD NOT SWITCH");
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
        title.setText("SWITCHING...");
        detail = new TextView(this);
        detail.setTextSize(15);
        detail.setTextColor(Color.rgb(170, 170, 170));
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(20, 12, 20, 0);
        root.addView(title);
        root.addView(detail);
        setContentView(root);
    }

    /** any key closes the app, except the release of the press that opened it */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_UP && SystemClock.uptimeMillis() - startedAt > IGNORE_KEYS_MS) finish();
        return true;
    }
}
