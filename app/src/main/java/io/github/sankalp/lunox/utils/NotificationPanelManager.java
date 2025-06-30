/*
 * Last Launcher
 * Copyright (C) 2019,2020 Shubham Tyagi
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
