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

package io.github.sankalp.lunox;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.ArrayMap;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.apmem.tools.layouts.FlowLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.github.sankalp.lunox.dialogs.AppSettingsPopup;
import io.github.sankalp.lunox.dialogs.ColorSizeDialog;
import io.github.sankalp.lunox.dialogs.DoubleTapLockDialog;
import io.github.sankalp.lunox.dialogs.FrozenAppsDialogs;
import io.github.sankalp.lunox.dialogs.GlobalColorSizeDialog;
import io.github.sankalp.lunox.dialogs.GlobalSettingsDialog;
import io.github.sankalp.lunox.dialogs.HiddenAppsDialogs;
import io.github.sankalp.lunox.dialogs.OnboardingTutorialDialog;
import io.github.sankalp.lunox.dialogs.PaddingDialog;
import io.github.sankalp.lunox.dialogs.RenameInputDialogs;
import io.github.sankalp.lunox.model.Apps;
import io.github.sankalp.lunox.model.Shortcut;
import io.github.sankalp.lunox.utils.CrashUtils;
import io.github.sankalp.lunox.utils.DbUtils;

import io.github.sankalp.lunox.utils.AccessibilityUtils;
import io.github.sankalp.lunox.utils.Gestures;
import io.github.sankalp.lunox.utils.NotificationPanelManager;
import io.github.sankalp.lunox.utils.ShortcutUtils;
import io.github.sankalp.lunox.utils.Utils;
import io.github.sankalp.lunox.views.textview.AppTextView;

import static android.content.Intent.ACTION_PACKAGE_ADDED;
import static android.content.Intent.ACTION_PACKAGE_CHANGED;
import static android.content.Intent.ACTION_PACKAGE_REMOVED;
import static android.content.Intent.ACTION_PACKAGE_REPLACED;
import static io.github.sankalp.lunox.utils.Constants.BACKUP_REQUEST;
import static io.github.sankalp.lunox.utils.Constants.COLOR_SNIFFER_REQUEST;
import static io.github.sankalp.lunox.utils.Constants.DEFAULT_COLOR_FOR_APPS;
import static io.github.sankalp.lunox.utils.Constants.DEFAULT_TEXT_SIZE_NORMAL_APPS;
import static io.github.sankalp.lunox.utils.Constants.DEFAULT_TEXT_SIZE_OFTEN_APPS;
import static io.github.sankalp.lunox.utils.Constants.FONTS_REQUEST;
import static io.github.sankalp.lunox.utils.Constants.RESTORE_REQUEST;
import static io.github.sankalp.lunox.utils.Constants.SORT_BY_COLOR;
import static io.github.sankalp.lunox.utils.Constants.SORT_BY_NAME;
import static io.github.sankalp.lunox.utils.Constants.SORT_BY_OPENING_COUNTS;
import static io.github.sankalp.lunox.utils.Constants.SORT_BY_RECENT_OPEN;
import static io.github.sankalp.lunox.utils.Constants.SORT_BY_SIZE;
import static io.github.sankalp.lunox.utils.Constants.SORT_BY_UPDATE_TIME;

/**
 * --------------------------------------------------------------------------
 * People can criticise me all the time they want,
 * by this is what I am and I won't change the way I live for them.
 * I live the way I want to flow.
 * -------------------------------------------------------------------------
 * -
 * If we don't transform the world, who will? If not now, when?
 * If you have something to give, give it now
 * -
 * Do your little bit of good where you are;
 * it's those little bits of good put together that overwhelm the world."
 * -
 * Don't just think, do it. Now it is you turn,  do it now, go fast and open pull request
 * -
 * ----------------------------------------------------------------------------
 * This Activity extends the api 14 Activity Class not latest AppCompatActivity
 * Reason: Small apk size
 */
@SuppressLint("NonConstantResourceId")
public class LauncherActivity extends Activity implements View.OnClickListener,
        View.OnLongClickListener,
        Gestures.OnSwipeListener,
        Gestures.OnDoubleTapListener {


    //region Field declarations
    public ArrayList<Apps> mAppsList;
    // home layout
    private FlowLayout mHomeLayout;
    // when search bar is appear this will be true and show search result
    private boolean searching = false;
    //todo: save this to db
    private int recentlyUsedCounter = 0;
    private final String TAG = "LauncherActivity";
    // broadcast receiver
    private BroadcastReceiver broadcastReceiverAppInstall;
    private BroadcastReceiver broadcastReceiverShortcutInstall;
    private Typeface mTypeface;
    //multi dialogs
    private Dialog dialogs;
    //search box
    private EditText mSearchBox;
    private LinearLayout setDefaultLayout;
    private Button setDefaultButton;

    private InputMethodManager imm;
    // gesture detector
    private Gestures detector;
    private ShortcutUtils shortcutUtils;

    // For handling long press on empty areas
    private GestureDetector longPressDetector;

    private final TextWatcher mTextWatcher= new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mSearchTask=new SearchTask();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //do here search
           mSearchTask.execute(charSequence);
        }

        @Override
        public void afterTextChanged(Editable editable) {

            // do everything
        }
    };
    private SearchTask mSearchTask;
    private Dialog globalSettingsDialog;
    private TextView bottomLabel;
    //endregion

    private void showSearchResult(ArrayList<Apps> filteredApps) {
        if (!searching) return;

        mHomeLayout.removeAllViews();
        mHomeLayout.setPadding(0, 150, 0, 0);

        // Performance improvement: Use batch operations for search results
        List<View> searchViews = new ArrayList<>();
        for (Apps apps : filteredApps) {
            searchViews.add(apps.getTextView());
        }

        if (mHomeLayout instanceof FlowLayout) {
            ((FlowLayout) mHomeLayout).addViewsBatch(searchViews);
        } else {
            // Fallback to individual additions
            for (View view : searchViews) {
                mHomeLayout.addView(view, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //pass touch event to detector
        detector.onTouchEvent(ev);
        longPressDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Helper method to find the view at the given coordinates
     */
    private View findViewAt(float x, float y) {
        // Convert coordinates to the home layout's coordinate system
        int[] location = new int[2];
        mHomeLayout.getLocationOnScreen(location);

        // Check if the touch is within the home layout bounds
        if (x >= location[0] && x <= location[0] + mHomeLayout.getWidth() &&
            y >= location[1] && y <= location[1] + mHomeLayout.getHeight()) {

            // Check each child view in the home layout
            for (int i = 0; i < mHomeLayout.getChildCount(); i++) {
                View child = mHomeLayout.getChildAt(i);
                child.getLocationOnScreen(location);

                if (x >= location[0] && x <= location[0] + child.getWidth() &&
                    y >= location[1] && y <= location[1] + child.getHeight()) {
                    return child;
                }
            }
        }

        return null; // No specific view found, it's empty space
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize the shared prefs may be done in application class
        DbUtils.init(this);
        shortcutUtils = new ShortcutUtils(this);

        if (BuildConfig.DEBUG) {
            new CrashUtils(getApplicationContext(), "");
        }

        int theme = DbUtils.getTheme();
        //theme must be set before setContentView
        setTheme(theme);

        setContentView(R.layout.activity_launcher);

        // set the status bar color as per theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setNavigationAndStatusBarColor(theme);
        }
        // set the fonts
        setFont();

        mHomeLayout = findViewById(R.id.home_layout);
        mHomeLayout.setOnLongClickListener(this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //our search box
        mSearchBox = findViewById(R.id.search_box);
        //setup listeners on mSearchBox
        setSearchBoxListeners();

        //set alignment default is center|center_vertical
        mHomeLayout.setGravity(DbUtils.getFlowLayoutAlignment());

        //set padding ..
        mHomeLayout.setPadding(DbUtils.getPaddingLeft(), DbUtils.getPaddingTop(), DbUtils.getPaddingRight(), DbUtils.getPaddingBottom());

        detector = new Gestures(this, this, this);

        // Initialize long press detector for empty areas
        longPressDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                // Check if the touch is on an empty area (not on an app)
                View touchedView = findViewAt(e.getX(), e.getY());
                if (!(touchedView instanceof AppTextView)) {
                    if (globalSettingsDialog == null || !globalSettingsDialog.isShowing()) {
                        globalSettingsDialog = new GlobalSettingsDialog(LauncherActivity.this, LauncherActivity.this);
                        globalSettingsDialog.setOnDismissListener(d -> globalSettingsDialog = null);
                        globalSettingsDialog.show();
                    }
                }
            }
        });

        // initGestures();

        // No permissions needed for basic launcher functionality

        // loads the apps
        loadApps();
        // register the receiver for installed, uninstall, update apps and shortcut pwa add
        registerForReceivers();

        setDefaultLayout = findViewById(R.id.set_default_layout);
        setDefaultButton = findViewById(R.id.set_default_button);
        setDefaultButton.setOnClickListener(this);

        // Show onboarding tutorial for first-time users
        showOnboardingTutorialIfNeeded();

        bottomLabel = findViewById(R.id.bottom_label);
    }

    private void setSearchBoxListeners() {
        mSearchBox.addTextChangedListener(mTextWatcher);
        mSearchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mSearchBox.setVisibility(View.GONE);
                imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
                //do something on clicking enter
                return true;
            }
            return false;
        });
    }

    /**
     * set the color of status bar and navigation bar as per theme
     * if theme color is light then pass this to system so status icon color will turn into black
     *
     * @param theme current theme applied to launcher
     */

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setNavigationAndStatusBarColor(int theme) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (theme == R.style.White || theme == R.style.WhiteOnGrey || theme == R.style.BlackOnGrey) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    public void setFont() {
        // get and set fonts
        String fontsPath = DbUtils.getFonts();
        if (fontsPath == null) {
            // Use system font instead of custom TTF to reduce APK size
            mTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        } else {
            try {
                mTypeface = Typeface.createFromFile(fontsPath);
            } catch (Exception i) {
                // Fallback to system font instead of custom TTF
                mTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
            }
        }

    }

    public void loadApps() {
        // get the apps installed on devices;
        final Intent startupIntent = new Intent(Intent.ACTION_MAIN, null);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);

        // For Android 11+ (API 30+), also try alternative method if first method returns few apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && activities.size() < 10) {
            // Try alternative method for better app detection
            List<ResolveInfo> alternativeActivities = pm.queryIntentActivities(startupIntent, PackageManager.MATCH_ALL);
            if (alternativeActivities.size() > activities.size()) {
                activities = alternativeActivities;
            }
        }

        // check whether our app list is already initialized if yes then clear this(when new app or shortcut installed)
        if (mAppsList != null) {
            mAppsList.clear();
            mHomeLayout.removeAllViews();
            mHomeLayout.removeAllViewsInLayout();
        }

        // shortcut or pwa counts
        final int installedShortcut = shortcutUtils.getShortcutCounts();

        // Log.d(TAG, "loadApps: install shortcut sizes::" + installedShortcut);
        final int appsCount = activities.size();

        mAppsList = new ArrayList<>(appsCount + installedShortcut);

        // get the most used apps
        // a list of app that are popular on f-droid and some of my apps
        List<String> oftenApps = Utils.getOftenAppsList();
        List<String> coloredAppsList = Utils.getColoredAppsList();

        String packageName, appName;
        int color, textSize;
        boolean hide;
        // iterate over each app and initialize app list
        for (ResolveInfo resolveInfo : activities) {

            packageName = resolveInfo.activityInfo.packageName;
            // activity name as com.example/com.example.MainActivity
            String activity = packageName + "/" + resolveInfo.activityInfo.name;
            /// save the app original name so that we can use this later e.g if user change
            /// the app name then we have the name in DB
            DbUtils.putAppOriginalName(activity, resolveInfo.loadLabel(pm).toString());
            // check whether user set the custom app name for eg. long name to small name
            appName = DbUtils.getAppName(activity, resolveInfo.loadLabel(pm).toString());
            // is app is hidden by user
            hide = DbUtils.isAppHidden(activity);
            // get the app text size
            textSize = DbUtils.getAppSize(activity);

            // check if text size is null then set the size to default size
            // size is null(-1) when user installed this app
            if (textSize == DbUtils.NULL_TEXT_SIZE) {
                if (oftenApps.contains(packageName)) {
                    textSize = DEFAULT_TEXT_SIZE_OFTEN_APPS;
                } else {
                    textSize = DEFAULT_TEXT_SIZE_NORMAL_APPS;
                }

                /// DbUtils.putAppSize(activity, textSize);
            }

            // get app color
            color = DbUtils.getAppColor(activity);

            /*Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            color= Utils.getDominantColor(Utils.drawableToBitmap(icon));*/


            // check for default color : set default colors if random color is not set
            if (!DbUtils.isRandomColor() && color == DbUtils.NULL_TEXT_COLOR) {
                color = DbUtils.getAppsColorDefault();
                if (coloredAppsList.contains(packageName)) {
                    color = Utils.getColor();
                }

            }
            // whether app size is frozen
            boolean freeze = DbUtils.isAppFrozen(activity);

            // this is a separate implementation of ColorSniffer app
            // if User set the color from external app like ColorSniffer
            // then use that colors
            if (BuildConfig.enableColorSniffer) {
                if (DbUtils.isExternalSourceColor() && color == DbUtils.NULL_TEXT_COLOR) {
                    color = DbUtils.getAppColorExternalSource(activity);
                }
            } else if (DbUtils.isRandomColor() && color == DbUtils.NULL_TEXT_COLOR) {
                color = Utils.generateColorFromString(appName);
            }


            int openingCounts = DbUtils.getOpeningCounts(activity);

            int updateTime;
            try {
                //ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                long time = pm.getPackageInfo(packageName, 0).lastUpdateTime;
                time = time / 10000;
                updateTime = (int) time;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                updateTime = 0;
            }
            // save all and add this is to app list
            mAppsList.add(new Apps(false, activity, appName, getCustomView(), color, textSize, hide, freeze, openingCounts, updateTime));

        }

        // now adds Shortcut
        // shortcut are stored in DB, android doesn't store them

        ArrayList<Shortcut> shortcuts = shortcutUtils.getAllShortcuts();
        if (shortcuts != null) {
            for (Shortcut s : shortcuts) {

                // shortcut only have URI
                String uri = s.getUri();
                // shortcut name
                String sName = s.getName();

               /* if (uri.isEmpty()) {
                    ++installedShortcut;
                    continue;
                }*/


                // this is the unique code for each uri
                // let store them in activity field app
                // As we have to store some uniquely identified info in Db
                // this be used as key as i have done for Each apps(see above)
                // Usually URI sting is too long and so it will take more memory and storage
                String sActivity = String.valueOf(Utils.hash(uri));

                // get color and size for this shortcut
                int sColor = DbUtils.getAppColor(sActivity);
                int sSize = DbUtils.getAppSize(sActivity);

                if (sSize == DbUtils.NULL_TEXT_SIZE) {
                    sSize = DEFAULT_TEXT_SIZE_NORMAL_APPS;
                }

                if (sColor == DbUtils.NULL_TEXT_COLOR) {
                    if (DbUtils.isRandomColor()) {
                        sColor = Utils.generateColorFromString(sName);
                    } else {
                        sColor = DbUtils.getAppsColorDefault();
                    }
                }

                boolean sFreeze = DbUtils.isAppFrozen(sActivity);
                int sOpeningCount = DbUtils.getOpeningCounts(sActivity);

                // add this shortcut to list
                // currently shortcut hide is disabled
                mAppsList.add(new Apps(true, uri, sName, getCustomView(), sColor, sSize, false, sFreeze, sOpeningCount, 0));

            }
        }

        // now sort the app list
        // and display this
        sortApps(DbUtils.getSortsTypes());
    }

    /**
     * @param type sorting type
     */
    public void sortApps(final int type) {
        new SortTask().execute(type);
    }

    // the text view and set the various parameters
    //TODO: new animated field for this(test randomly)
    private AppTextView getCustomView() {
        //  AnimatedTextView textView=new AnimatedTextView(this);
        // textView.setColorSpace(15);
        AppTextView textView = new AppTextView(this);
        textView.setOnClickListener(this);
        textView.setOnLongClickListener(this);
        textView.setPadding(10, 0, 4, -2);
        textView.setTypeface(mTypeface);
        return textView;
    }

    // app text is clicked
    // so launch the app
    @Override
    public void onClick(View view) {
        String activityName = null;
        if (view.getTag() != null) {
            activityName = view.getTag().toString();
        }

        if (view.equals(mSearchBox)) {
            //do nothing
            return;
        } else if (view.equals(setDefaultButton)) {
            Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            startActivity(intent);
        } else if (view instanceof AppTextView) {
            // get the activity
            if (searching) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
                mSearchBox.setVisibility(View.GONE);
            }

            AppTextView appTextView = (AppTextView) view;

            if (appTextView.isShortcut()) {
                try {
                    Intent intent = Intent.parseUri(appTextView.getUri(), 0);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    appOpened(activityName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //Notes to me:if view store package and component name then this could reduce this splits
                String[] strings = activityName.split("/");
                try {
                    final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.setClassName(strings[0], strings[1]);
                    intent.setComponent(new ComponentName(strings[0], strings[1]));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    // tell the our db that app is opened
                    appOpened(activityName);
                } catch (Exception ignore) {
                    //  Log.e(TAG, "onClick: exception::" + ignore);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDefaultLauncher();

        // Check if we need to reload apps (e.g., after returning from settings)
        if (mAppsList != null && mAppsList.size() < 10) {
            // If we have very few apps, try reloading in case permissions were granted
            loadApps();
        }

        // Check if search box is visible and hide it if needed
        if (mSearchBox.getVisibility() == View.VISIBLE) {
            mSearchBox.setVisibility(View.GONE);
            mHomeLayout.setPadding(DbUtils.getPaddingLeft(), DbUtils.getPaddingTop(), DbUtils.getPaddingRight(), DbUtils.getPaddingBottom());
        }

        // Reset search state
        searching = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (dialogs != null) {
            dialogs.dismiss();
            dialogs = null;
        }

        closeGlobalSettingsDialog();

        if (mSearchTask!=null) {
            mSearchTask.cancel(true);
            mSearchTask=null;
        }
    }

    //show the option on long click
    @Override
    public boolean onLongClick(View view) {
        if (view instanceof AppTextView) {
            // show app setting
            showPopup((String) view.getTag(), (AppTextView) view);
        } else if (view instanceof FlowLayout) {
            // show launcher setting for FlowLayout (this handles long press on FlowLayout itself)
            if (globalSettingsDialog == null || !globalSettingsDialog.isShowing()) {
                globalSettingsDialog = new GlobalSettingsDialog(this, this);
                globalSettingsDialog.setOnDismissListener(d -> globalSettingsDialog = null);
                globalSettingsDialog.show();
            }
        }
        return true;
    }

    private void showPopup(String activityName, AppTextView view) {
        // Close global settings dialog if it's open when showing app settings
        closeGlobalSettingsDialog();
        // show app settings dialog instead of popup menu
        AppSettingsPopup popup = new AppSettingsPopup(this, activityName, view, this);
        popup.show();
    }

    //reset the app color to default color;
    public void resetAppColor(String activityName) {
        DbUtils.removeColor(activityName);
        boolean sortNeeded = (DbUtils.getSortsTypes() == SORT_BY_COLOR);
        addAppAfterReset(activityName, sortNeeded);
    }

    //  add a new app: generally called after reset
    private void addAppAfterReset(String activityName, boolean sortNeeded) {
        for (ListIterator<Apps> iterator = mAppsList.listIterator(); iterator.hasNext(); ) {
            Apps app = iterator.next();
            if (app.getActivityName().equalsIgnoreCase(activityName)) {
                iterator.remove();
                //now add new App
                int color;
                if (DbUtils.isRandomColor()) {
                    color = Utils.generateColorFromString(activityName);
                } else {
                    color = DbUtils.getAppsColorDefault();
                }
                String appOriginalName = DbUtils.getAppOriginalName(activityName, "");
                String appName = DbUtils.getAppName(activityName, appOriginalName);
                int openingCounts = DbUtils.getOpeningCounts(activityName);
                boolean hide = app.isHidden();
                boolean freezeSize = app.isSizeFrozen();
                int appUpdateTime = app.getUpdateTime();
                Apps newApp = new Apps(app.isShortcut(), activityName, appName, getCustomView(), color, DEFAULT_TEXT_SIZE_NORMAL_APPS, hide, freezeSize, openingCounts, appUpdateTime);

                //mAppsList.add(newApp);
                iterator.add(newApp);
                if (sortNeeded)
                    sortApps(DbUtils.getSortsTypes());
                break;
            }
        }
    }

    // as method name suggest
    public void freezeAppSize(String activityName) {
        boolean b = DbUtils.isAppFrozen(activityName);
        for (Apps apps : mAppsList) {
            if (activityName.equalsIgnoreCase(apps.getActivityName())) {
                apps.setFreeze(!b);
            }
        }

    }

    // as method name suggest
    public void hideApp(String activityName) {
        for (Apps apps : mAppsList) {
            if (activityName.equalsIgnoreCase(apps.getActivityName())) {
                apps.setAppHidden(true);
            }
        }

    }

    // show the app rename Dialog
    public void renameApp(String activityName, String appName) {
        showDialog(new RenameInputDialogs(this, activityName, appName, this), true);
    }

    // this is called by RenameInput.class Dialog when user set the name and sort the apps
    public void onAppRenamed(String activityName, String appNewName) {
        for (Apps app : mAppsList) {
            if (app.getActivityName().equalsIgnoreCase(activityName)) {
                app.setAppName(appNewName.trim());
                if (SORT_BY_NAME == DbUtils.getSortsTypes()) {
                    sortApps(SORT_BY_NAME);
                }
                break;
            }
        }
    }

    // reset the app
    public void resetApp(String activityName) {
        DbUtils.removeAppName(activityName);
        DbUtils.removeColor(activityName);
        DbUtils.removeSize(activityName);
        addAppAfterReset(activityName, true);
    }

    public void showAppInfo(String activityName) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activityName.split("/")[0]));
        startActivity(intent);
    }

    public void uninstallApp(String activityName) {
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + activityName.split("/")[0]));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, 97);
    }

    //show dialog(i.e a color seek bar) for change color
    public void changeColorSize(String activityName, TextView view) {
        int color = DbUtils.getAppColor(activityName);
        if (color == DbUtils.NULL_TEXT_COLOR) {
            color = view.getCurrentTextColor();
        }

        int size = DbUtils.getAppSize(activityName);
        if (size == DbUtils.NULL_TEXT_SIZE) {
            for (Apps apps : mAppsList) {
                if (apps.getActivityName().equals(activityName)) {
                    size = apps.getSize();
                    break;
                }
            }
        }
        showDialog(new ColorSizeDialog(this, activityName, color, view, size), true);
    }

    //TO1DO: multi thread check for memory leaks if any, or check any bad behaviour;
    private void appOpened(String activity) {
        /* new Thread() {
            @Override
            public void run() {
                super.run();*/
        for (Apps apps : mAppsList) {
            if (apps.getActivityName().equalsIgnoreCase(activity)) {
                apps.increaseOpeningCounts();// save to Db that app is opened by user
                recentlyUsedCounter++;
                apps.setRecentUsedWeight(recentlyUsedCounter);

                if (DbUtils.getSortsTypes() == SORT_BY_OPENING_COUNTS) {
                    int counter = apps.getOpeningCounts();
                    if (counter % 5 == 0) {
                        sortApps(SORT_BY_OPENING_COUNTS);
                    }
                } else if (DbUtils.getSortsTypes() == SORT_BY_RECENT_OPEN) {
                    sortApps(SORT_BY_RECENT_OPEN);
                }

                // increase the app view size if not frozen
                if (!DbUtils.isSizeFrozen() && !DbUtils.isAppFrozen(activity)) {
                    int size = DbUtils.getAppSize(activity);
                    size += 2;
                    apps.setSize(size);
                    if (DbUtils.getSortsTypes() == SORT_BY_SIZE) {
                        sortApps(SORT_BY_SIZE);
                    }
                }


                break;
            }
        }
        /*   }
        }.start();*/
    }

    @Override
    public void onBackPressed() {
        mSearchBox.setVisibility(View.GONE);

        if (searching) {
            //check this line
            imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);

            searching = false;
            if (mSearchTask!=null) {
                mSearchTask.cancel(true);
                mSearchTask=null;
            }
            mHomeLayout.setPadding(DbUtils.getPaddingLeft(), DbUtils.getPaddingTop(), DbUtils.getPaddingRight(), DbUtils.getPaddingBottom());
            sortApps(DbUtils.getSortsTypes());
        }
    }

    // register the receiver
    // when new app installed, app updated and app uninstalled launcher have to reflect it
    private void registerForReceivers() {
        /*if (broadcastReceiverShortcutInstall!=null){
            unregisterReceiver(broadcastReceiverShortcutInstall);
        }
        if (broadcastReceiverAppInstall!=null){
            unregisterReceiver(broadcastReceiverAppInstall);
        }*/
        //app install and uninstall receiver

        //  Log.d("WTF", "registerForReceivers: called ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PACKAGE_ADDED);
        intentFilter.addAction(ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        if (broadcastReceiverAppInstall == null) {
            broadcastReceiverAppInstall = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    loadApps();
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(broadcastReceiverAppInstall, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(broadcastReceiverAppInstall, intentFilter);
            }
        }

        //shortcut install receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.launcher.action.INSTALL_SHORTCUT");
        filter.addAction("com.android.launcher.action.CREATE_SHORTCUT");

        if (broadcastReceiverShortcutInstall == null) {
            broadcastReceiverShortcutInstall = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Intent shortcutIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                    String uri = shortcutIntent.toUri(0);
                    if (shortcutIntent.getAction() == null) {
                        shortcutIntent.setAction(Intent.ACTION_VIEW);

                    }
                    String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

                    if (shortcutUtils.isShortcutToApp(uri)) {
                        return;
                    }

                    if (!shortcutUtils.isShortcutAlreadyAvailable(name)) {
                        addShortcut(uri, name);
                    }
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(broadcastReceiverShortcutInstall, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(broadcastReceiverShortcutInstall, filter);
            }
        }

    }

    // unregister the receivers on destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dialogs != null) {
            dialogs.dismiss();
            dialogs = null;
        }

        closeGlobalSettingsDialog();

        if (mSearchTask!=null) {
            mSearchTask.cancel(true);
            mSearchTask=null;
        }

        if (imm!=null){
            if(imm.isActive()){
                imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
            }
            imm=null;
        }

        // Performance improvement: Clear recycled views to free memory
        if (mHomeLayout instanceof FlowLayout) {
            ((FlowLayout) mHomeLayout).clearRecycledViews();
        }

        unregisterReceiver(broadcastReceiverAppInstall);
        unregisterReceiver(broadcastReceiverShortcutInstall);
        broadcastReceiverAppInstall = null;
        broadcastReceiverShortcutInstall = null;
        shortcutUtils.close();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Performance improvement: Clear recycled views on low memory
        if (mHomeLayout instanceof FlowLayout) {
            ((FlowLayout) mHomeLayout).clearRecycledViews();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        // restore request
        switch (requestCode) {

            case RESTORE_REQUEST:

                Uri uri = data.getData();
                ContentResolver cr = getContentResolver();
                try {
                    boolean b = DbUtils.loadDbFromFile(cr.openInputStream(uri));
                    if (b) {
                        recreate();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            case FONTS_REQUEST:

                try {

               /* String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                cursor.close();*/
                    ///new
                    Uri uri1 = data.getData();
                    ContentResolver cr1 = getContentResolver();

                    File fontFile = new File(getFilesDir(), "font.ttf");
                    try (InputStream inputFontStream = cr1.openInputStream(uri1);
                         OutputStream out = new FileOutputStream(fontFile);) {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inputFontStream.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                    String path = fontFile.getPath();
                    //Log.i(TAG, "onActivityResult: " + path);
                    mTypeface = Typeface.createFromFile(path);
                    DbUtils.setFonts(path);
                    loadApps();
                } catch (Exception i) {
                    //i.printStackTrace();
                    // Fallback to system font instead of custom TTF
                    mTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
                }
                break;

            case BACKUP_REQUEST:

                Uri uri2 = data.getData();
                ObjectOutputStream output = null;
                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri2, "w");
                    FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    output = new ObjectOutputStream(fileOutputStream);
                    output.writeObject(DbUtils.getDBData());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (output != null) {
                            output.flush();
                            output.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                break;


            case COLOR_SNIFFER_REQUEST:

                colorSnifferCall(data.getBundleExtra("color_bundle"));

                break;
        }

    }

    //may be override of abstract class method to be called from color sniffer #3 types
    public void colorSnifferCall(Bundle bundle) {
        boolean defaultColorSet = false;// for change set
        final int DEFAULT_COLOR = bundle.getInt(DEFAULT_COLOR_FOR_APPS);//keys
        // not set by ColorSniffer
        if (DEFAULT_COLOR != DbUtils.NULL_TEXT_COLOR) { //NULL_TEXT_COLOR=-1
            defaultColorSet = true;// to save cpu cycle
        }

        // get each value as proposed by Color Sniffer App developer
        for (Apps apps : mAppsList) {
            TextView textView = apps.getTextView();
            String appPackage = apps.getActivityName();
            int color = bundle.getInt(appPackage);
            if (color != DbUtils.NULL_TEXT_COLOR) {
                textView.setTextColor(color);
                DbUtils.putAppColorExternalSource(appPackage, color);
                // DbUtils.putAppColor(appPackage, color);
            } else if (defaultColorSet) {
                //set default color
                DbUtils.putAppColor(appPackage, DEFAULT_COLOR);
                textView.setTextColor(DEFAULT_COLOR);
            }//else do nothing theme default color will apply
        }
    }

    //Clipboard manager
    public Map<String, Integer> clipboardData() {
        Map<String, Integer> result = null;
        // Log.d(TAG, "clipboardData: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    String tabSeparatedData = item.getText().toString();

                    //validate tab Separated Data and get its data
                    //unique id bae73ae068dacc6cb659d1fb231e7b11 i.e lunox-ColorSniffer MD5-128

                    String[] line = tabSeparatedData.split("\n");//get each line

                    Map<String, Integer> colorsAndId = new ArrayMap<>(); // map to put all values in key and values format
                    // iterate over every line
                    for (String entry : line) {
                        String[] activityIdAndColor = entry.split("\t");// split line into id and color
                        int color = Color.parseColor(activityIdAndColor[1]);
                        colorsAndId.put(activityIdAndColor[0], color);// put id and color to map

                    }
                    setAppsColorFromClipboard(colorsAndId);
                    result = colorsAndId;// return map
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // return empty null/
        return result;
    }

    private void setAppsColorFromClipboard(Map<String, Integer> colorsAndId) {
        if (colorsAndId == null) return;
        DbUtils.externalSourceColor(true);
        for (Apps apps : mAppsList) {
            try {
                TextView textView = apps.getTextView();
                String s = apps.getActivityName();
                Integer newColor = colorsAndId.get(s);
                if (newColor == null) continue;
                textView.setTextColor(newColor);
                DbUtils.putAppColorExternalSource(s, newColor);
            } catch (NullPointerException ignore) {

            }
        }
    }

    // show the hidden app dialog
    public void showHiddenApps() {
        // Check for hidden apps
        boolean hasHidden = false;
        for (Apps app : mAppsList) {
            if (app.isHidden()) {
                hasHidden = true;
                break;
            }
        }
        if (hasHidden) {
            showDialog(new HiddenAppsDialogs(this, mAppsList), false);
            if (bottomLabel != null) bottomLabel.setVisibility(View.GONE);
        } else {
            if (bottomLabel != null) {
                bottomLabel.setText("No apps are hidden");
                bottomLabel.setVisibility(View.VISIBLE);
                bottomLabel.postDelayed(() -> bottomLabel.setVisibility(View.GONE), 3000);
            }
        }
    }

    // show the frozen app dialog
    public void showFrozenApps() {
        // Check for frozen apps
        boolean hasFrozen = false;
        for (Apps app : mAppsList) {
            if (app.isSizeFrozen()) {
                hasFrozen = true;
                break;
            }
        }
        if (hasFrozen) {
            showDialog(new FrozenAppsDialogs(this, mAppsList), false);
            if (bottomLabel != null) bottomLabel.setVisibility(View.GONE);
        } else {
            if (bottomLabel != null) {
                bottomLabel.setText("No apps are frozen");
                bottomLabel.setVisibility(View.VISIBLE);
                bottomLabel.postDelayed(() -> bottomLabel.setVisibility(View.GONE), 3000);
            }
        }
    }

    //set the flow layout alignment it is called from global settings
    public void setFlowLayoutAlignment(int gravity) {
        mHomeLayout.setGravity(gravity);
        DbUtils.setFlowLayoutAlignment(gravity);
    }

    public void setPadding() {
        showDialog(new PaddingDialog(this, mHomeLayout), false);
    }

    public void setColorsAndSize() {
        showDialog(new GlobalColorSizeDialog(this, mAppsList), true);
    }

    private void addShortcut(String uri, String appName) {
        if (mAppsList == null) return;
        mAppsList.add(new Apps(true, uri, appName, getCustomView(), DbUtils.NULL_TEXT_COLOR, DEFAULT_TEXT_SIZE_NORMAL_APPS, false, false, 0, (int) System.currentTimeMillis() / 1000));
        shortcutUtils.addShortcut(new Shortcut(appName, uri));
        // Log.d(TAG, "addShortcut: shortcut name==" + appName);
        sortApps(DbUtils.getSortsTypes());
    }

    /**
     * remove the shortcut
     *
     * @param view shortcut view to be removed...
     */
    public void removeShortcut(AppTextView view) {
        // view.setVisibility(View.GONE);
        shortcutUtils.removeShortcut(new Shortcut(view.getText().toString(), view.getUri()));

        // if (b)
        loadApps();
    }

     @Override
    public void onSwipe(Gestures.Direction direction) {
        if (direction == Gestures.Direction.SWIPE_RIGHT) {
            if (DbUtils.isSearchBarGestureEnabled() && !searching) {
            searching = true;
            mSearchBox.setText("");
            mSearchBox.setVisibility(View.VISIBLE);
            mSearchBox.requestFocus();
                mSearchBox.animate().alpha(1.0f).setDuration(150).start();
            imm.showSoftInput(mSearchBox, InputMethodManager.SHOW_IMPLICIT);
                // Optionally, dim the background or add a visual cue
            }
        } else if (direction == Gestures.Direction.SWIPE_LEFT) {
            if (searching) {
                mSearchBox.animate().alpha(0.0f).setDuration(150).withEndAction(() -> {
                mSearchBox.setVisibility(View.GONE);
                    mSearchBox.setAlpha(1.0f);
                }).start();
                imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
                // Reset app list to default state and padding
                mHomeLayout.setPadding(DbUtils.getPaddingLeft(), DbUtils.getPaddingTop(), DbUtils.getPaddingRight(), DbUtils.getPaddingBottom());
                sortApps(DbUtils.getSortsTypes());
                searching = false;
            }
        }
    }

    @Override
    public void onDoubleTap(MotionEvent e) {
        // Check if double tap to lock is enabled
        if (!DbUtils.isDoubleTapToLockEnabled()) {
            return;
        }

        // Check if the double tap is on an empty area (not on an app)
        View touchedView = findViewAt(e.getX(), e.getY());
        if (!(touchedView instanceof AppTextView)) {
            // Attempt to lock screen using accessibility service
            boolean success = AccessibilityUtils.requestLockScreen(this);
            if (!success) {
                // Show message if accessibility service is not enabled
                if (!AccessibilityUtils.isAccessibilityServiceEnabled(this)) {
                    showAccessibilityServiceDialog();
                }
            }
        }
    }

    private void checkDefaultLauncher() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentLauncherPackage = resolveInfo.activityInfo.packageName;
        if (getPackageName().equals(currentLauncherPackage)) {
            setDefaultLayout.setVisibility(View.GONE);
        } else {
            setDefaultLayout.setVisibility(View.VISIBLE);
        }
    }

    class SearchTask extends AsyncTask<CharSequence, Void, ArrayList<Apps>> {
        @Override
        protected void onPostExecute(ArrayList<Apps> filteredApps) {
            super.onPostExecute(filteredApps);
            showSearchResult(filteredApps);
        }

        @Override
        protected ArrayList<Apps> doInBackground(CharSequence... charSequences) {
            ArrayList<Apps> filteredApps = new ArrayList<>();
            for (Apps app : mAppsList) {
                if (charSequences[0].length() == 0) {
                    filteredApps.add(app);
                } else if (Utils.simpleFuzzySearch(charSequences[0], app.getAppName())) {
                    filteredApps.add(app);
                }
            }
            return filteredApps;
        }
    }

    class SortTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Performance improvement: Use batch operations for better performance
            mHomeLayout.removeAllViews();
            mHomeLayout.removeAllViewsInLayout();

            // Prepare views for batch addition
            List<View> viewsToAdd = new ArrayList<>();

            for (Apps app : mAppsList) {
                AppTextView textView = app.getTextView();
                if (textView.getParent() != null) {
                    ((ViewGroup) textView.getParent()).removeView(textView);
                }
                viewsToAdd.add(textView);
            }

            // Use batch add for better performance
            if (mHomeLayout instanceof FlowLayout) {
                ((FlowLayout) mHomeLayout).addViewsBatch(viewsToAdd);
            } else {
                // Fallback to individual additions
                for (View view : viewsToAdd) {
                    mHomeLayout.addView(view, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                }
            }
        }

        @Override
        protected Void doInBackground(final Integer... integers) {
            final int type = integers[0];
            DbUtils.setAppsSortsType(type);

            //sort the apps alphabetically
            Collections.sort(mAppsList, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                    a.getAppName(),
                    b.getAppName()
            ));

            switch (type) {
                case SORT_BY_SIZE://descending
                    Collections.sort(mAppsList, (apps, t1) -> (t1.getSize() - apps.getSize()));
                    break;
                case SORT_BY_OPENING_COUNTS://descending
                    Collections.sort(mAppsList, (apps, t1) -> t1.getOpeningCounts() - apps.getOpeningCounts());
                    break;
                case SORT_BY_COLOR:
                    Collections.sort(mAppsList, (apps, t1) -> {
                        float[] hsv = new float[3];
                        Color.colorToHSV(apps.getColor(), hsv);
                        float[] another = new float[3];
                        Color.colorToHSV(t1.getColor(), another);
                        for (int i = 0; i < 3; i++) {
                            if (hsv[i] != another[i]) {
                                return (hsv[i] < another[i]) ? -1 : 1;
                            }
                        }
                        return 0;
                    });
                    break;
                case SORT_BY_UPDATE_TIME://descending
                    Collections.sort(mAppsList, (apps, t1) -> t1.getUpdateTime() - apps.getUpdateTime());
                    break;
                case SORT_BY_RECENT_OPEN://descending
                    Collections.sort(mAppsList, (apps, t1) -> (t1.getRecentUsedWeight() - apps.getRecentUsedWeight()));
                    break;
            }
            return null;
        }
    }



    /**
     * Show dialog to enable accessibility service for double tap to lock
     */
    private void showAccessibilityServiceDialog() {
        Context ctx;
        if (DbUtils.getTheme() == R.style.Wallpaper)
            ctx = new ContextThemeWrapper(this, R.style.AppTheme);
        else
            ctx = new ContextThemeWrapper(this, DbUtils.getTheme());

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Enable Accessibility Service");
        builder.setMessage("To use double tap to lock screen, please enable the Lunox Accessibility Service in Settings.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            AccessibilityUtils.openAccessibilitySettings(this);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Show detailed double tap to lock dialog
     */
    private void showOnboardingTutorialIfNeeded() {
        if (DbUtils.isFirstTimeUser() && !DbUtils.isTutorialCompleted()) {
            // Use a post to ensure the UI is fully loaded before showing the dialog
            mHomeLayout.post(() -> {
                OnboardingTutorialDialog tutorialDialog = new OnboardingTutorialDialog(this, this);
                tutorialDialog.show();
            });
        }
    }

    public void showOnboardingTutorial() {
        OnboardingTutorialDialog tutorialDialog = new OnboardingTutorialDialog(this, this);
        tutorialDialog.show();
    }

    private void showDoubleTapLockDialog() {
        showDialog(new DoubleTapLockDialog(this, this), true);
    }

    private void showDialog(Dialog dialog, boolean setWindow) {
        // Close global settings dialog if it's open when showing other dialogs
        closeGlobalSettingsDialog();

        if (setWindow) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
            }
        }
        dialog.show();
        this.dialogs = dialog;
    }

    /**
     * Close the global settings dialog if it's currently open
     */
    public void closeGlobalSettingsDialog() {
        if (globalSettingsDialog != null && globalSettingsDialog.isShowing()) {
            globalSettingsDialog.dismiss();
            globalSettingsDialog = null;
        }
    }

}