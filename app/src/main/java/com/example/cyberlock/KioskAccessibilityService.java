package com.example.cyberlock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class KioskAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View blockOverlay;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Check karo ki user ne Apply dabaya hai ya nahi
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("is_locked", false)) return; 

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String pkg = event.getPackageName().toString();
                String cls = event.getClassName() != null ? event.getClassName().toString() : "";

                if (!pkg.equals(getPackageName()) || cls.contains("NotificationShade") || cls.contains("StatusBar")) {
                    triggerLockdown();
                }
            }
        }
    }

    private void triggerLockdown() {
        if (blockOverlay == null) {
            blockOverlay = new View(this);
            blockOverlay.setBackgroundColor(Color.BLACK);
            int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
            
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            );
            try { windowManager.addView(blockOverlay, params); } catch (Exception e) {}
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        handler.postDelayed(() -> {
            if (blockOverlay != null) {
                try { windowManager.removeView(blockOverlay); } catch (Exception e) {}
                blockOverlay = null;
            }
        }, 2000);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("is_locked", false)) return super.onKeyEvent(event);

        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || 
            keyCode == KeyEvent.KEYCODE_APP_SWITCH || keyCode == KeyEvent.KEYCODE_VOLUME_UP || 
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            triggerLockdown();
            return true; 
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {
        if (blockOverlay != null) {
            try { windowManager.removeView(blockOverlay); } catch (Exception e) {}
            blockOverlay = null;
        }
    }
}
