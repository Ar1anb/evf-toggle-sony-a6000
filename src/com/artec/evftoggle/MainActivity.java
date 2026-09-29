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
 * Opening the app sets the custom button to Deactivate Monitor and starts a background watcher. From then on, every
 * press of that button switches between Viewfinder and Monitor, until the camera is turned off. The app shows the
 * result for a moment and closes itself; any key closes it sooner. Errors stay on screen until a key is pressed.
 */
public class MainActivity extends Activity {
    private static final long SHOW_MS = 2500;
    private static final long IGNORE_KEYS_MS = 500;

    private final Handler handler = new Handler();
    private final Runnable close = new Runnable() { public void run() { finish(); } };
    private TextView title, detail;
    private long startedAt;
    private boolean done;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        buildUi();
        startedAt = SystemClock.uptimeMillis();
        done = saved != null && saved.getBoolean("done", false);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("done", done);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (done) { handler.postDelayed(close, SHOW_MS); return; }
        done = true;
        try {
            if (NativeBackup.readByte(Display.KEY_FUNCTION_ID) != Display.KEY_DEACTIVATE_MONITOR) {
                NativeBackup.writeByte(Display.KEY_FUNCTION_ID, Display.KEY_DEACTIVATE_MONITOR);
                NativeBackup.sync();
            }
            int r = NativeBackup.startWatcher();
            if (r < 0) { showError("Could not start the button watcher (code " + r + ")."); return; }
            title.setText(r == 1 ? "ALREADY ON" : "BUTTON READY");
            detail.setText("Press C1 to switch viewfinder / monitor.\nWorks until the camera is turned off.");
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
        title.setText("COULD NOT START");
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
        title.setText("STARTING...");
        detail = new TextView(this);
        detail.setTextSize(15);
        detail.setTextColor(Color.rgb(170, 170, 170));
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(20, 12, 20, 0);
        root.addView(title);
        root.addView(detail);
        setContentView(root);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_UP && SystemClock.uptimeMillis() - startedAt > IGNORE_KEYS_MS) finish();
        return true;
    }
}
