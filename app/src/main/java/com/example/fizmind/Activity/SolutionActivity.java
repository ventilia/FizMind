package com.example.fizmind.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fizmind.R;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.keyboard.DisplayManager;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.solver.MeasurementValidator;
import com.example.fizmind.solver.MeasurementValidatorImpl;
import com.example.fizmind.solver.SolutionFormatter;
import com.example.fizmind.solver.Solver;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SolutionActivity extends AppCompatActivity {
    private TextView solutionTextView;
    private MeasurementValidator measurementValidator;
    private DisplayManager displayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);
        solutionTextView = findViewById(R.id.solution_text);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        measurementValidator = new MeasurementValidatorImpl();

        //  шрифты
        Typeface montserratAlternatesTypeface = Typeface.createFromAsset(
                getAssets(), "fonts/MontserratAlternates-Regular.ttf");
        Typeface stixTypeface = Typeface.createFromAsset(getAssets(), "fonts/stix_two_text_italic.ttf");


        displayManager = new DisplayManager(stixTypeface);


        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // получаем данные
        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<ConcreteMeasurement> measurements =
                    intent.getParcelableArrayListExtra("measurements");
            String unknown = intent.getStringExtra("unknown");

            LogUtils.d("SolutionActivity", "получены данные: measurements="
                    + measurements + ", unknown=" + unknown);

            if (measurements != null && !measurements.isEmpty() && unknown != null) {
                displaySolution(measurements, unknown, montserratAlternatesTypeface);
            } else {
                solutionTextView.setText("ошибка: данные не переданы корректно");
                LogUtils.e("SolutionActivity",
                        "некорректные данные: measurements=" + measurements + ", unknown=" + unknown);
            }
        } else {
            solutionTextView.setText("ошибка: intent пуст");
            LogUtils.e("SolutionActivity", "intent отсутствует");
        }
    }

    /**
     * отображает решение задачи
     * @param measurements список измерений
     * @param unknown искомая величина
     * @param typeface шрифт для форматирования решения
     */
    private void displaySolution(
            List<ConcreteMeasurement> measurements,
            String unknown,
            Typeface typeface
    ) {
        FormulaDatabase formulaDatabase = new FormulaDatabase();
        Solver solver = new Solver(formulaDatabase);
        SolutionFormatter formatter = new SolutionFormatter(typeface, displayManager);

        // конвертация в си
        List<ConcreteMeasurement> siMeasurements =
                measurementValidator.requiresConversion(measurements)
                        ? measurementValidator.convertToSI(measurements)
                        : measurements;

        // валидация
        for (ConcreteMeasurement m : siMeasurements) {
            if (!m.validate()) {
                solutionTextView.setText(
                        "ошибка: некорректное измерение: " + m);
                LogUtils.e("SolutionActivity", "некорректное измерение: " + m);
                return;
            }
        }

        Map<String, Double> knownValues = formatter.convertToMap(siMeasurements);
        LogUtils.d("SolutionActivity", "известные величины: " + knownValues);

        try {
            //форматирование
            Solver.SolutionResult result = solver.solve(knownValues, unknown);
            SpannableStringBuilder solution = formatter.formatSolution(
                    measurements, siMeasurements, unknown, result);
            solutionTextView.setText(solution);
            LogUtils.d("SolutionActivity",
                    "решение: " + unknown + " = " + result.getResult());
        } catch (Exception e) {
            solutionTextView.setText(
                    "ошибка вычисления: " + e.getMessage());
            LogUtils.e("SolutionActivity", "ошибка вычисления: "
                    + e.getMessage(), e);
        }
    }
}