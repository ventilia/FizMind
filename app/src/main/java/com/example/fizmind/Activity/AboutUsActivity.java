package com.example.fizmind.Activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.example.fizmind.R;
import com.example.fizmind.utils.LogUtils;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            LogUtils.logButtonPressed(TAG, "Назад");
            finish();
        });
    }
}