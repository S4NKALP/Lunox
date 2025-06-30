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
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import io.github.sankalp.lunox.LauncherActivity;
import io.github.sankalp.lunox.R;
import io.github.sankalp.lunox.utils.DbUtils;

public class ThemeSelectorDialog extends Dialog implements View.OnClickListener {

    private final LauncherActivity context;

    ThemeSelectorDialog(Context context, LauncherActivity launcherActivity) {
        super(context);
        this.context = launcherActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_theme_selector);
        LinearLayout ll = findViewById(R.id.theme_linear_layout);
        for (int i = 0; i < ll.getChildCount(); i++) {
            ll.getChildAt(i).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.t1) {
            setTheme(R.style.AppTheme);
        } else if (id == R.id.t2) {
            setTheme(R.style.Wallpaper);
        } else if (id == R.id.t3) {
            setTheme(R.style.Black);
        } else if (id == R.id.t4) {
            setTheme(R.style.White);
        } else if (id == R.id.t5) {
            setTheme(R.style.WhiteOnGrey);
        } else if (id == R.id.t6) {
            setTheme(R.style.BlackOnGrey);
        } else if (id == R.id.t35) {
            setTheme(R.style.Hacker_green);
        } else if (id == R.id.t36) {
            setTheme(R.style.Hacker_red);
        }
    }

    private void setTheme(int appTheme) {
        DbUtils.setTheme(appTheme);
        DbUtils.externalSourceColor(false);
        cancel();
        context.recreate();
    }
}
