package com.example.fizmind.solver;

import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.example.fizmind.SIConverter;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.utils.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Форматировщик решения для красивого вывода
 */
public class SolutionFormatter {

    /**
     * Форматирует пошаговое решение
     * @param originalMeasurements исходные измерения
     * @param siMeasurements измерения после конвертации в СИ
     * @param unknownDesignation обозначение неизвестной
     * @param formula использованная формула
     * @param result результат вычисления
     * @return отформатированное решение
     */
    public SpannableStringBuilder formatSolution(List<ConcreteMeasurement> originalMeasurements,
                                                 List<ConcreteMeasurement> siMeasurements,
                                                 String unknownDesignation, Formula formula, double result) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // Шаг 1: Дано
        builder.append("Дано:\n");
        for (ConcreteMeasurement measurement : originalMeasurements) {
            builder.append(measurement.getOriginalDisplay()).append("\n");
        }
        builder.append("\n");

        // Шаг 2: Перевод в СИ
        builder.append("Перевод в СИ:\n");
        boolean conversionPerformed = false;
        for (int i = 0; i < originalMeasurements.size(); i++) {
            ConcreteMeasurement original = originalMeasurements.get(i);
            ConcreteMeasurement si = siMeasurements.get(i);
            builder.append(original.getDesignation()).append(": ");
            if (!original.isSIUnit()) {
                conversionPerformed = true;
                String steps = original.getConversionSteps();
                if (!steps.isEmpty()) {
                    builder.append(original.getDesignation())
                            .append(" = ")
                            .append(steps)
                            .append("\n");
                } else {
                    builder.append("ошибка: шаги перевода не сгенерированы\n");
                }
            } else {
                builder.append(original.getOriginalDisplay()).append(" (уже в СИ)\n");
            }
        }
        if (!conversionPerformed) {
            builder.append("Все величины уже в СИ.\n");
        }
        builder.append("\n");

        // Шаг 3: Воспользуемся формулой
        builder.append("Воспользуемся формулой:\n");
        int formulaStart = builder.length();
        builder.append(formula.getExpression());
        int formulaEnd = builder.length();
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), formulaStart, formulaEnd,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n\n");

        // Шаг 4: Подставим значения
        builder.append("Подставим значения:\n");
        StringBuilder substitution = new StringBuilder();
        substitution.append(unknownDesignation).append(" = ");
        for (String var : formula.getVariables()) {
            if (!var.equals(unknownDesignation)) {
                ConcreteMeasurement measurement = findMeasurement(siMeasurements, var);
                if (measurement != null) {
                    substitution.append(SIConverter.formatValue(measurement.getValue())).append(" × ");
                }
            }
        }
        substitution.delete(substitution.length() - 3, substitution.length()); // Убираем лишнее " × "
        builder.append(substitution.toString()).append("\n");

        // Шаг 5: Результат
        builder.append("Результат:\n");
        builder.append(unknownDesignation)
                .append(" = ")
                .append(SIConverter.formatValue(result))
                .append("\n");

        // Шаг 6: Округленный ответ
        double roundedResult = Math.round(result * 100.0) / 100.0;
        builder.append("Ответ: ")
                .append(unknownDesignation)
                .append(" ≈ ")
                .append(SIConverter.formatValue(roundedResult));

        LogUtils.d("SolutionFormatter", "Сформировано решение:\n" + builder.toString());
        return builder;
    }

    /**
     * Находит измерение по обозначению
     * @param measurements список измерений
     * @param designation обозначение
     * @return измерение или null
     */
    private ConcreteMeasurement findMeasurement(List<ConcreteMeasurement> measurements, String designation) {
        for (ConcreteMeasurement measurement : measurements) {
            if (measurement.getDesignation().equals(designation)) {
                return measurement;
            }
        }
        return null;
    }

    /**
     * Преобразует список измерений в Map для вычислений
     * @param measurements список измерений
     * @return карта обозначений и значений
     */
    public Map<String, Double> convertToMap(List<ConcreteMeasurement> measurements) {
        Map<String, Double> knownValues = new HashMap<>();
        for (ConcreteMeasurement measurement : measurements) {
            knownValues.put(measurement.getDesignation(), measurement.getValue());
        }
        return knownValues;
    }
}