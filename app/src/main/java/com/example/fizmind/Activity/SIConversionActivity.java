package com.example.fizmind.Activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;
import com.example.fizmind.fragments.KeyboardFragment;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

public class SIConversionActivity extends AppCompatActivity {

    private static final String TAG = "SIConversionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_si_conversion);
        LogUtils.logActivityStarted(TAG, "Активность перевода в СИ");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // обновляем значение g при запуске
        PhysicalQuantityRegistry.updateGravityValue(this);
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            LogUtils.logButtonPressed(TAG, "Назад");
            finish();
        });

        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance(true, false);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyboard_container, keyboardFragment)
                .commit();
        LogUtils.logFragmentInitialized(TAG, "Фрагмент клавиатуры", "режим перевода = true, переключение на неизвестное = false");
    }
}