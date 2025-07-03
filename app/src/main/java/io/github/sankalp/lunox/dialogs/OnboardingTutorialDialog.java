/*
 * Lunox
 * Copyright (C) 2025 Sankalp Tharu
 *
 * This file is part of Last Launcher (forked).
 * Licensed under the GNU General Public License v3 or later.
 * See <http://www.gnu.org/licenses/>.
 *
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
import io.github.sankalp.lunox.utils.DbUtils;

/**
 * Dialog for showing onboarding tutorial to new users
 */
public class OnboardingTutorialDialog extends Dialog implements View.OnClickListener {

    private final LauncherActivity launcherActivity;
    private final Context context;

    private TextView titleView;
    private TextView stepIndicatorView;
    private TextView contentView;
    private TextView skipButton;
    private TextView previousButton;
    private TextView nextButton;

    private int currentStep = 1;
    private static final int TOTAL_STEPS = 7;

    // Tutorial step data - using static arrays to reduce memory footprint
    private static final int[] STEP_TITLES = {
        R.string.tutorial_step_1_title, R.string.tutorial_step_2_title, R.string.tutorial_step_3_title,
        R.string.tutorial_step_4_title, R.string.tutorial_step_5_title, R.string.tutorial_step_6_title,
        R.string.tutorial_step_7_title
    };

    private static final int[] STEP_CONTENTS = {
        R.string.tutorial_step_1_content, R.string.tutorial_step_2_content, R.string.tutorial_step_3_content,
        R.string.tutorial_step_4_content, R.string.tutorial_step_5_content, R.string.tutorial_step_6_content,
        R.string.tutorial_step_7_content
    };

    public OnboardingTutorialDialog(Context context, LauncherActivity launcherActivity) {
        super(context);
        this.context = context;
        this.launcherActivity = launcherActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_onboarding_tutorial);

        // Make background transparent for rounded corners
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize views
        titleView = findViewById(R.id.tv_tutorial_title);
        stepIndicatorView = findViewById(R.id.tv_step_indicator);
        contentView = findViewById(R.id.tv_tutorial_content);
        skipButton = findViewById(R.id.btn_skip);
        previousButton = findViewById(R.id.btn_previous);
        nextButton = findViewById(R.id.btn_next);

        // Set click listeners
        skipButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        // Update content for first step
        updateStepContent();

        // Prevent dismissing by tapping outside
        setCancelable(false);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_skip) {
            finishTutorial();
        } else if (id == R.id.btn_previous) {
            if (currentStep > 1) {
                currentStep--;
                updateStepContent();
            }
        } else if (id == R.id.btn_next) {
            if (currentStep < TOTAL_STEPS) {
                currentStep++;
                updateStepContent();
            } else {
                finishTutorial();
            }
        }
    }

    private void updateStepContent() {
        // Update title
        titleView.setText(STEP_TITLES[currentStep - 1]);

        // Update step indicator
        stepIndicatorView.setText(currentStep + "/" + TOTAL_STEPS);

        // Update content
        contentView.setText(STEP_CONTENTS[currentStep - 1]);

        // Update button visibility and text
        if (currentStep == 1) {
            previousButton.setVisibility(View.GONE);
        } else {
            previousButton.setVisibility(View.VISIBLE);
        }

        if (currentStep == TOTAL_STEPS) {
            nextButton.setText(R.string.onboarding_finish);
            skipButton.setVisibility(View.GONE);
        } else {
            nextButton.setText(R.string.onboarding_next);
            skipButton.setVisibility(View.VISIBLE);
        }
    }

    private void finishTutorial() {
        // Mark tutorial as completed
        DbUtils.setTutorialCompleted(true);
        DbUtils.setFirstTimeUser(false);

        // Dismiss dialog
        dismiss();
    }
}
