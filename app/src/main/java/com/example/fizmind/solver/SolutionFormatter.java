package com.example.fizmind.solver;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import com.example.fizmind.SIConverter;
import com.example.fizmind.animation.CustomTypefaceSpan;
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

    private final Typeface montserratAlternatesTypeface; // шрифт для вторичных надписей
    private boolean useMontserratAlternates = true; // флаг использования MontserratAlternates

    /**
     * конструктор форматировщика
     * @param montserratAlternatesTypeface шрифт MontserratAlternates
     */
    public SolutionFormatter(Typeface montserratAlternatesTypeface) {
        this.montserratAlternatesTypeface = montserratAlternatesTypeface;
    }

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
        int start = builder.length();
        builder.append("Дано:\n");
        int end = builder.length();
        applyMontserratAlternates(builder, start, end);
        for (ConcreteMeasurement measurement : originalMeasurements) {
            String designation = measurement.getDesignation();
            String subscript = measurement.getSubscript();
            String fullDesignation = subscript.isEmpty() ? designation : designation + "_" + subscript;
            builder.append(fullDesignation)
                    .append(" = ")
                    .append(SIConverter.formatValue(measurement.getOriginalValue()))
                    .append(" ")
                    .append(measurement.getOriginalUnit())
                    .append("\n");
        }
        builder.append("\n");

        // шаг 2: перевод в СИ
        start = builder.length();
        builder.append("Перевод в СИ:\n");
        end = builder.length();
        applyMontserratAlternates(builder, start, end);
        for (ConcreteMeasurement original : originalMeasurements) {
            String designation = original.getDesignation();
            String subscript = original.getSubscript();
            String fullDesignation = subscript.isEmpty() ? designation : designation + "_" + subscript;
            if (!original.isSIUnit()) {
                String steps = original.getConversionSteps();
                if (!steps.isEmpty()) {
                    builder.append(fullDesignation)
                            .append(" = ")
                            .append(steps)
                            .append("\n");
                } else {
                    builder.append(fullDesignation)
                            .append(" = ошибка: шаги перевода не сгенерированы\n");
                }
            } else {
                builder.append(fullDesignation)
                        .append(" = ")
                        .append(SIConverter.formatValue(original.getOriginalValue()))
                        .append(" ")
                        .append(original.getOriginalUnit());
                start = builder.length();
                builder.append(" (уже в СИ)\n");
                end = builder.length();
                applyMontserratAlternates(builder, start, end - 1); // применяем к "(уже в СИ)"
            }
        }
        builder.append("\n");

        // шаг 3: указываем формулу
        start = builder.length();
        builder.append("Воспользуемся формулой:\n");
        end = builder.length();
        applyMontserratAlternates(builder, start, end);
        int formulaStart = builder.length();
        builder.append(formula.getExpression());
        int formulaEnd = builder.length();
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), formulaStart, formulaEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n\n");

        // шаг 4: подставляем значения
        start = builder.length();
        builder.append("Подставим значения:\n");
        end = builder.length();
        applyMontserratAlternates(builder, start, end);
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
        start = builder.length();
        builder.append("Результат:\n");
        end = builder.length();
        applyMontserratAlternates(builder, start, end);
        builder.append(unknownDesignation)
                .append(" = ")
                .append(SIConverter.formatValue(result))
                .append("\n");

        // шаг 6: округленный ответ
        start = builder.length();
        builder.append("Ответ: ");
        end = builder.length();
        applyMontserratAlternates(builder, start, end);
        double roundedResult = Math.round(result * 100.0) / 100.0;
        builder.append(unknownDesignation)
                .append(" ≈ ")
                .append(SIConverter.formatValue(roundedResult));

        LogUtils.d("SolutionFormatter", "сформировано решение:\n" + builder.toString());
        return builder;
    }

    /**
     * применяет шрифт MontserratAlternates к указанной части текста
     * @param builder SpannableStringBuilder для форматирования
     * @param start начальный индекс
     * @param end конечный индекс
     */
    private void applyMontserratAlternates(SpannableStringBuilder builder, int start, int end) {
        if (useMontserratAlternates && montserratAlternatesTypeface != null) {
            builder.setSpan(new CustomTypefaceSpan(montserratAlternatesTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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

    /**
     * устанавливает флаг использования шрифта MontserratAlternates
     * @param useMontserratAlternates значение флага
     */
    public void setUseMontserratAlternates(boolean useMontserratAlternates) {
        this.useMontserratAlternates = useMontserratAlternates;
    }
}