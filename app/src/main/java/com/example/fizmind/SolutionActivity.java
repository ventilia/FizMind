package com.example.fizmind;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
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
import java.util.List;
import java.util.Map;

/**
 * активити для отображения решения физической задачи
 */
public class SolutionActivity extends AppCompatActivity {
    private TextView solutionTextView;
    private MeasurementValidator measurementValidator;

    /**
     * инициализация активити
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        solutionTextView = findViewById(R.id.solution_text);
        measurementValidator = new MeasurementValidatorImpl();

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<ConcreteMeasurement> measurements = intent.getParcelableArrayListExtra("measurements");
            String unknown = intent.getStringExtra("unknown");

            LogUtils.d("SolutionActivity", "получены данные: measurements=" +
                    (measurements != null ? measurements.toString() : "null") + ", unknown=" + unknown);

            if (measurements != null && !measurements.isEmpty() && unknown != null) {
                displaySolution(measurements, unknown);
            } else {
                solutionTextView.setText("ошибка: данные не переданы корректно");
                LogUtils.e("SolutionActivity", "некорректные входные данные: measurements=" +
                        measurements + ", unknown=" + unknown);
            }
        } else {
            solutionTextView.setText("ошибка: Intent пуст");
            LogUtils.e("SolutionActivity", "Intent extras отсутствуют");
        }
    }

    /**
     * отображает решение на экране
     * @param measurements список исходных измерений
     * @param unknown обозначение неизвестной величины
     */
    private void displaySolution(List<ConcreteMeasurement> measurements, String unknown) {
        FormulaDatabase formulaDatabase = new FormulaDatabase();
        InputAnalyzer inputAnalyzer = new InputAnalyzer(formulaDatabase);
        Solver solver = new Solver();
        SolutionFormatter formatter = new SolutionFormatter();

        // проверка и конвертация в СИ
        List<ConcreteMeasurement> siMeasurements = measurements;
        if (measurementValidator.requiresConversion(measurements)) {
            LogUtils.d("SolutionActivity", "выполняется конвертация измерений в СИ");
            siMeasurements = measurementValidator.convertToSI(measurements);
            LogUtils.d("SolutionActivity", "измерения после конвертации: " + siMeasurements);
        } else {
            LogUtils.d("SolutionActivity", "конвертация в СИ не требуется");
        }

        // проверка валидности измерений
        for (ConcreteMeasurement measurement : siMeasurements) {
            if (!measurement.validate()) {
                solutionTextView.setText("ошибка: некорректное измерение: " + measurement.toString());
                LogUtils.e("SolutionActivity", "некорректное измерение: " + measurement);
                return;
            }
        }

        // преобразование измерений в карту
        Map<String, Double> knownValues = formatter.convertToMap(siMeasurements);
        LogUtils.d("SolutionActivity", "измерения преобразованы в карту: " + knownValues);

        // поиск подходящей формулы
        Formula formula = inputAnalyzer.findSuitableFormula(knownValues, unknown);
        if (formula == null) {
            solutionTextView.setText("ошибка: подходящая формула не найдена");
            LogUtils.w("SolutionActivity", "формула не найдена для knownValues=" + knownValues + ", unknown=" + unknown);
            return;
        }
        LogUtils.d("SolutionActivity", "найдена формула: " + formula.getExpression());

        // вычисление результата
        try {
            double result = solver.solve(formula, knownValues, unknown);
            LogUtils.d("SolutionActivity", "результат вычисления: " + unknown + " = " + result);

            // форматирование и отображение решения
            SpannableStringBuilder solution = formatter.formatSolution(measurements, siMeasurements, unknown, formula, result);
            solutionTextView.setText(solution);
            LogUtils.d("SolutionActivity", "решение отформатировано и отображено");
        } catch (IllegalArgumentException e) {
            solutionTextView.setText("ошибка вычисления: " + e.getMessage());
            LogUtils.e("SolutionActivity", "ошибка вычисления: " + e.getMessage());
        } catch (Exception e) {
            solutionTextView.setText("ошибка: неожиданная ошибка при вычислении");
            LogUtils.e("SolutionActivity", "неожиданная ошибка: " + e.getMessage());
        }
    }
}