package com.example.fizmind.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.fizmind.R;
import com.example.fizmind.keyboard.KeyboardFragment;

/**
 * Активность для конвертации единиц СИ с использованием фрагмента клавиатуры.
 * Отображает урезанный вариант клавиатуры без блока "Введите неизвестное".
 */
public class SIConversionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_si_conversion);

        // Кнопка "Назад"
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SIConversionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Добавление фрагмента клавиатуры без блока "Введите неизвестное"
        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance(false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.keyboard_container, keyboardFragment);
        transaction.commit();
    }
}