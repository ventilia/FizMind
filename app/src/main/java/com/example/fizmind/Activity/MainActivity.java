package com.example.fizmind.Activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;
import com.example.fizmind.R;
import com.example.fizmind.animation.ButtonAnimation;
import com.example.fizmind.utils.LogUtils;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
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

        // увеличение области свайпа
        try {
            Field dragHelperField = DrawerLayout.class.getDeclaredField("mLeftDragger");
            dragHelperField.setAccessible(true);
            Object dragHelper = dragHelperField.get(drawerLayout);
            Field edgeSizeField = dragHelper.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(dragHelper);
            edgeSizeField.setInt(dragHelper, edgeSize * 10);
            LogUtils.d(TAG, "Swipe area increased to: " + (edgeSize * 10));
        } catch (Exception e) {
            LogUtils.e(TAG, "Failed to increase swipe area", e);
        }

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
            @Override public void onDrawerOpened(@NonNull View drawerView) {}
            @Override public void onDrawerClosed(@NonNull View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {}
        });

        drawerLayout.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getX() - startX;
                    if (deltaX > 50 && startX < 600 && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.openDrawer(GravityCompat.START);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startX = 0;
                    break;
            }
            return false;
        });

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        configureButton(R.id.btn_si_conversion, SIConversionActivity.class);
        configureButton(R.id.btn_physics_calculator, PhysicsCalculatorActivity.class);
        configureButton(R.id.btn_theory, TheoryActivity.class);

        LinearLayout settingsLayout = findViewById(R.id.settings_layout);
        LinearLayout aboutUsLayout = findViewById(R.id.about_us_layout);
        settingsLayout.setOnClickListener(v -> openActivity(SettingsActivity.class));
        aboutUsLayout.setOnClickListener(v -> openActivity(AboutUsActivity.class));

        TextView emailText = findViewById(R.id.emailTextView);
        emailText.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("email", emailText.getText().toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Snackbar.make(drawerLayout, "Скопировано в буфер обмена", Snackbar.LENGTH_SHORT).show();
                LogUtils.logButtonPressed(TAG, "копирование e-mail");
            }
        });
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
