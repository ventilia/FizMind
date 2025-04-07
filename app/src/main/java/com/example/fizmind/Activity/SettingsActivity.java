package com.example.fizmind.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;

import com.example.fizmind.utils.LogUtils;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        LogUtils.logActivityStarted(TAG, "Активность настроек");

        // Добавляем фрагмент настроек
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new com.example.fizmind.SettingsFragment())
                .commit();

        // Настройка кнопки "Назад"
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            LogUtils.logButtonPressed(TAG, "Назад");
            finish();
        });
    }
}