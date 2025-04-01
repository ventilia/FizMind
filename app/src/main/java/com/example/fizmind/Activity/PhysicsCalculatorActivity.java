package com.example.fizmind.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.fizmind.R;
import com.example.fizmind.keyboard.KeyboardFragment;


public class PhysicsCalculatorActivity extends AppCompatActivity {

    private static final String TAG = "PhysicsCalculatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Фиксируем портретную ориентацию
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_physics_calculator);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {

            finish();
        });

        // Создаем фрагмент клавиатуры с режимом калькулятора и разрешенным вводом неизвестного
        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance(false, true);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.keyboard_container, keyboardFragment);
        transaction.commit();
    }
}
