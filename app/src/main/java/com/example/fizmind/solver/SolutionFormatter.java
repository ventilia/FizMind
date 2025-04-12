package com.example.fizmind.solver;

import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.example.fizmind.formulas.Formula;

import java.util.Map;

/**
 * форматировщик решения для красивого вывода
 */
public class SolutionFormatter {
    /**
     * форматирует пошаговое решение
     * @param knownValues карта известных величин
     * @param unknownDesignation обозначение неизвестной
     * @param formula использованная формула
     * @param result результат вычисления
     * @return отформатированное решение
     */
    public SpannableStringBuilder formatSolution(Map<String, Double> knownValues, String unknownDesignation,
                                                 Formula formula, double result) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // шаг 1: известные величины
        builder.append("Дано:\n");
        for (Map.Entry<String, Double> entry : knownValues.entrySet()) {
            builder.append(entry.getKey()).append(" = ").append(String.valueOf(entry.getValue())).append("\n");
        }
        builder.append("\n");

        // шаг 2: формула
        builder.append("Формула:\n");
        int formulaStart = builder.length();
        builder.append(formula.getExpression());
        int formulaEnd = builder.length();
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), formulaStart, formulaEnd,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n\n");

        // шаг 3: подстановка значений
        builder.append("Подставим значения:\n");
        StringBuilder substitution = new StringBuilder();
        substitution.append(unknownDesignation).append(" = ");
        for (String var : formula.getVariables()) {
            if (!var.equals(unknownDesignation)) {
                substitution.append(knownValues.get(var)).append(" * ");
            }
        }
        substitution.delete(substitution.length() - 3, substitution.length()); // убираем лишнее " * "
        builder.append(substitution.toString()).append("\n");

        // шаг 4: результат
        builder.append("Результат:\n");
        builder.append(unknownDesignation).append(" = ").append(String.valueOf(result)).append("\n");

        // шаг 5: округленный ответ
        double roundedResult = Math.round(result * 100.0) / 100.0;
        builder.append("Ответ: ").append(unknownDesignation).append(" ≈ ").append(String.valueOf(roundedResult));

        return builder;
    }
}