/*
 * Lunox
 * Copyright (C) 2025 Sankalp Tharu
 *
 * This file is part of Last Launcher (forked).
 * Licensed under the GNU General Public License v3 or later.
 * See <http://www.gnu.org/licenses/>.
 *
*/

package io.github.sankalp.lunox.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Utility class to manage notification panel expansion
 */
public class NotificationPanelManager {

    private static final String TAG = "NotificationPanelManager";

    /**
     * Attempts to expand the notification panel using reflection
     * @param context The application context
     * @return true if successful, false otherwise
     */
    public static boolean expandNotificationPanel(Context context) {
        try {
            // Get the StatusBarManager service
            Object statusBarService = context.getSystemService("statusbar");

            if (statusBarService == null) {
                Log.w(TAG, "StatusBar service not available");
                return false;
            }

            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expandMethod;

            // Different method names for different Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                // Android 4.2+ uses expandNotificationsPanel
                expandMethod = statusBarManager.getMethod("expandNotificationsPanel");
            } else {
                // Older versions use expand
                expandMethod = statusBarManager.getMethod("expand");
            }

            expandMethod.setAccessible(true);
            expandMethod.invoke(statusBarService);

            Log.d(TAG, "Notification panel expanded successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to expand notification panel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Attempts to collapse the notification panel using reflection
     * @param context The application context
     * @return true if successful, false otherwise
     */
    public static boolean collapseNotificationPanel(Context context) {
        try {
            // Get the StatusBarManager service
            Object statusBarService = context.getSystemService("statusbar");

            if (statusBarService == null) {
                Log.w(TAG, "StatusBar service not available");
                return false;
            }

            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method collapseMethod;

            // Different method names for different Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                // Android 4.2+ uses collapsePanels
                collapseMethod = statusBarManager.getMethod("collapsePanels");
            } else {
                // Older versions use collapse
                collapseMethod = statusBarManager.getMethod("collapse");
            }

            collapseMethod.setAccessible(true);
            collapseMethod.invoke(statusBarService);

            Log.d(TAG, "Notification panel collapsed successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to collapse notification panel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
