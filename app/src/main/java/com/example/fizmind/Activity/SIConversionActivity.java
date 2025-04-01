package com.example.fizmind.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.R;
import com.example.fizmind.keyboard.KeyboardFragment;

/**
 * Активность конвертации в СИ.
 */
public class SIConversionActivity extends AppCompatActivity {

    private static final String TAG = "SIConversionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_si_conversion);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {

            finish();
        });

        // Создаем фрагмент клавиатуры с режимом конвертации и разрешенным вводом неизвестного
        KeyboardFragment fragment = KeyboardFragment.newInstance(true, true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyboard_container, fragment)
                .commit();
    }
}
