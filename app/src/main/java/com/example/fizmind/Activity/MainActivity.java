package com.example.fizmind.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.example.fizmind.R;
import com.example.fizmind.animation.ButtonAnimation;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        // я забыл что это
        drawerLayout.setScrimColor(Color.parseColor("#80000000"));

        // меню
        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // кнопки
        configureButton(R.id.btn_si_conversion, SIConversionActivity.class);
        configureButton(R.id.btn_physics_calculator, PhysicsCalculatorActivity.class);
        configureButton(R.id.btn_theory, TheoryActivity.class);

        // боковое меню
        LinearLayout settingsLayout = findViewById(R.id.settings_layout);
        LinearLayout aboutUsLayout = findViewById(R.id.about_us_layout);
        settingsLayout.setOnClickListener(v -> openActivity(SettingsActivity.class));
        aboutUsLayout.setOnClickListener(v -> openActivity(AboutUsActivity.class));
    }

    private void configureButton(int buttonId, Class<?> targetActivity) {
        CardView button = findViewById(buttonId);
        ButtonAnimation.addAnimation(button);
        button.setOnClickListener(v -> openActivity(targetActivity));
    }

    private void openActivity(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
