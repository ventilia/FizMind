package com.example.fizmind;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.solver.InputAnalyzer;
import com.example.fizmind.solver.Solver;
import com.example.fizmind.solver.SolutionFormatter;
import com.example.fizmind.utils.LogUtils;

import java.util.Map;

/**
 * активити для отображения решения
 */
public class SolutionActivity extends AppCompatActivity {
    private TextView solutionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        solutionTextView = findViewById(R.id.solution_text);

        // получение данных из Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Map<String, Double> knownValues = (Map<String, Double>) extras.getSerializable("knownValues");
            String unknown = extras.getString("unknown");

            LogUtils.d("SolutionActivity", "получены данные: knownValues = " + knownValues + ", unknown = " + unknown);

            if (knownValues != null && unknown != null) {
                displaySolution(knownValues, unknown);
            } else {
                solutionTextView.setText("Ошибка: данные не переданы");
                LogUtils.e("SolutionActivity", "данные не переданы: knownValues = " + knownValues + ", unknown = " + unknown);
            }
        } else {
            solutionTextView.setText("Ошибка: Intent пуст");
            LogUtils.e("SolutionActivity", "Intent extras отсутствуют");
        }
    }

    /**
     * отображает решение на экране
     * @param knownValues известные величины
     * @param unknown неизвестная величина
     */
    private void displaySolution(Map<String, Double> knownValues, String unknown) {
        FormulaDatabase formulaDatabase = new FormulaDatabase();
        InputAnalyzer inputAnalyzer = new InputAnalyzer(formulaDatabase);
        Solver solver = new Solver();
        SolutionFormatter formatter = new SolutionFormatter();

        // поиск подходящей формулы
        Formula formula = inputAnalyzer.findSuitableFormula(knownValues, unknown);
        if (formula == null) {
            solutionTextView.setText("Подходящая формула не найдена");
            LogUtils.w("SolutionActivity", "формула не найдена для knownValues = " + knownValues + ", unknown = " + unknown);
            return;
        }

        try {
            // вычисление результата
            double result = solver.solve(formula, knownValues, unknown);
            LogUtils.d("SolutionActivity", "результат вычисления: " + result);

            // форматирование и отображение решения
            SpannableStringBuilder solution = formatter.formatSolution(knownValues, unknown, formula, result);
            solutionTextView.setText(solution);
        } catch (IllegalArgumentException e) {
            solutionTextView.setText("Ошибка: " + e.getMessage());
            LogUtils.e("SolutionActivity", "ошибка вычисления: " + e.getMessage());
        }
    }
}