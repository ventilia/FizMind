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
     * @param unknownDesignation обозначение неизвестной
     * @param formula использованная формула
     * @param result результат вычисления
     * @return отформатированное решение
     */
    public SpannableStringBuilder formatSolution(List<ConcreteMeasurement> originalMeasurements,
                                                 List<ConcreteMeasurement> siMeasurements,
                                                 String unknownDesignation, Formula formula, double result) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // шаг 1: дано
        builder.append("Дано:\n");
        for (ConcreteMeasurement measurement : originalMeasurements) {
            builder.append(measurement.getOriginalDisplay()).append("\n");
        }
        builder.append("\n");

        // шаг 2: перевод в СИ, если требуется
        boolean conversionNeeded = false;
        for (int i = 0; i < originalMeasurements.size(); i++) {
            ConcreteMeasurement original = originalMeasurements.get(i);
            ConcreteMeasurement si = siMeasurements.get(i);
            if (!original.isSIUnit()) {
                conversionNeeded = true;
                builder.append(original.getDesignation()).append(" требуется перевести в СИ\n");
                builder.append(original.getConversionSteps()).append("\n\n");
            }
        }
        if (conversionNeeded) {
            builder.append("\n");
        }

        // шаг 3: воспользуемся формулой
        builder.append("Воспользуемся формулой:\n");
        int formulaStart = builder.length();
        builder.append(formula.getExpression());
        int formulaEnd = builder.length();
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), formulaStart, formulaEnd,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n\n");

        // шаг 4: подставим значения
        builder.append("Подставим значения:\n");
        StringBuilder substitution = new StringBuilder();
        substitution.append(unknownDesignation).append(" = ");
        for (String var : formula.getVariables()) {
            if (!var.equals(unknownDesignation)) {
                ConcreteMeasurement measurement = findMeasurement(siMeasurements, var);
                if (measurement != null) {
                    substitution.append(SIConverter.formatValue(measurement.getValue())).append(" * ");
                }
            }
        }
        substitution.delete(substitution.length() - 3, substitution.length()); // убираем лишнее " * "
        builder.append(substitution.toString()).append("\n");

        // шаг 5: результат
        builder.append("Результат:\n");
        builder.append(unknownDesignation).append(" = ").append(SIConverter.formatValue(result)).append("\n");

        // шаг 6: округленный ответ
        double roundedResult = Math.round(result * 100.0) / 100.0;
        builder.append("Ответ: ").append(unknownDesignation).append(" ≈ ").append(SIConverter.formatValue(roundedResult));

        LogUtils.d("SolutionFormatter", "сформировано решение:\n" + builder.toString());
        return builder;
    }

    /**
     * находит измерение по обозначению
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
     * преобразует список измерений в Map для вычислений
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