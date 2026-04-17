package com.example.newsnow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newsnow.utils.LocaleHelper;
import com.example.newsnow.utils.ThemeHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup themeRadioGroup;
    private SwitchMaterial switchAutoRefresh;
    private TextView tvCurrentLanguage;
    private static final String PREFS_NAME = "newsnow_prefs";
    private static final String KEY_AUTO_REFRESH = "auto_refresh";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        themeRadioGroup = findViewById(R.id.theme_radio_group);
        switchAutoRefresh = findViewById(R.id.switch_auto_refresh);
        tvCurrentLanguage = findViewById(R.id.tv_language_name);
 
        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        int savedTheme = ThemeHelper.getSavedTheme(this);
        switch (savedTheme) {
            case ThemeHelper.THEME_LIGHT:
                ((RadioButton) findViewById(R.id.radio_light)).setChecked(true);
                break;
            case ThemeHelper.THEME_SYSTEM:
                ((RadioButton) findViewById(R.id.radio_system)).setChecked(true);
                break;
            case ThemeHelper.THEME_DARK:
            default:
                ((RadioButton) findViewById(R.id.radio_dark)).setChecked(true);
                break;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        switchAutoRefresh.setChecked(prefs.getBoolean(KEY_AUTO_REFRESH, true));

        // Update language name display
        String currentLang = LocaleHelper.getLanguage(this);
        String langName = "English";
        switch (currentLang) {
            case "hi": langName = "हिंदी (Hindi)"; break;
            case "gu": langName = "ગુજરાતી (Gujarati)"; break;
            case "mr": langName = "मराठी (Marathi)"; break;
            case "kn": langName = "ಕನ್ನಡ (Kannada)"; break;
        }
        tvCurrentLanguage.setText(langName);
    }

    private void setupListeners() {
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int theme;
            if (checkedId == R.id.radio_light) {
                theme = ThemeHelper.THEME_LIGHT;
            } else if (checkedId == R.id.radio_system) {
                theme = ThemeHelper.THEME_SYSTEM;
            } else {
                theme = ThemeHelper.THEME_DARK;
            }
            ThemeHelper.saveTheme(this, theme);
            ThemeHelper.applyTheme(theme);
            recreate(); // Apply theme instantly
        });

        switchAutoRefresh.setOnCheckedChangeListener((btn, isChecked) -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit().putBoolean(KEY_AUTO_REFRESH, isChecked).apply();
        });

        findViewById(R.id.btn_change_language).setOnClickListener(v -> showLanguageDialog());

        findViewById(R.id.btn_help_center).setOnClickListener(v -> showHelpSheet());
        findViewById(R.id.btn_contact_us).setOnClickListener(v -> showContactSheet());
        findViewById(R.id.btn_feedback).setOnClickListener(v -> showFeedbackSheet());
    }

    private void showHelpSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(R.layout.bottom_sheet_help);
        dialog.show();
    }

    private void showContactSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(R.layout.bottom_sheet_contact);
        dialog.show();
    }

    private void showFeedbackSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_feedback, null);
        
        android.widget.EditText etSubject = view.findViewById(R.id.et_feedback_subject);
        android.widget.EditText etMessage = view.findViewById(R.id.et_feedback_message);
        View btnSubmit = view.findViewById(R.id.btn_submit_feedback);

        btnSubmit.setOnClickListener(v -> {
            String sub = etSubject.getText().toString().trim();
            String msg = etMessage.getText().toString().trim();

            if (sub.isEmpty() || msg.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            } else {
                // Simulation of hidden submission
                Toast.makeText(this, R.string.feedback_success, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "हिंदी (Hindi)", "ગુજરાતી (Gujarati)", "मराठी (Marathi)", "ಕನ್ನಡ (Kannada)"};
        String[] languageCodes = {"en", "hi", "gu", "mr", "kn"};

        int currentSelection = 0;
        String currentLang = LocaleHelper.getLanguage(this);
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                currentSelection = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_language)
                .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                    updateLanguage(languageCodes[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateLanguage(String code) {
        LocaleHelper.setLocale(this, code);
        
        // Restart the whole application to apply changes correctly
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
