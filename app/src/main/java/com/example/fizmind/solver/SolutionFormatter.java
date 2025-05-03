package com.example.fizmind.solver;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import com.example.fizmind.SI.SIConverter;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.keyboard.DisplayManager;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.utils.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolutionFormatter {
    private final Typeface montserratAlternatesTypeface;
    private final DisplayManager displayManager;

    // конструктор с зависимостями
    public SolutionFormatter(Typeface montserratAlternatesTypeface, DisplayManager displayManager) {
        this.montserratAlternatesTypeface = montserratAlternatesTypeface;
        this.displayManager = displayManager;
    }

    // форматирование решения задачи
    public SpannableStringBuilder formatSolution(List<ConcreteMeasurement> originalMeasurements,
                                                 List<ConcreteMeasurement> siMeasurements,
                                                 String unknownDesignation, Solver.SolutionResult result) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // вывод исходных данных
        int start = builder.length();
        builder.append("Дано:\n");
        applyTypeface(builder, start, builder.length());
        for (ConcreteMeasurement measurement : originalMeasurements) {
            String displayDesignation = displayManager.getDisplayTextFromLogicalId(measurement.getBaseDesignation());
            builder.append(displayDesignation)
                    .append(" = ")
                    .append(SIConverter.formatValue(measurement.getOriginalValue()))
                    .append(" ")
                    .append(measurement.getOriginalUnit())
                    .append("\n");
        }
        builder.append("\n");

        // перевод в си
        start = builder.length();
        builder.append("Перевод в СИ:\n");
        applyTypeface(builder, start, builder.length());
        for (ConcreteMeasurement measurement : siMeasurements) {
            String displayDesignation = displayManager.getDisplayTextFromLogicalId(measurement.getBaseDesignation());
            if (!measurement.isSIUnit()) {
                builder.append(displayDesignation)
                        .append(" = ")
                        .append(measurement.getConversionSteps())
                        .append("\n");
            } else {
                builder.append(displayDesignation)
                        .append(" = ")
                        .append(SIConverter.formatValue(measurement.getValue()))
                        .append(" ")
                        .append(measurement.getUnit())
                        .append(" (уже в СИ)\n");
            }
        }
        builder.append("\n");

        // преобразование измерений в карту известных значений
        Map<String, Double> knownValues = convertToMap(siMeasurements);

        // промежуточные вычисления (заглушка, так как шаги не реализованы)
        boolean hasIntermediateSteps = false;
        List<Solver.Step> steps = result.getStepsAsList();
        for (Solver.Step step : steps) {
            if (!step.getVariable().equals(unknownDesignation)) {
                hasIntermediateSteps = true;
                break;
            }
        }
        if (hasIntermediateSteps) {
            start = builder.length();
            builder.append("Промежуточные вычисления:\n");
            applyTypeface(builder, start, builder.length());
            for (Solver.Step step : steps) {
                if (!step.getVariable().equals(unknownDesignation)) {
                    Formula formula = step.getFormula();
                    String displayExpression = displayManager.getDisplayExpression(formula, step.getVariable());
                    builder.append(Html.fromHtml(displayExpression)).append("\n");
                    String substitution = buildSubstitution(displayExpression, formula, knownValues, step.getVariable());
                    builder.append(Html.fromHtml(substitution)).append("\n");
                    String displayVariable = displayManager.getDisplayTextFromLogicalId(step.getVariable());
                    builder.append(displayVariable)
                            .append(" = ")
                            .append(SIConverter.formatValue(step.getValue()))
                            .append(" ")
                            .append(getUnit(step.getVariable()))
                            .append("\n\n");
                    knownValues.put(step.getVariable(), step.getValue());
                }
            }
        }

        // финальный шаг с формулой (заглушка)
        Solver.Step finalStep = steps.isEmpty() ? null : steps.get(steps.size() - 1);
        if (finalStep != null && finalStep.getVariable().equals(unknownDesignation)) {
            start = builder.length();
            builder.append("Воспользуемся формулой:\n");
            applyTypeface(builder, start, builder.length());
            Formula formula = finalStep.getFormula();
            String displayExpression = displayManager.getDisplayExpression(formula, unknownDesignation);
            builder.append(Html.fromHtml(displayExpression)).append("\n\n");

            start = builder.length();
            builder.append("Подставим значения:\n");
            applyTypeface(builder, start, builder.length());
            String substitution = buildSubstitution(displayExpression, formula, knownValues, unknownDesignation);
            builder.append(Html.fromHtml(substitution)).append("\n\n");
        }

        // результат
        start = builder.length();
        builder.append("Результат:\n");
        applyTypeface(builder, start, builder.length());
        String displayUnknown = displayManager.getDisplayTextFromLogicalId(unknownDesignation);
        String unit = getUnit(unknownDesignation);
        builder.append(displayUnknown)
                .append(" = ")
                .append(SIConverter.formatValue(result.getResult()))
                .append(" ")
                .append(unit)
                .append("\n\n");

        // ответ с округлением
        start = builder.length();
        builder.append("Ответ: ");
        applyTypeface(builder, start, builder.length());
        double roundedResult = Math.round(result.getResult() * 100.0) / 100.0;
        builder.append(displayUnknown)
                .append(" ≈ ")
                .append(SIConverter.formatValue(roundedResult))
                .append(" ")
                .append(unit);
        int boldStart = start + "Ответ: ".length();
        builder.setSpan(new StyleSpan(Typeface.BOLD), boldStart, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        LogUtils.d("SolutionFormatter", "решение:\n" + builder.toString());
        return builder;
    }

    // построение подстановки значений в формулу
    private String buildSubstitution(String displayExpression, Formula formula, Map<String, Double> knownValues, String targetVariable) {
        String[] parts = displayExpression.split("=");
        if (parts.length != 2) return "ошибка в формуле";
        String left = parts[0].trim();
        String right = parts[1].trim();

        List<String> formulaVariables = formula.getVariables();
        Map<String, String> displayToFullMap = new HashMap<>();
        for (String fullVar : formulaVariables) {
            String base = fullVar.contains("_") ? fullVar.split("_")[0] : fullVar;
            displayToFullMap.put(base, fullVar);
        }

        for (String displayVar : displayToFullMap.keySet()) {
            if (!displayVar.equals(left)) {
                String fullVar = displayToFullMap.get(displayVar);
                if (knownValues.containsKey(fullVar)) {
                    double value = knownValues.get(fullVar);
                    String valueStr = SIConverter.formatValue(value);
                    right = right.replaceAll("\\b" + displayVar + "\\b", valueStr);
                }
            }
        }

        return left + " = " + right;
    }

    // получение единицы измерения из реестра
    private String getUnit(String designation) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        return pq != null ? pq.getSiUnit() : "";
    }

    // применение шрифта к тексту
    private void applyTypeface(SpannableStringBuilder builder, int start, int end) {
        if (montserratAlternatesTypeface != null) {
            builder.setSpan(new CustomTypefaceSpan(montserratAlternatesTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    // преобразование списка измерений в карту
    public Map<String, Double> convertToMap(List<ConcreteMeasurement> measurements) {
        Map<String, Double> knownValues = new HashMap<>();
        for (ConcreteMeasurement measurement : measurements) {
            knownValues.put(measurement.getBaseDesignation(), measurement.getValue());
        }
        return knownValues;
    }
}