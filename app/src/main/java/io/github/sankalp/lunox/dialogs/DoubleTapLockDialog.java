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

package io.github.sankalp.lunox.dialogs;

import android.app.Dialog;
import android.content.Context;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import io.github.sankalp.lunox.LauncherActivity;
import io.github.sankalp.lunox.R;
import io.github.sankalp.lunox.utils.AccessibilityUtils;
import io.github.sankalp.lunox.utils.DbUtils;

/**
 * Dialog for configuring double tap to lock screen functionality
 */
public class DoubleTapLockDialog extends Dialog implements View.OnClickListener {

    private final LauncherActivity launcherActivity;
    private final Context context;
    private TextView enableButton;
    private TextView closeButton;
    private TextView mainDescription;
    private TextView additionalInfo;

    public DoubleTapLockDialog(Context context, LauncherActivity launcherActivity) {
        super(context);
        this.context = context;
        this.launcherActivity = launcherActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_double_tap_lock);

        // Make background transparent for rounded corners
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize views
        enableButton = findViewById(R.id.btn_enable);
        closeButton = findViewById(R.id.btn_close);
        mainDescription = findViewById(R.id.tv_main_description);
        additionalInfo = findViewById(R.id.tv_additional_info);

        // Set click listeners
        enableButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);

        // Update content based on current state
        updateDialogContent();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Refresh dialog content when user returns from settings
            updateDialogContent();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_enable) {
            handleEnableButton();
        } else if (id == R.id.btn_close) {
            dismiss();
        }
    }

    private void handleEnableButton() {
        boolean isCurrentlyEnabled = DbUtils.isDoubleTapToLockEnabled();

        if (isCurrentlyEnabled) {
            // Disable the feature
            DbUtils.setDoubleTapToLock(false);
            updateDialogContent();
            dismiss();
        } else {
            // Check if accessibility service is enabled before enabling the feature
            if (AccessibilityUtils.isAccessibilityServiceEnabled(context)) {
                // Service is enabled, so we can enable the feature
                DbUtils.setDoubleTapToLock(true);
                updateDialogContent();
                dismiss();
            } else {
                // Service is not enabled, open accessibility settings but don't enable the feature yet
                AccessibilityUtils.openAccessibilitySettings(context);
                // Keep the dialog open so user can try again after enabling the service
                updateDialogContent();
            }
        }
    }

    private void updateDialogContent() {
        boolean isEnabled = DbUtils.isDoubleTapToLockEnabled();
        boolean isServiceEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(context);

        // Update button text based on current state
        if (isEnabled) {
            enableButton.setText("Disable");
        } else if (isServiceEnabled) {
            enableButton.setText("Enable");
        } else {
            enableButton.setText("Open Settings");
        }

        // Update description based on current state
        if (isEnabled && isServiceEnabled) {
            mainDescription.setText("Double tap to lock is currently enabled and working. You can disable it below if you no longer want this feature.");
            additionalInfo.setText("The accessibility service is active and allows you to lock your screen by double tapping on empty areas of the launcher.");
        } else if (isEnabled && !isServiceEnabled) {
            mainDescription.setText("Double tap to lock is enabled but the accessibility service is not active. Please enable Lunox's accessibility service in your device's settings.");
            additionalInfo.setText("The accessibility service is required for the double tap to lock feature to work. It only locks your screen and does not collect any data.");
        } else if (!isEnabled && isServiceEnabled) {
            mainDescription.setText("The accessibility service is enabled. You can now enable double tap to lock to quickly lock your screen by double tapping on empty areas.");
            additionalInfo.setText("This feature will use the accessibility service to lock your screen when you double tap on empty areas of the launcher.");
        } else {
            mainDescription.setText("To enable double tap to lock, you first need to enable Lunox's accessibility service in your device's settings.");
            additionalInfo.setText("The accessibility service is required for this feature to work. It only locks your screen and does not collect or share any data. Click Enable to open the settings.");
        }
    }


}
