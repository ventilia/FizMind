package com.example.fizmind.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;
import com.example.fizmind.keyboard.KeyboardFragment;

/**
 * Активность для перевода физических величин в систему СИ.
 * Переключение на поле "Введите неизвестное" запрещено.
 */
public class SIConversionActivity extends AppCompatActivity {

    // Тег для логирования
    private static final String TAG = "SIConversionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_si_conversion);
        Log.d(TAG, "Активность перевода в СИ запущена");

        // Настройка кнопки "Назад"
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Нажата кнопка 'Назад'");
            finish();
        });

        // Инициализация фрагмента клавиатуры
        // Первый аргумент: isConversionMode = true (режим перевода в СИ)
        // Второй аргумент: isUnknownInputAllowed = false (запрет переключения на "Введите неизвестное")
        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance(true, false);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyboard_container, keyboardFragment)
                .commit();
        Log.d(TAG, "Фрагмент клавиатуры инициализирован: режим перевода = true, переключение на неизвестное = false");
    }
}