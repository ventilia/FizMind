package com.example.fizmind.Activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;
import com.example.fizmind.keyboard.KeyboardFragment;
import com.example.fizmind.utils.LogUtils;

/**
 * активность физического калькулятора.
 * переключение на поле "Введите неизвестное" разрешено.
 */
public class PhysicsCalculatorActivity extends AppCompatActivity {

    private static final String TAG = "PhysicsCalculatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_physics_calculator);
        LogUtils.logActivityStarted(TAG, "Активность физического калькулятора");

        // настройка кнопки "Назад"
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            LogUtils.logButtonPressed(TAG, "Назад");
            finish();
        });

        // инициализация фрагмента клавиатуры
        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance(false, true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyboard_container, keyboardFragment)
                .commit();
        LogUtils.logFragmentInitialized(TAG, "Фрагмент клавиатуры", "режим перевода = false, переключение на неизвестное = true");
    }
}