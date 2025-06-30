/*
 * Lunox Launcher
 * Copyright (C) 2024 Sankalp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.sankalp.lunox.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Accessibility service for lock screen functionality
 * This service can perform global actions like simulating power key press
 */
public class LockScreenAccessibilityService extends AccessibilityService {
    
    private static final String TAG = "LockScreenAccessibilityService";
    private static LockScreenAccessibilityService instance;
    
    public static final String ACTION_LOCK_SCREEN = "io.github.sankalp.lunox.ACTION_LOCK_SCREEN";
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Accessibility service created");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "Accessibility service destroyed");
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // We don't need to handle accessibility events for this use case
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }
    
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return super.onKeyEvent(event);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_LOCK_SCREEN.equals(intent.getAction())) {
            lockScreen();
        }
        return START_NOT_STICKY;
    }
    
    /**
     * Lock the screen using accessibility service global action
     */
    public void lockScreen() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ supports GLOBAL_ACTION_LOCK_SCREEN
                boolean success = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                Log.d(TAG, "Lock screen action performed: " + success);
                if (!success) {
                    // Fallback to power button simulation
                    simulatePowerButton();
                }
            } else {
                // For older versions, simulate power button press
                simulatePowerButton();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to lock screen", e);
        }
    }
    
    /**
     * Simulate power button press using accessibility service
     */
    private void simulatePowerButton() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use gesture simulation to simulate power button
                // This is a workaround since we can't directly simulate hardware keys
                // We'll use the global action for power dialog which can lock the screen
                boolean success = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
                Log.d(TAG, "Power dialog action performed: " + success);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to simulate power button", e);
        }
    }
    
    /**
     * Get the current instance of the accessibility service
     */
    public static LockScreenAccessibilityService getInstance() {
        return instance;
    }
    
    /**
     * Check if the accessibility service is running
     */
    public static boolean isServiceRunning() {
        return instance != null;
    }
}
