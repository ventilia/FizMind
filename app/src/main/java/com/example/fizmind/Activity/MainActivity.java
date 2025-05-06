package com.example.fizmind.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;
import com.example.fizmind.R;
import com.example.fizmind.animation.ButtonAnimation;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private float startX;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);


        drawerLayout.setScrimColor(Color.parseColor("#80000000"));


        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        // увеличить область свайпа
        try {
            Field dragHelperField = DrawerLayout.class.getDeclaredField("mLeftDragger");
            dragHelperField.setAccessible(true);
            Object dragHelper = dragHelperField.get(drawerLayout);
            Field edgeSizeField = dragHelper.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(dragHelper);
            edgeSizeField.setInt(dragHelper, edgeSize * 10); // Увеличиваем в 10 раз
            Log.d("Drawer", "Swipe area increased to: " + (edgeSize * 10));
        } catch (Exception e) {
            Log.e("Drawer", "Failed to increase swipe area", e);
        }


        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                Log.d("Drawer", "Drawer sliding: " + slideOffset);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                Log.d("Drawer", "Drawer opened");
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                Log.d("Drawer", "Drawer closed");
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                Log.d("Drawer", "Drawer state changed: " + newState);
            }
        });


        drawerLayout.setOnTouchListener((v, event) -> {
            Log.d("Drawer", "Touch event: action=" + event.getAction() + ", x=" + event.getX() + ", y=" + event.getY());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getX() - startX;
                    if (deltaX > 50 && startX < 600 && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.openDrawer(GravityCompat.START);
                        Log.d("Drawer", "Custom swipe detected, opening drawer");
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startX = 0;
                    break;
            }
            return false; // передаём событие
        });


        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> {
            Log.d("Drawer", "Menu icon clicked");
            drawerLayout.openDrawer(GravityCompat.START);
        });


        configureButton(R.id.btn_si_conversion, SIConversionActivity.class);
        configureButton(R.id.btn_physics_calculator, PhysicsCalculatorActivity.class);
        configureButton(R.id.btn_theory, TheoryActivity.class);


        LinearLayout settingsLayout = findViewById(R.id.settings_layout);
        LinearLayout aboutUsLayout = findViewById(R.id.about_us_layout);
        settingsLayout.setOnClickListener(v -> openActivity(SettingsActivity.class));
        aboutUsLayout.setOnClickListener(v -> openActivity(AboutUsActivity.class));
    }

    private void configureButton(int buttonId, Class<?> targetActivity) {
        CardView button = findViewById(buttonId);
        if (button != null) {
            ButtonAnimation.addAnimation(button);
            button.setOnClickListener(v -> openActivity(targetActivity));
        }
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