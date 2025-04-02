package com.example.fizmind.Activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;
import com.example.fizmind.keyboard.KeyboardFragment;

/**
 * Активность физического калькулятора.
 * Переключение на поле "Введите неизвестное" разрешено.
 */
public class PhysicsCalculatorActivity extends AppCompatActivity {

    // Тег для логирования
    private static final String TAG = "PhysicsCalculatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Устанавливаем портретную ориентацию
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_physics_calculator);
        Log.d(TAG, "Активность физического калькулятора запущена");

        // Настройка кнопки "Назад"
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Нажата кнопка 'Назад'");
            finish();
        });

        // Инициализация фрагмента клавиатуры
        // Первый аргумент: isConversionMode = false (режим калькулятора)
        // Второй аргумент: isUnknownInputAllowed = true (разрешено переключение на "Введите неизвестное")
        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance(false, true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyboard_container, keyboardFragment)
                .commit();
        Log.d(TAG, "Фрагмент клавиатуры инициализирован: режим перевода = false, переключение на неизвестное = true");
    }
}