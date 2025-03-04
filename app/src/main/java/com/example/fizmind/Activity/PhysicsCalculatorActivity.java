package com.example.fizmind.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fizmind.keyboard.InputController;
import com.example.fizmind.keyboard.KeyboardLogic;
import com.example.fizmind.R;

import java.util.Arrays;
import java.util.List;

public class PhysicsCalculatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_physics_calculator);


        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PhysicsCalculatorActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Инициализация
        List<TextView> keyboardCells = Arrays.asList(
                findViewById(R.id.key_1),
                findViewById(R.id.key_2),
                findViewById(R.id.key_3),
                findViewById(R.id.key_4),
                findViewById(R.id.key_5),
                findViewById(R.id.key_6),
                findViewById(R.id.key_7),
                findViewById(R.id.key_8),
                findViewById(R.id.key_9),
                findViewById(R.id.key_10),
                findViewById(R.id.key_11),
                findViewById(R.id.key_12),
                findViewById(R.id.key_13),
                findViewById(R.id.key_14),
                findViewById(R.id.key_15),
                findViewById(R.id.key_16),
                findViewById(R.id.key_17),
                findViewById(R.id.key_18),
                findViewById(R.id.key_19),
                findViewById(R.id.key_20),
                findViewById(R.id.key_21)
        );

        TextView pageNumberView = findViewById(R.id.page_number);
        TextView designationButton = findViewById(R.id.Designation);
        TextView unitsButton = findViewById(R.id.Units_of_measurement);
        TextView numbersButton = findViewById(R.id.Numbers_and_operations);
        ImageButton prevPageButton = findViewById(R.id.button_prev_page);
        ImageButton nextPageButton = findViewById(R.id.button_next_page);

        // ЭКЗЕМПЛЯР
        KeyboardLogic keyboardLogic = new KeyboardLogic(
                this,
                keyboardCells,
                pageNumberView,
                designationButton,
                unitsButton,
                numbersButton,
                prevPageButton,
                nextPageButton
        );

        // шрифт
        keyboardLogic.setUseStixFont(true);

        TextView displayView = findViewById(R.id.editText_designations);
        InputController inputController = new InputController(displayView);
        displayView.setMovementMethod(new ScrollingMovementMethod());


        keyboardLogic.setInputController(inputController);


        inputController.setStixTypeface(keyboardLogic.getStixTypeface());


        ImageButton buttonSave = findViewById(R.id.button_save);
        ImageButton buttonLeft = findViewById(R.id.button_left);
        ImageButton buttonRight = findViewById(R.id.button_right);
        ImageButton buttonClear = findViewById(R.id.button_clear);

        buttonSave.setOnClickListener(v -> {
            Log.d("PhysicsCalculatorActivity", "Нажата кнопка SAVE");
            inputController.onDownArrowPressed();
        });

        buttonLeft.setOnClickListener(v -> {
            Log.d("PhysicsCalculatorActivity", "Нажата кнопка LEFT");
            inputController.onLeftArrowPressed();
        });

        buttonRight.setOnClickListener(v -> {
            Log.d("PhysicsCalculatorActivity", "Нажата кнопка RIGHT");
            inputController.onRightArrowPressed();
        });


        buttonClear.setOnClickListener(v -> {
            Log.d("PhysicsCalculatorActivity", "Нажата кнопка делет");
            inputController.onDeletePressed();
        });

        // длительнное нажатие на делет
        buttonClear.setOnLongClickListener(v -> {
            Log.d("PhysicsCalculatorActivity", "Длительное нажатие на кнопку делет");
            inputController.clearAll();
            return true;
        });
    }
}