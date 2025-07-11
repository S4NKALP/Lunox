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

package io.github.sankalp.lunox.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.util.ArrayList;

import io.github.sankalp.lunox.R;
import io.github.sankalp.lunox.adapters.UniversalAdapter;
import io.github.sankalp.lunox.model.Apps;
import io.github.sankalp.lunox.utils.DbUtils;

public class FrozenAppsDialogs extends Dialog {

    private final ArrayList<Apps> mAppsList;
    private final Context context;
    ArrayList<Apps> frozenApps = new ArrayList<>();
    private ListView listView;

    public FrozenAppsDialogs(Context context, ArrayList<Apps> appsList) {
        super(context);
        this.context = context;
        mAppsList = appsList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_hidden_apps);
        listView = findViewById(R.id.hidden_app_list);

        UniversalAdapter adapter = new UniversalAdapter(context, frozenApps);
        listView.setAdapter(adapter);

        adapter.setOnClickListener(this::confirmationAndRemove);

        // Show or hide the empty label based on list content
        android.widget.TextView emptyLabel = findViewById(R.id.empty_label);
        if (frozenApps.isEmpty()) {
            emptyLabel.setVisibility(android.view.View.VISIBLE);
            emptyLabel.setText("No apps are frozen");
        } else {
            emptyLabel.setVisibility(android.view.View.GONE);
        }

    }

    private void confirmationAndRemove(Apps apps, View view) {
        Context ctx;
        if (DbUtils.getTheme() == R.style.Wallpaper)
            ctx = new ContextThemeWrapper(context, R.style.AppTheme);
        else
            ctx = new ContextThemeWrapper(context, DbUtils.getTheme());

        PopupMenu popupMenu = new PopupMenu(ctx, view);
        popupMenu.getMenuInflater().inflate(R.menu.remove_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {

            if (menuItem.getItemId() == R.id.menu_remove_this) {
                apps.setFreeze(false);
                updateFrozenList();
            }
            return true;

        });
        popupMenu.show();

    }

    public int updateFrozenList() {
        // only show frozen app
        for (Apps apps : mAppsList) {
            if (apps.isSizeFrozen()) {
                frozenApps.add(apps);
            }
        }
        return frozenApps.size();
    }

}
