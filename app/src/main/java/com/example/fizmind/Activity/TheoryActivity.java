package com.example.fizmind.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;
import com.example.fizmind.animation.ButtonAnimation;


public class TheoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theory);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> onBackPressed());


        View btnFormulas = findViewById(R.id.btn_si_conversion);
        ButtonAnimation.addAnimation(btnFormulas); // добавление анимации
        btnFormulas.setOnClickListener(v -> {
            Intent intent = new Intent(TheoryActivity.this, DetailActivity.class);
            intent.putExtra("type", "formulas");
            startActivity(intent);
        });


        View btnDesignations = findViewById(R.id.btn_physics_calculator);
        ButtonAnimation.addAnimation(btnDesignations); // добавление анимации
        btnDesignations.setOnClickListener(v -> {
            Intent intent = new Intent(TheoryActivity.this, DetailActivity.class);
            intent.putExtra("type", "designations");
            startActivity(intent);
        });


        View btnGuide = findViewById(R.id.btn_theory);
        ButtonAnimation.addAnimation(btnGuide); // добавление анимации
        btnGuide.setOnClickListener(v -> {
            Intent intent = new Intent(TheoryActivity.this, DetailActivity.class);
            intent.putExtra("type", "guide");
            startActivity(intent);
        });
    }
}