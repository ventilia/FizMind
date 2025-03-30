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
 * Активность физического калькулятора с использованием фрагмента клавиатуры.
 * Отображает полный вариант клавиатуры с блоком "Введите неизвестное".
 */
public class PhysicsCalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_physics_calculator);

        // Кнопка "Назад"
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PhysicsCalculatorActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Добавление фрагмента клавиатуры с блоком "Введите неизвестное"
        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance(true);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.keyboard_container, keyboardFragment);
        transaction.commit();
    }
}