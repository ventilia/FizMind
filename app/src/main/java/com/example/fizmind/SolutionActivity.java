package com.example.fizmind;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.solver.InputAnalyzer;
import com.example.fizmind.solver.MeasurementValidator;
import com.example.fizmind.solver.MeasurementValidatorImpl;
import com.example.fizmind.solver.SolutionFormatter;
import com.example.fizmind.solver.Solver;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * активити для отображения решения физической задачи
 */
public class SolutionActivity extends AppCompatActivity {
    private TextView solutionTextView;
    private MeasurementValidator measurementValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        solutionTextView = findViewById(R.id.solution_text);
        measurementValidator = new MeasurementValidatorImpl();

        // загрузка шрифта MontserratAlternates
        Typeface montserratAlternatesTypeface = Typeface.createFromAsset(getAssets(), "fonts/MontserratAlternates-Regular.ttf");

        // настройка кнопки "назад"
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<ConcreteMeasurement> measurements = intent.getParcelableArrayListExtra("measurements");
            String unknown = intent.getStringExtra("unknown");

            LogUtils.d("SolutionActivity", "получены данные: measurements=" + measurements + ", unknown=" + unknown);

            if (measurements != null && !measurements.isEmpty() && unknown != null) {
                displaySolution(measurements, unknown, montserratAlternatesTypeface);
            } else {
                solutionTextView.setText("ошибка: данные не переданы корректно");
                LogUtils.e("SolutionActivity", "некорректные данные: measurements=" + measurements + ", unknown=" + unknown);
            }
        } else {
            solutionTextView.setText("ошибка: Intent пуст");
            LogUtils.e("SolutionActivity", "Intent отсутствует");
        }
    }

    /**
     * отображает решение задачи на экране
     * @param measurements исходные измерения
     * @param unknown неизвестная величина
     * @param typeface шрифт для форматирования
     */
    private void displaySolution(List<ConcreteMeasurement> measurements, String unknown, Typeface typeface) {
        FormulaDatabase formulaDatabase = new FormulaDatabase();
        InputAnalyzer inputAnalyzer = new InputAnalyzer(formulaDatabase);
        Solver solver = new Solver();
        SolutionFormatter formatter = new SolutionFormatter(typeface);

        // конвертация измерений в СИ
        List<ConcreteMeasurement> siMeasurements = measurementValidator.requiresConversion(measurements)
                ? measurementValidator.convertToSI(measurements)
                : measurements;

        // валидация измерений
        for (ConcreteMeasurement measurement : siMeasurements) {
            if (!measurement.validate()) {
                solutionTextView.setText("ошибка: некорректное измерение: " + measurement);
                LogUtils.e("SolutionActivity", "некорректное измерение: " + measurement);
                return;
            }
        }

        // преобразование измерений в карту
        Map<String, Double> knownValues = formatter.convertToMap(siMeasurements);
        LogUtils.d("SolutionActivity", "известные величины: " + knownValues);

        // поиск пути решения
        List<Formula> formulaPath = inputAnalyzer.findFormulaPath(knownValues, unknown);
        if (formulaPath == null || formulaPath.isEmpty()) {
            solutionTextView.setText("ошибка: решение не найдено");
            LogUtils.w("SolutionActivity", "путь не найден для " + unknown);
            return;
        }

        // выбор подходящей формулы для вычисления неизвестной
        Formula targetFormula = null;
        for (Formula formula : formulaPath) {
            List<String> variables = formula.getVariables();
            if (variables.contains(unknown) && variables.stream().filter(var -> !var.equals(unknown)).allMatch(knownValues::containsKey)) {
                targetFormula = formula;
                break;
            }
        }

        if (targetFormula == null) {
            solutionTextView.setText("ошибка: подходящая формула не найдена");
            LogUtils.w("SolutionActivity", "не найдена формула для вычисления " + unknown);
            return;
        }

        // вычисление результата
        try {
            double result = solver.solve(targetFormula, knownValues, unknown);
            SpannableStringBuilder solution = formatter.formatSolution(measurements, siMeasurements, unknown, targetFormula, result);
            solutionTextView.setText(solution);
            LogUtils.d("SolutionActivity", "решение: " + unknown + " = " + result);
        } catch (Exception e) {
            solutionTextView.setText("ошибка вычисления: " + e.getMessage());
            LogUtils.e("SolutionActivity", "ошибка вычисления: " + e.getMessage(), e);
        }
    }
}