package com.example.fizmind;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fizmind.animation.ButtonAnimation;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);
        configureButton(R.id.btn_si_conversion, SIConversionActivity.class);
        configureButton(R.id.btn_physics_calculator, PhysicsCalculatorActivity.class);
        configureButton(R.id.btn_theory, TheoryActivity.class);
    }

    private void configureButton(int buttonId, Class<?> targetActivity) {
        CardView button = findViewById(buttonId);

        ButtonAnimation.addAnimation(button); // Анимация
        button.setOnClickListener(v -> openActivity(targetActivity));
    }
    private void openActivity(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
    }


}
