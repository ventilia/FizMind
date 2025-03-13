package com.example.fizmind.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fizmind.R;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Настройка кнопки "назад"
        ImageView backArrow = findViewById(R.id.backButton);
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_drawer", true); // Флаг для открытия DrawerLayout
            startActivity(intent);
            finish();
        });

        // Настройка аккордеона
        TextView accordionHeader = findViewById(R.id.accordion_header);
        LinearLayout accordionContent = findViewById(R.id.accordion_content);
        accordionHeader.setOnClickListener(v -> {
            if (accordionContent.getVisibility() == View.GONE) {
                accordionContent.setVisibility(View.VISIBLE);
            } else {
                accordionContent.setVisibility(View.GONE);
            }
        });
    }
}