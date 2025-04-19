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
 * форматировщик решения для красивого вывода
 */
public class SolutionFormatter {

    /**
     * форматирует пошаговое решение
     * @param originalMeasurements исходные измерения
     * @param siMeasurements измерения после конвертации в СИ
     * @param unknownDesignation обозначение неизвестной величины
     * @param formula использованная формула
     * @param result результат вычисления
     * @return отформатированное решение в виде SpannableStringBuilder
     */
    public SpannableStringBuilder formatSolution(List<ConcreteMeasurement> originalMeasurements,
                                                 List<ConcreteMeasurement> siMeasurements,
                                                 String unknownDesignation, Formula formula, double result) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // шаг 1: выводим "Дано"
        builder.append("Дано:\n");
        for (ConcreteMeasurement measurement : originalMeasurements) {
            builder.append(measurement.getOriginalDisplay()).append("\n");
        }
        builder.append("\n");

        // шаг 2: перевод в СИ
        builder.append("Перевод в СИ:\n");
        for (ConcreteMeasurement original : originalMeasurements) {
            if (!original.isSIUnit()) {
                String steps = original.getConversionSteps();
                if (!steps.isEmpty()) {
                    builder.append(original.getDesignation())
                            .append(" = ")
                            .append(steps)
                            .append("\n");
                } else {
                    builder.append(original.getDesignation())
                            .append(" = ошибка: шаги перевода не сгенерированы\n");
                }
            } else {
                builder.append(original.getDesignation())
                        .append(" = ")
                        .append(SIConverter.formatValue(original.getValue()))
                        .append(" ")
                        .append(original.getUnit())
                        .append(" (уже в СИ)\n");
            }
        }
        builder.append("\n");

        // шаг 3: указываем формулу
        builder.append("Воспользуемся формулой:\n");
        int formulaStart = builder.length();
        builder.append(formula.getExpression());
        int formulaEnd = builder.length();
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), formulaStart, formulaEnd,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n\n");

        // шаг 4: подставляем значения
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
        substitution.delete(substitution.length() - 3, substitution.length()); // убираем лишнее " × "
        builder.append(substitution.toString()).append("\n");

        // шаг 5: выводим результат
        builder.append("Результат:\n");
        builder.append(unknownDesignation)
                .append(" = ")
                .append(SIConverter.formatValue(result))
                .append("\n");

        // шаг 6: округленный ответ
        double roundedResult = Math.round(result * 100.0) / 100.0;
        builder.append("Ответ: ")
                .append(unknownDesignation)
                .append(" ≈ ")
                .append(SIConverter.formatValue(roundedResult));

        LogUtils.d("SolutionFormatter", "сформировано решение:\n" + builder.toString());
        return builder;
    }

    /**
     * находит измерение по обозначению
     * @param measurements список измерений
     * @param designation обозначение для поиска
     * @return найденное измерение или null, если не найдено
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
     * преобразует список измерений в карту для вычислений
     * @param measurements список измерений
     * @return карта с обозначениями и значениями
     */
    public Map<String, Double> convertToMap(List<ConcreteMeasurement> measurements) {
        Map<String, Double> knownValues = new HashMap<>();
        for (ConcreteMeasurement measurement : measurements) {
            knownValues.put(measurement.getDesignation(), measurement.getValue());
        }
        return knownValues;
    }
}