package com.example.fizmind.Activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;
import com.example.fizmind.fragments.KeyboardFragment;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

public class PhysicsCalculatorActivity extends AppCompatActivity {

    private static final String TAG = "PhysicsCalculatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_physics_calculator);
        LogUtils.logActivityStarted(TAG, "Активность физического калькулятора");

        PhysicalQuantityRegistry.updateGravityValue(this);


        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            LogUtils.logButtonPressed(TAG, "Назад");
            finish();
        });


        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance(false, true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyboard_container, keyboardFragment)
                .commit();
        LogUtils.logFragmentInitialized(TAG, "Фрагмент клавиатуры", "режим перевода = false, переключение на неизвестное = true");
    }
}