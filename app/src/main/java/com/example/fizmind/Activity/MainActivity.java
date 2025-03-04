package com.example.fizmind.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.widget.ImageView;
import androidx.cardview.widget.CardView;
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
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageView menuIcon = findViewById(R.id.menu_icon);


        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_language) {

            } else if (id == R.id.nav_settings) {

            } else if (id == R.id.nav_placeholder) {

            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // кнопки
        configureButton(R.id.btn_si_conversion, SIConversionActivity.class);
        configureButton(R.id.btn_physics_calculator, PhysicsCalculatorActivity.class);
        configureButton(R.id.btn_theory, TheoryActivity.class);
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
