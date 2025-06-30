/*
 * Last Launcher
 * Copyright (C) 2019 Shubham Tyagi
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

package io.github.sankalp.lunox.dialogs;

import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import io.github.sankalp.lunox.LauncherActivity;
import io.github.sankalp.lunox.R;
import io.github.sankalp.lunox.utils.DbUtils;
import io.github.sankalp.lunox.utils.DeviceAdminManager;

public class PermissionDialog extends Dialog implements View.OnClickListener {

    private final LauncherActivity context;
    private TextView usageAccessButton;
    private TextView deviceAdminButton;
    private TextView continueButton;

    public PermissionDialog(Context context, LauncherActivity launcherActivity) {
        super(context);
        this.context = launcherActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_permission_setup);
        setCancelable(false);

        // Initialize views
        usageAccessButton = findViewById(R.id.permission_usage_access);
        deviceAdminButton = findViewById(R.id.permission_device_admin);
        continueButton = findViewById(R.id.permission_continue);

        // Set click listeners
        usageAccessButton.setOnClickListener(this);
        deviceAdminButton.setOnClickListener(this);
        continueButton.setOnClickListener(this);

        // Update button states
        updatePermissionStates();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.permission_usage_access) {
            openUsageAccessSettings();
        } else if (id == R.id.permission_device_admin) {
            enableDeviceAdmin();
        } else if (id == R.id.permission_continue) {
            continueToLauncher();
        }
    }

    private void openUsageAccessSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
            Toast.makeText(context, "Please enable Usage Access for Lunox", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Fallback to general settings if specific intent fails
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
            Toast.makeText(context, "Please find and enable Usage Access for Lunox in Settings", Toast.LENGTH_LONG).show();
        }
    }

    private void enableDeviceAdmin() {
        try {
            DeviceAdminManager.requestDeviceAdminPermission(context);
            Toast.makeText(context, "Please enable Device Admin for Lunox", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Unable to open Device Admin settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void continueToLauncher() {
        // Mark permission dialog as shown
        DbUtils.setPermissionDialogShown(true);

        // Check if at least one permission is granted
        boolean hasUsageAccess = checkUsageAccessPermission();
        boolean hasDeviceAdmin = DeviceAdminManager.isDeviceAdminActive(context);

        if (hasUsageAccess || hasDeviceAdmin) {
            dismiss();
            context.loadApps(); // Reload apps after permissions are granted
        } else {
            Toast.makeText(context, "Please grant at least one permission to continue", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkUsageAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), context.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        }
        return true; // For older versions, assume permission is granted
    }

    private void updatePermissionStates() {
        // Update Usage Access button
        boolean hasUsageAccess = checkUsageAccessPermission();
        if (hasUsageAccess) {
            usageAccessButton.setText("Usage Access");
            usageAccessButton.setAlpha(0.7f);
        } else {
            usageAccessButton.setText("Enable Usage Access");
            usageAccessButton.setAlpha(1.0f);
        }

        // Update Device Admin button
        boolean hasDeviceAdmin = DeviceAdminManager.isDeviceAdminActive(context);
        if (hasDeviceAdmin) {
            deviceAdminButton.setText("Device Admin");
            deviceAdminButton.setAlpha(0.7f);
        } else {
            deviceAdminButton.setText("Enable Device Admin");
            deviceAdminButton.setAlpha(1.0f);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Update permission states when dialog becomes visible
        updatePermissionStates();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Update permission states when user returns to the dialog
            updatePermissionStates();
        }
    }
}
