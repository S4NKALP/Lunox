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

package io.github.sankalp.lunox.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import io.github.sankalp.lunox.services.LockScreenAccessibilityService;

/**
 * Utility class for accessibility service operations
 */
public class AccessibilityUtils {

    private static final String TAG = "AccessibilityUtils";

    /**
     * Check if our accessibility service is enabled
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) {
            return false;
        }

        // Check if accessibility is enabled and our service is in the enabled services list
        String enabledServices = Settings.Secure.getString(
            context.getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (enabledServices == null || enabledServices.isEmpty()) {
            return false;
        }

        String packageName = context.getPackageName();
        String serviceName = LockScreenAccessibilityService.class.getName();
        String fullServiceName = packageName + "/" + serviceName;

        // Check if our service is in the enabled services list
        return enabledServices.contains(fullServiceName);
    }

    /**
     * Open accessibility settings to enable our service
     */
    public static void openAccessibilitySettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open accessibility settings", e);
        }
    }

    /**
     * Request lock screen action through accessibility service
     */
    public static boolean requestLockScreen(Context context) {
        if (!isAccessibilityServiceEnabled(context)) {
            Log.w(TAG, "Accessibility service not enabled");
            return false;
        }

        LockScreenAccessibilityService service = LockScreenAccessibilityService.getInstance();
        if (service != null) {
            service.lockScreen();
            return true;
        } else {
            // Try to start the service with intent
            try {
                Intent intent = new Intent(context, LockScreenAccessibilityService.class);
                intent.setAction(LockScreenAccessibilityService.ACTION_LOCK_SCREEN);
                context.startService(intent);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to start accessibility service", e);
                return false;
            }
        }
    }

    /**
     * Check if accessibility service is running
     */
    public static boolean isServiceRunning() {
        return LockScreenAccessibilityService.isServiceRunning();
    }
}
