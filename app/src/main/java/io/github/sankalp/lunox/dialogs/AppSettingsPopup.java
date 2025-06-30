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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import io.github.sankalp.lunox.LauncherActivity;
import io.github.sankalp.lunox.R;
import io.github.sankalp.lunox.utils.DbUtils;
import io.github.sankalp.lunox.views.textview.AppTextView;

/**
 * App settings dialog that shows options for individual apps
 */
public class AppSettingsPopup extends Dialog implements View.OnClickListener {

    private final LauncherActivity launcherActivity;
    private final Context context;
    private final String activityName;
    private final AppTextView appTextView;
    private TextView freezeSize;
    private TextView hideApp;
    private TextView resetToDefault;
    private TextView uninstallApp;

    public AppSettingsPopup(Context context, String activityName, AppTextView appTextView, LauncherActivity launcherActivity) {
        super(context);
        this.context = context;
        this.activityName = activityName;
        this.appTextView = appTextView;
        this.launcherActivity = launcherActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_app_settings);

        // Set the dialog background to match popup style with border
        Window window = getWindow();
        if (window != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                window.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popup_background, null));
            } else {
                window.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popup_background));
            }
        }

        // Set up click listeners
        findViewById(R.id.app_settings_color).setOnClickListener(this);
        findViewById(R.id.app_settings_rename).setOnClickListener(this);

        freezeSize = findViewById(R.id.app_settings_freeze_size);
        freezeSize.setOnClickListener(this);

        findViewById(R.id.app_settings_app_info).setOnClickListener(this);
        findViewById(R.id.app_settings_reset_color).setOnClickListener(this);

        hideApp = findViewById(R.id.app_settings_hide);
        hideApp.setOnClickListener(this);

        resetToDefault = findViewById(R.id.app_settings_reset_to_default);
        resetToDefault.setOnClickListener(this);
        resetToDefault.setTextColor(Color.parseColor("#E53935"));

        uninstallApp = findViewById(R.id.app_settings_uninstall);
        uninstallApp.setOnClickListener(this);
        uninstallApp.setTextColor(Color.parseColor("#E53935"));

        // Set proper text based on DB value
        if (DbUtils.isAppFrozen(activityName)) {
            freezeSize.setText(R.string.unfreeze_size);
        } else {
            freezeSize.setText(R.string.freeze_size);
        }

        // Handle shortcut-specific settings
        if (appTextView.isShortcut()) {
            uninstallApp.setText(R.string.remove);
            hideApp.setVisibility(View.GONE);
            findViewById(R.id.app_settings_rename).setVisibility(View.GONE);
            findViewById(R.id.app_settings_app_info).setVisibility(View.GONE);
        } else {
            hideApp.setTextColor(Color.parseColor("#E53935"));
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.app_settings_color) {
            launcherActivity.changeColorSize(activityName, appTextView);
            cancel();
        } else if (id == R.id.app_settings_rename) {
            launcherActivity.renameApp(activityName, appTextView.getText().toString());
            cancel();
        } else if (id == R.id.app_settings_freeze_size) {
            launcherActivity.freezeAppSize(activityName);
            cancel();
        } else if (id == R.id.app_settings_app_info) {
            launcherActivity.showAppInfo(activityName);
            cancel();
        } else if (id == R.id.app_settings_reset_color) {
            launcherActivity.resetAppColor(activityName);
            cancel();
        } else if (id == R.id.app_settings_hide) {
            launcherActivity.hideApp(activityName);
            cancel();
        } else if (id == R.id.app_settings_reset_to_default) {
            launcherActivity.resetApp(activityName);
            cancel();
        } else if (id == R.id.app_settings_uninstall) {
            if (appTextView.isShortcut()) {
                launcherActivity.removeShortcut(appTextView);
            } else {
                launcherActivity.uninstallApp(activityName);
            }
            cancel();
        }
    }
}
