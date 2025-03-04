package com.example.fizmind.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;
import com.example.fizmind.R;
import com.example.fizmind.animation.ButtonAnimation;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Устанавливаем портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        // Инициализируем DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Находим иконку меню и устанавливаем обработчик для открытия бокового меню
        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Находим кастомные элементы меню в боковом меню
        LinearLayout languageLayout = findViewById(R.id.language_layout);
        LinearLayout settingsLayout = findViewById(R.id.settings_layout);
        LinearLayout aboutUsLayout = findViewById(R.id.about_us_layout);

        // Устанавливаем обработчики кликов для элементов меню
        languageLayout.setOnClickListener(v -> {
            // TODO: Реализовать выбор языка (например, открыть активити или диалог)
            Toast.makeText(this, "Выбран язык", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START); // Закрываем меню после клика
        });

        settingsLayout.setOnClickListener(v -> {
            // TODO: Реализовать переход в настройки
            Toast.makeText(this, "Выбраны настройки", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START); // Закрываем меню после клика
        });

        aboutUsLayout.setOnClickListener(v -> {
            // TODO: Реализовать переход в "О нас"
            Toast.makeText(this, "Выбрано О нас", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START); // Закрываем меню после клика
        });

        // Настраиваем кнопки главного экрана
        configureButton(R.id.btn_si_conversion, SIConversionActivity.class);
        configureButton(R.id.btn_physics_calculator, PhysicsCalculatorActivity.class);
        configureButton(R.id.btn_theory, TheoryActivity.class);
    }

    /**
     * Настраивает кнопку с анимацией и обработчиком клика для открытия целевой активности.
     *
     * @param buttonId       ID ресурса кнопки (CardView)
     * @param targetActivity Класс активности, которая откроется при клике
     */
    private void configureButton(int buttonId, Class<?> targetActivity) {
        CardView button = findViewById(buttonId);
        if (button != null) {
            ButtonAnimation.addAnimation(button); // Добавляем анимацию кнопке
            button.setOnClickListener(v -> openActivity(targetActivity)); // Устанавливаем обработчик клика
        }
    }

    /**
     * Открывает указанную активность.
     *
     * @param targetActivity Класс активности для открытия
     */
    private void openActivity(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
    }

    @Override
    public void onBackPressed() {
        // Если боковое меню открыто, закрываем его, иначе выполняем стандартное действие
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}