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
 * активити для отображения решения
 */
public class SolutionActivity extends AppCompatActivity {
    private TextView solutionTextView;
    private MeasurementValidator measurementValidator;

    /**
     * создание активити и инициализация интерфейса
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        solutionTextView = findViewById(R.id.solution_text);
        measurementValidator = new MeasurementValidatorImpl();

        // получение данных из intent
        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<ConcreteMeasurement> measurements = intent.getParcelableArrayListExtra("measurements");
            String unknown = intent.getStringExtra("unknown");

            // логируем полученные данные для отладки
            LogUtils.d("SolutionActivity", "получены данные: measurements=" + (measurements != null ? measurements.toString() : "null") + ", unknown=" + unknown);

            if (measurements != null && !measurements.isEmpty() && unknown != null) {
                displaySolution(measurements, unknown);
            } else {
                solutionTextView.setText("Ошибка: данные не переданы корректно");
                LogUtils.e("SolutionActivity", "данные не переданы: measurements=" + measurements + ", unknown=" + unknown);
            }
        } else {
            solutionTextView.setText("Ошибка: Intent пуст");
            LogUtils.e("SolutionActivity", "Intent extras отсутствуют");
        }
    }

    /**
     * отображает решение на экране
     * @param measurements список измерений
     * @param unknown неизвестная величина
     */
    private void displaySolution(List<ConcreteMeasurement> measurements, String unknown) {
        FormulaDatabase formulaDatabase = new FormulaDatabase();
        InputAnalyzer inputAnalyzer = new InputAnalyzer(formulaDatabase);
        Solver solver = new Solver();
        SolutionFormatter formatter = new SolutionFormatter();

        // проверка и конвертация в СИ, если требуется
        List<ConcreteMeasurement> siMeasurements = measurements;
        if (measurementValidator.requiresConversion(measurements)) {
            siMeasurements = measurementValidator.convertToSI(measurements);
        }

        // преобразование в map для вычислений
        Map<String, Double> knownValues = formatter.convertToMap(siMeasurements);

        // поиск подходящей формулы
        Formula formula = inputAnalyzer.findSuitableFormula(knownValues, unknown);
        if (formula == null) {
            solutionTextView.setText("Подходящая формула не найдена");
            LogUtils.w("SolutionActivity", "формула не найдена для knownValues=" + knownValues + ", unknown=" + unknown);
            return;
        }

        try {
            // вычисление результата
            double result = solver.solve(formula, knownValues, unknown);
            LogUtils.d("SolutionActivity", "результат вычисления: " + result);

            // форматирование и отображение решения с учетом единиц измерения
            SpannableStringBuilder solution = formatter.formatSolution(measurements, siMeasurements, unknown, formula, result);
            solutionTextView.setText(solution);
        } catch (IllegalArgumentException e) {
            solutionTextView.setText("Ошибка: " + e.getMessage());
            LogUtils.e("SolutionActivity", "ошибка вычисления: " + e.getMessage());
        }
    }
}