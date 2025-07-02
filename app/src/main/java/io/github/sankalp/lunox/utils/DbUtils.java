/*
 * Lunox
 * Copyright (C) 2025 Sankalp Tharu
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
import android.view.Gravity;

import java.io.InputStream;
import java.util.Map;

import io.github.sankalp.lunox.R;

/**
 * This is the our database class
 * This is purely based on Shared prefs bcz -
 * 1. faster than Sql Db
 * 2. low memory usage than Sql db
 * 3. low Cpu usage than sql db
 * 4. easy to add new column when updating the db
 * 5. easy to backup and restore.
 * 6. no overhead when updating db version.
 * <p>
 * <p>
 */
public class DbUtils {

    public static final int NULL_TEXT_SIZE = -1;
    public final static int NULL_TEXT_COLOR = -1;

    private static final String PADDING_TOP = "padding_top";
    private static final String RANDOM_COLOR_FOR_APPS = "random_color_for_apps";
    private static final String READ_WRITE_PERMISSION = "read_write_permission";
    private static final String LAUNCHER_FONTS = "launcher_fonts";
    private static final String LAUNCHER_THEME = "launcher_theme";
    private static final String LAUNCHER_FREEZE_SIZE = "launcher_freeze_size";
    private static final String APPS_COLOR_FROM_EXTERNAL_SOURCE = "external_app_color";
    //new addition
    private static final String FLOW_LAYOUT_ALIGNMENT = "flow_layout_alignment";
    private static final String MAX_APP_SIZE = "max_app_size";
    private static final String MIN_APP_SIZE = "max_app_size";
    private static final String PADDING_LEFT = "padding_left";
    private static final String PADDING_RIGHT = "padding_right";
    private static final String PADDING_BOTTOM = "padding_bottom";

    private static final String GLOBAL_SIZE_ADDITION_EXTRA = "global_size_addition_extra";
    private static final String APPS_COLORS_DEFAULT = "apps_color_default";
    private static final String APPS_SORTS_TYPE = "apps_sorts_types";
    private static final String DOUBLE_TAP_TO_LOCK = "double_tap_to_lock";
    private static final String SEARCH_BAR_GESTURE = "search_bar_gesture";


    public static void init(Context context) {
        SpUtils.getInstance().init(context);
    }

    public static void clearDB() {
        SpUtils.getInstance().clear();
    }

    public static Map<String, ?> getDBData() {
        return SpUtils.getInstance().getAll();
    }

    public static boolean loadDbFromFile(InputStream inputStream) {
        return SpUtils.getInstance().loadSharedPreferencesFromFile(inputStream);
    }


    public static void putAppOriginalName(String activityName, String value) {
        activityName = activityName.replaceAll("\\.", "_") + "_app_original_name";
        SpUtils.getInstance().putString(activityName, value);
    }

    public static void putAppName(String activityName, String value) {
        activityName = activityName.replaceAll("\\.", "_") + "_app_name";
        SpUtils.getInstance().putString(activityName, value);
    }

    public static void putAppSize(String activityName, int size) {
        activityName = activityName.replaceAll("\\.", "_") + "_size";
        SpUtils.getInstance().putInt(activityName, size);

    }

    public static void putAppColor(String activityName, int color) {
        activityName = activityName.replaceAll("\\.", "_") + "_color";
        SpUtils.getInstance().putInt(activityName, color);
    }


    public static void putAppColorImmediately(String activityName, int color) {
        activityName = activityName.replaceAll("\\.", "_") + "_color";
        SpUtils.getInstance().putIntCommit(activityName, color);
    }

    public static String getAppOriginalName(String activityName, String defaultValue) {
        activityName = activityName.replaceAll("\\.", "_") + "_app_original_name";
        return SpUtils.getInstance().getString(activityName, defaultValue);
    }

    public static String getAppName(String activityName, String defaultValue) {
        activityName = activityName.replaceAll("\\.", "_") + "_app_name";
        return SpUtils.getInstance().getString(activityName, defaultValue);
    }

    public static int getAppSize(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_size";
        return SpUtils.getInstance().getInt(activityName, NULL_TEXT_SIZE);
    }

    public static int getAppColor(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_color";
        return SpUtils.getInstance().getInt(activityName, NULL_TEXT_COLOR);
    }

    public static void hideApp(String activityName, boolean value) {
        activityName = activityName.replaceAll("\\.", "_") + "_hide";
        SpUtils.getInstance().putBoolean(activityName, value);
    }

    public static void freezeAppSize(String activityName, boolean value) {
        activityName = activityName.replaceAll("\\.", "_") + "_freeze";
        SpUtils.getInstance().putBoolean(activityName, value);
    }

    public static boolean isAppFrozen(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_freeze";
        return SpUtils.getInstance().getBoolean(activityName, false);

    }

    public static boolean isAppHidden(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_hide";
        return SpUtils.getInstance().getBoolean(activityName, false);

    }

    public static void removeColor(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_color";
        SpUtils.getInstance().remove(activityName);
    }

    public static void removeSize(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_size";
        SpUtils.getInstance().remove(activityName);
    }

    public static void removeAppName(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_app_name";
        SpUtils.getInstance().remove(activityName);
    }

    public static int getTheme() {
        int theme = SpUtils.getInstance().getInt(LAUNCHER_THEME, 2);
        if (theme == 1) {
            return R.style.Wallpaper;
        } else if (theme == 2) {
            return R.style.AppTheme;
        } else if (theme == 3) {
            return R.style.White;
        } else if (theme == 4) {
            return R.style.WhiteOnGrey;
        } else if (theme == 5) {
            return R.style.Black;
        } else if (theme == 6) {
            return R.style.BlackOnGrey;
        } else if (theme == 7) {
            return R.style.Hacker_green;
        } else if (theme == 8) {
            return R.style.Hacker_red;
        }
        return R.style.AppTheme;
    }

    public static void setTheme(int id) {
        int theme = 0;
        if (id == R.style.Wallpaper) {
            theme = 1;
        } else if (id == R.style.AppTheme) {
            theme = 2;
        } else if (id == R.style.White) {
            theme = 3;
        } else if (id == R.style.WhiteOnGrey) {
            theme = 4;
        } else if (id == R.style.Black) {
            theme = 5;
        } else if (id == R.style.BlackOnGrey) {
            theme = 6;
        } else if (id == R.style.Hacker_green) {
            theme = 7;
        } else if (id == R.style.Hacker_red) {
            theme = 8;
        }
        SpUtils.getInstance().putInt(LAUNCHER_THEME, theme);
    }

    public static String getFonts() {
        return SpUtils.getInstance().getString(LAUNCHER_FONTS, null);
    }

    public static void setFonts(String path) {
        SpUtils.getInstance().putString(LAUNCHER_FONTS, path);
    }


    public static void permissionRequired(boolean b) {
        SpUtils.getInstance().putBoolean(READ_WRITE_PERMISSION, b);
    }

    public static boolean isRandomColor() {
        return SpUtils.getInstance().getBoolean(RANDOM_COLOR_FOR_APPS, false);
    }

    public static void randomColor(boolean b) {
        SpUtils.getInstance().putBoolean(RANDOM_COLOR_FOR_APPS, b);
    }

    public static void freezeSize(boolean b) {
        SpUtils.getInstance().putBoolean(LAUNCHER_FREEZE_SIZE, b);
    }

    public static boolean isSizeFrozen() {
        return SpUtils.getInstance().getBoolean(LAUNCHER_FREEZE_SIZE, false);

    }


    public static boolean isExternalSourceColor() {
        return SpUtils.getInstance().getBoolean(APPS_COLOR_FROM_EXTERNAL_SOURCE, false);
    }

    public static void externalSourceColor(boolean b) {
        SpUtils.getInstance().putBoolean(APPS_COLOR_FROM_EXTERNAL_SOURCE, b);
    }

    public static int getAppColorExternalSource(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_external_color";
        return SpUtils.getInstance().getInt(activityName, NULL_TEXT_COLOR);
    }

    public static void putAppColorExternalSource(String activityName, int color) {
        activityName = activityName.replaceAll("\\.", "_") + "_external_color";
        SpUtils.getInstance().putInt(activityName, color);
    }

    public static int getFlowLayoutAlignment() {
        return SpUtils.getInstance().getInt(FLOW_LAYOUT_ALIGNMENT, Gravity.CENTER | Gravity.CENTER_VERTICAL);
    }

    public static void setFlowLayoutAlignment(int gravity) {
        SpUtils.getInstance().putInt(FLOW_LAYOUT_ALIGNMENT, gravity);
    }

    public static int getMaxAppSize() {
        return SpUtils.getInstance().getInt(MAX_APP_SIZE, Constants.MAX_TEXT_SIZE_FOR_APPS);
    }

    public static void setMaxAppSize(int size) {
        SpUtils.getInstance().putInt(MAX_APP_SIZE, size);
    }

    public static int getMinAppSize() {
        return SpUtils.getInstance().getInt(MIN_APP_SIZE, Constants.MIN_TEXT_SIZE_FOR_APPS);
    }

    public static void setMinAppSize(int size) {
        SpUtils.getInstance().putInt(MIN_APP_SIZE, size);
    }

    ///////
    public static int getPaddingLeft() {
        return SpUtils.getInstance().getInt(PADDING_LEFT, 0);
    }

    public static void setPaddingLeft(int padding) {
        SpUtils.getInstance().putInt(PADDING_LEFT, padding);
    }

    public static int getPaddingRight() {
        return SpUtils.getInstance().getInt(PADDING_RIGHT, 0);
    }

    public static void setPaddingRight(int padding) {
        SpUtils.getInstance().putInt(PADDING_RIGHT, padding);
    }

    public static int getPaddingTop() {
        return SpUtils.getInstance().getInt(PADDING_TOP, 0);
    }

    public static void setPaddingTop(int padding) {
        SpUtils.getInstance().putInt(PADDING_TOP, padding);
    }

    public static int getPaddingBottom() {
        return SpUtils.getInstance().getInt(PADDING_BOTTOM, 0);
    }

    public static void setPaddingBottom(int padding) {
        SpUtils.getInstance().putInt(PADDING_BOTTOM, padding);
    }


    public static void setGroupPrefix(String activityName, String prefix) {
        activityName = activityName.replaceAll("\\.", "_") + "_group_prefix";
        SpUtils.getInstance().putString(activityName, prefix);
    }

    public static void setCategories(String activityName, String categories) {
        activityName = activityName.replaceAll("\\.", "_") + "_categories";
        SpUtils.getInstance().putString(activityName, categories);
    }

    public static void setOpeningCounts(String activityName, int count) {
        activityName = activityName.replaceAll("\\.", "_") + "_opening_counts";
        SpUtils.getInstance().putString(activityName, codeCount(count));
    }

    public static String getGroupPrefix(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_group_prefix";
        return SpUtils.getInstance().getString(activityName);
    }

    public static String getCategories(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_categories";
        return SpUtils.getInstance().getString(activityName);
    }

    public static int getOpeningCounts(String activityName) {
        activityName = activityName.replaceAll("\\.", "_") + "_opening_counts";
        return decodeCount(SpUtils.getInstance().getString(activityName, null));
    }

    public static int getGlobalSizeAdditionExtra() {
        return SpUtils.getInstance().getInt(GLOBAL_SIZE_ADDITION_EXTRA, 0);
    }

    public static void setGlobalSizeAdditionExtra(int extra) {
        SpUtils.getInstance().putInt(GLOBAL_SIZE_ADDITION_EXTRA, extra);
    }

    public static int getAppsColorDefault() {
        return SpUtils.getInstance().getInt(APPS_COLORS_DEFAULT, NULL_TEXT_COLOR);
    }

    public static void setAppsColorDefault(int color) {
        SpUtils.getInstance().putInt(APPS_COLORS_DEFAULT, color);
    }

    public static void removeFont() {
        SpUtils.getInstance().remove(LAUNCHER_FONTS);
    }

    public static boolean isFontExists() {
        return SpUtils.getInstance().contains(LAUNCHER_FONTS);
    }

    public static int getSortsTypes() {
        return SpUtils.getInstance().getInt(APPS_SORTS_TYPE, 1);
    }

    public static void setAppsSortsType(int type) {
        SpUtils.getInstance().putInt(APPS_SORTS_TYPE, type);
    }



    /**
     * Check if double tap to lock is enabled
     */
    public static boolean isDoubleTapToLockEnabled() {
        return SpUtils.getInstance().getBoolean(DOUBLE_TAP_TO_LOCK, false);
    }

    /**
     * Set double tap to lock preference
     */
    public static void setDoubleTapToLock(boolean enabled) {
        SpUtils.getInstance().putBoolean(DOUBLE_TAP_TO_LOCK, enabled);
    }

    /**
     * Check if search bar gesture is enabled
     */
    public static boolean isSearchBarGestureEnabled() {
        return SpUtils.getInstance().getBoolean(SEARCH_BAR_GESTURE, true);
    }

    /**
     * Set search bar gesture preference
     */
    public static void setSearchBarGestureEnabled(boolean enabled) {
        SpUtils.getInstance().putBoolean(SEARCH_BAR_GESTURE, enabled);
    }

    //  a simple ciphered counter: "opening counter" is a private thing
    // rest is on device security
    private static String codeCount(int count) {
        char[] map = "(e*+@_$k&m".toCharArray();
        int info = count ^ 86194;
        StringBuilder enc = new StringBuilder();
        while (info > 0) {
            int c = info % 10;
            info /= 10;
            enc.append(map[c]);
        }
        return enc.toString();
    }

    private static int decodeCount(String text) {
        if (text == null) return 0;

        String map = "(e*+@_$k&m";
        int value = 0;
        for (int i = text.length() - 1; i >= 0; i--) {
            value *= 10;
            value += map.indexOf(text.charAt(i));
        }
        return value ^ 86194;
    }


}
