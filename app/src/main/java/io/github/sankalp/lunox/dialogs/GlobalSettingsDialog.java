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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.sankalp.lunox.BuildConfig;
import io.github.sankalp.lunox.LauncherActivity;
import io.github.sankalp.lunox.R;
import io.github.sankalp.lunox.dialogs.DoubleTapLockDialog;
import io.github.sankalp.lunox.model.Apps;
import io.github.sankalp.lunox.utils.AccessibilityUtils;
import io.github.sankalp.lunox.utils.Constants;
import io.github.sankalp.lunox.utils.DbUtils;
import io.github.sankalp.lunox.utils.Utils;

import static io.github.sankalp.lunox.utils.Constants.BACKUP_REQUEST;
import static io.github.sankalp.lunox.utils.Constants.FONTS_REQUEST;
import static io.github.sankalp.lunox.utils.Constants.RESTORE_REQUEST;

/**
 * this the launcher setting Dialog
 */
public class GlobalSettingsDialog extends Dialog implements View.OnClickListener {


    //private static final String TAG = "Global";/**/
    private final LauncherActivity launcherActivity;
    private final Context context;
    private TextView freezeSize;
    private TextView doubleTapLockButton;
    private TextView searchBarGestureButton;

    public GlobalSettingsDialog(Context context, LauncherActivity launcherActivity) {
        super(context);
        this.context = context;
        this.launcherActivity = launcherActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_global_settings);

        findViewById(R.id.settings_themes).setOnClickListener(this);
        freezeSize = findViewById(R.id.settings_freeze_size);
        freezeSize.setOnClickListener(this);

        findViewById(R.id.settings_fonts).setOnClickListener(this);

        TextView reset = findViewById(R.id.settings_reset_to_defaults);
        reset.setOnClickListener(this);
        reset.setTextColor(Color.parseColor("#E53935"));
        findViewById(R.id.settings_backup).setOnClickListener(this);
        findViewById(R.id.settings_restore).setOnClickListener(this);
        findViewById(R.id.settings_alignment).setOnClickListener(this);
        findViewById(R.id.settings_padding).setOnClickListener(this);
        findViewById(R.id.settings_color_size).setOnClickListener(this);
        findViewById(R.id.settings_sort_app_by).setOnClickListener(this);
        findViewById(R.id.settings_restart_launcher).setOnClickListener(this);

        //TODO: remove this var
        TextView colorSniffer = findViewById(R.id.settings_color_sniffer);
        colorSniffer.setOnClickListener(this);

        if (!BuildConfig.enableColorSniffer) {
            if (DbUtils.isRandomColor()) {
                colorSniffer.setText(R.string.fixed_colors);
            } else
                colorSniffer.setText(R.string.random_colors);
        }

        findViewById(R.id.settings_frozen_apps).setOnClickListener(this);
        findViewById(R.id.settings_hidden_apps).setOnClickListener(this);

        // Double tap to lock setting
        doubleTapLockButton = findViewById(R.id.settings_double_tap_lock);
        doubleTapLockButton.setOnClickListener(this);

        // Search bar gesture toggle
        searchBarGestureButton = findViewById(R.id.settings_search_bar_gesture);
        searchBarGestureButton.setOnClickListener(this);
        updateSearchBarGestureButtonText();

        //reflect the DB value
        if (DbUtils.isSizeFrozen()) {
            freezeSize.setText(R.string.unfreeze_app_size);
        } else
            freezeSize.setText(R.string.freeze_apps_size);

        // Set double tap to lock button text
        updateDoubleTapLockButtonText();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.settings_fonts) {
            fontSelection(view);
        } else if (id == R.id.settings_themes) {
            showThemeDialog();
        } else if (id == R.id.settings_color_sniffer) {
            if (BuildConfig.enableColorSniffer)
                showColorSnifferDialog();
            else randomColor();
        } else if (id == R.id.settings_sort_app_by) {
            sortApps(view);
        } else if (id == R.id.settings_color_size) {
            showColorAndSizeDialog();
        } else if (id == R.id.settings_freeze_size) {
            freezeAppsSize();
        } else if (id == R.id.settings_hidden_apps) {
            hiddenApps();
        } else if (id == R.id.settings_frozen_apps) {
            frozenApps();
        } else if (id == R.id.settings_backup) {
            backup();
        } else if (id == R.id.settings_restore) {
            restore();
        } else if (id == R.id.settings_reset_to_defaults) {
            defaultSettings();
        } else if (id == R.id.settings_alignment) {
            setFlowLayoutAlignment(view);
        } else if (id == R.id.settings_padding) {
            launcherActivity.setPadding();
            cancel();
        } else if (id == R.id.settings_restart_launcher) {
            launcherActivity.recreate();
        } else if (id == R.id.settings_double_tap_lock) {
            toggleDoubleTapToLock();
        } else if (id == R.id.settings_search_bar_gesture) {
            toggleSearchBarGesture();
        }
    }

    private void sortApps(View view) {
        Context context;
        if (DbUtils.getTheme() == R.style.Wallpaper)
            context = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        else
            context = new ContextThemeWrapper(getContext(), DbUtils.getTheme());

        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.sort_apps_popups, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.menu_sort_by_name) {
                launcherActivity.sortApps(Constants.SORT_BY_NAME);
            } else if (id == R.id.menu_sort_by_opening_counts) {
                launcherActivity.sortApps(Constants.SORT_BY_OPENING_COUNTS);
            } else if (id == R.id.menu_sort_by_color) {
                launcherActivity.sortApps(Constants.SORT_BY_COLOR);
            } else if (id == R.id.menu_sort_by_size) {
                launcherActivity.sortApps(Constants.SORT_BY_SIZE);
            } else if (id == R.id.menu_sort_by_update_time) {
                launcherActivity.sortApps(Constants.SORT_BY_UPDATE_TIME);
            } else if (id == R.id.menu_sort_by_recent_use) {
                launcherActivity.sortApps(Constants.SORT_BY_RECENT_OPEN);
            }
            return true;
        });
        popupMenu.show();
    }

    private void showColorAndSizeDialog() {
        launcherActivity.setColorsAndSize();
        cancel();
    }

    private void setFlowLayoutAlignment(View view) {
        Context context;
        if (DbUtils.getTheme() == R.style.Wallpaper)
            context = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        else
            context = new ContextThemeWrapper(getContext(), DbUtils.getTheme());

        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.alignment_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.menu_center) {
                launcherActivity.setFlowLayoutAlignment(Gravity.CENTER | Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            } else if (id == R.id.menu_end) {
                launcherActivity.setFlowLayoutAlignment(Gravity.END | Gravity.CENTER_VERTICAL);
            } else if (id == R.id.menu_start) {
                launcherActivity.setFlowLayoutAlignment(Gravity.START | Gravity.CENTER_VERTICAL);
            }
            return true;
        });
        popupMenu.show();
    }

    private void randomColor() {
        boolean rColor = !DbUtils.isRandomColor();
        DbUtils.randomColor(rColor);
        cancel();
        if (rColor) {
            int color;
            for (Apps app : launcherActivity.mAppsList) {
                color = DbUtils.getAppColor(app.getActivityName());
                if (color == DbUtils.NULL_TEXT_COLOR) {
                    color = Utils.generateColorFromString(app.getActivityName());
                    app.getTextView().setTextColor(color);
                }
            }
        } else {
            launcherActivity.recreate();
        }
    }

    private void showColorSnifferDialog() {
        cancel();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("ryey.colorsniffer");

        // if color snifer app is not installed then send user to install it
        // else show color sniffer option
        if (intent == null) {
            //Change this to proper url , currently this also show BASTARD PLAY STORE
            Uri uri = Uri.parse("market://details?id=ryey.colorsniffer");
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else {
            new ColorSnifferDialog(getContext(), launcherActivity).show();

        }
    }

    private void freezeAppsSize() {
        boolean b = DbUtils.isSizeFrozen();
        DbUtils.freezeSize(!b);
        if (!b) {
            freezeSize.setText(R.string.unfreeze_app_size);
        } else
            freezeSize.setText(R.string.freeze_apps_size);
    }

    private void frozenApps() {
        launcherActivity.showFrozenApps();
        cancel();
    }

    //show hidden apps
    private void hiddenApps() {
        launcherActivity.showHiddenApps();
        cancel();
    }

    private void showThemeDialog() {
        cancel();
        new ThemeSelectorDialog(getContext(), launcherActivity).show();
    }

    private void defaultSettings() {
        DbUtils.clearDB();
        launcherActivity.recreate();
    }

    private void toggleDoubleTapToLock() {
        boolean currentState = DbUtils.isDoubleTapToLockEnabled();

        if (currentState) {
            // If currently enabled, just disable it directly
            DbUtils.setDoubleTapToLock(false);
            updateDoubleTapLockButtonText();
        } else {
            // If currently disabled, show the detailed dialog to explain and enable the feature
            DoubleTapLockDialog doubleTapDialog = new DoubleTapLockDialog(context, launcherActivity);
            doubleTapDialog.show();

            // Update button text when dialog is dismissed
            doubleTapDialog.setOnDismissListener(dialog -> updateDoubleTapLockButtonText());
        }
    }

    private void updateDoubleTapLockButtonText() {
        if (DbUtils.isDoubleTapToLockEnabled()) {
            doubleTapLockButton.setText(R.string.double_tap_to_lock_enabled);
        } else {
            doubleTapLockButton.setText(R.string.double_tap_to_lock_disabled);
        }
    }

    private void showAccessibilityServiceDialog() {
        Context ctx;
        if (DbUtils.getTheme() == R.style.Wallpaper)
            ctx = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        else
            ctx = new ContextThemeWrapper(getContext(), DbUtils.getTheme());

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Enable Accessibility Service");
        builder.setMessage("To use double tap to lock screen, please enable the Lunox Accessibility Service in Settings.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            AccessibilityUtils.openAccessibilitySettings(context);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void backup() {
        cancel();
        Intent intentBackupFiles;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intentBackupFiles = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        } else {
            intentBackupFiles = new Intent(Intent.ACTION_GET_CONTENT);
            ;
        }
        intentBackupFiles.addCategory(Intent.CATEGORY_OPENABLE);

        intentBackupFiles.setType("*/*");

        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HHSS", Locale.getDefault());
        df.format(new Date());
        String date = df.format(new Date());

        intentBackupFiles.putExtra(Intent.EXTRA_TITLE, "Backup_lunox_" + date);

        Intent intent = Intent.createChooser(intentBackupFiles, launcherActivity.getString(R.string.choose_old_backup_files));
        launcherActivity.startActivityForResult(intent, BACKUP_REQUEST);
    }

    private void restore() {
        cancel();
        Intent intentRestoreFiles;
        intentRestoreFiles = new Intent(Intent.ACTION_GET_CONTENT);
        intentRestoreFiles.addCategory(Intent.CATEGORY_OPENABLE);
        intentRestoreFiles.setType("*/*");
        Intent intent = Intent.createChooser(intentRestoreFiles, launcherActivity.getString(R.string.choose_old_backup_files));
        launcherActivity.startActivityForResult(intent, RESTORE_REQUEST);
    }

    private void setFonts() {
        cancel();
        Intent intentSetFonts = new Intent(Intent.ACTION_GET_CONTENT);

        intentSetFonts.addCategory(Intent.CATEGORY_OPENABLE);
        //intentSetFonts.setType("application/x-font-ttf");
        // intentSetFonts.setType("file/plain");
        intentSetFonts.setType("*/*");
        Intent intent = Intent.createChooser(intentSetFonts, "Choose Fonts");
        launcherActivity.startActivityForResult(intent, FONTS_REQUEST);

    }

    private void fontSelection(View view) {
        Context context;
        if (DbUtils.getTheme() == R.style.Wallpaper)
            context = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        else
            context = new ContextThemeWrapper(getContext(), DbUtils.getTheme());

        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.font_selection_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.menu_choose_fonts) {
                setFonts();
            } else if (id == R.id.menu_default_font) {
                if (DbUtils.isFontExists()) {
                    DbUtils.removeFont();
                    launcherActivity.setFont();
                    launcherActivity.loadApps();
                    cancel();
                }
            }
            return true;
        });
        popupMenu.show();
    }

    private void toggleSearchBarGesture() {
        boolean enabled = DbUtils.isSearchBarGestureEnabled();
        DbUtils.setSearchBarGestureEnabled(!enabled);
        updateSearchBarGestureButtonText();
    }

    private void updateSearchBarGestureButtonText() {
        if (DbUtils.isSearchBarGestureEnabled()) {
            searchBarGestureButton.setText(R.string.search_bar_gesture);
            searchBarGestureButton.setAlpha(1.0f);
        } else {
            searchBarGestureButton.setText(context.getString(R.string.search_bar_gesture) + " (Off)");
            searchBarGestureButton.setAlpha(0.5f);
        }
    }

}
