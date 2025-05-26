package com.example.fizmind.Activity;

import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.fizmind.R;
import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.keyboard.DisplayManager;
import com.example.fizmind.solver.SolutionFormatter;
import com.example.fizmind.solver.Solver;
import com.example.fizmind.utils.LogUtils;

// активность для отображения решения задачи
public class SolutionActivity extends AppCompatActivity {
    private TextView solutionTextView;
    private AppDatabase database;
    private DisplayManager displayManager;
    private Typeface stixTypeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);
        solutionTextView = findViewById(R.id.solution_text);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // инициализация базы данных
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "fizmind-db")
                .allowMainThreadQueries()
                .build();

        // шрифты
        Typeface montserratAlternatesTypeface = Typeface.createFromAsset(
                getAssets(), "fonts/MontserratAlternates-Regular.ttf");
        stixTypeface = Typeface.createFromAsset(getAssets(), "fonts/stix_two_text_italic.ttf");

        // инициализация менеджера отображения
        displayManager = new DisplayManager(stixTypeface, database);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // отображение решения
        displaySolution(montserratAlternatesTypeface);
        LogUtils.d("SolutionActivity", "активность создана");
    }

    // отображает решение задачи
    private void displaySolution(Typeface montserratTypeface) {
        FormulaDatabase formulaDatabase = new FormulaDatabase();
        Solver solver = new Solver(formulaDatabase, database);
        SolutionFormatter formatter = new SolutionFormatter(montserratTypeface, stixTypeface, displayManager, database);

        try {
            Solver.SolutionResult result = solver.solve();
            SpannableStringBuilder solution = formatter.formatSolution(result);
            solutionTextView.setText(solution);
            LogUtils.d("SolutionActivity", "решение успешно отображено");
        } catch (Exception e) {
            solutionTextView.setText("ошибка вычисления: " + e.getMessage());
            LogUtils.e("SolutionActivity", "ошибка вычисления: " + e.getMessage(), e);
        }
    }
}