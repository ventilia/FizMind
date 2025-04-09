package com.example.fizmind.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;
import com.example.fizmind.utils.LogUtils;

/**
 * активность настроек приложения
 */
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        LogUtils.updateSettings(this);
        LogUtils.logActivityStarted(TAG, "активность настроек");


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();


        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            LogUtils.logButtonPressed(TAG, "назад");
            finish();
        });
    }
}