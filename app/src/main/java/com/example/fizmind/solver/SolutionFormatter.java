package com.example.fizmind.solver;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import com.example.fizmind.SI.SIConverter;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.keyboard.DisplayManager;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// форматировщик решения задачи на основе данных из базы данных
public class SolutionFormatter {
    private final Typeface montserratAlternatesTypeface;
    private final DisplayManager displayManager;
    private final AppDatabase database;

    // конструктор
    public SolutionFormatter(Typeface montserratAlternatesTypeface, DisplayManager displayManager, AppDatabase database) {
        this.montserratAlternatesTypeface = montserratAlternatesTypeface;
        this.displayManager = displayManager;
        this.database = database;
    }

    // форматирование решения
    public SpannableStringBuilder formatSolution(Solver.SolutionResult result) {
        List<ConcreteMeasurementEntity> measurements = database.measurementDao().getAllMeasurements();
        List<UnknownQuantityEntity> unknowns = database.unknownQuantityDao().getAllUnknowns();

        if (unknowns.isEmpty()) {
            throw new IllegalStateException("нет неизвестных величин для решения");
        }

        String unknownDesignation = unknowns.get(0).getLogicalDesignation();
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // раздел "Дано"
        int start = builder.length();
        builder.append("Дано:\n");
        applyTypeface(builder, start, builder.length());
        for (ConcreteMeasurementEntity measurement : measurements) {
            String displayDesignation = displayManager.getDisplayTextFromLogicalId(measurement.getBaseDesignation());
            builder.append(displayDesignation)
                    .append(" = ")
                    .append(SIConverter.formatValue(measurement.getOriginalValue()))
                    .append(" ")
                    .append(measurement.getOriginalUnit())
                    .append("\n");
        }
        builder.append("\n");

        // раздел "Перевод в СИ"
        start = builder.length();
        builder.append("Перевод в СИ:\n");
        applyTypeface(builder, start, builder.length());
        for (ConcreteMeasurementEntity measurement : measurements) {
            String displayDesignation = displayManager.getDisplayTextFromLogicalId(measurement.getBaseDesignation());
            if (!measurement.isSIUnit()) {
                String steps = measurement.getConversionSteps();
                if (!steps.isEmpty()) {
                    // формируем строку вида "m = 3g = 3*0.001 = 0.003 kg"
                    builder.append(displayDesignation)
                            .append(" = ")
                            .append(SIConverter.formatValue(measurement.getOriginalValue()))
                            .append(measurement.getOriginalUnit()) // без пробела для компактности
                            .append(" = ")
                            .append(steps)
                            .append("\n");
                } else {
                    builder.append(displayDesignation)
                            .append(" = ")
                            .append(SIConverter.formatValue(measurement.getValue()))
                            .append(" ")
                            .append(measurement.getUnit())
                            .append(" (нет шагов конвертации)\n");
                }
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

        // подготовка известных значений
        Map<String, Double> knownValues = new HashMap<>();
        for (ConcreteMeasurementEntity measurement : measurements) {
            String fullDesignation = measurement.getSubscript().isEmpty() ?
                    measurement.getBaseDesignation() : measurement.getBaseDesignation() + "_" + measurement.getSubscript();
            knownValues.put(fullDesignation, measurement.getValue());
        }

        // промежуточные вычисления
        boolean hasIntermediateSteps = false;
        for (Solver.Step step : result.getSteps()) {
            if (!step.getVariable().equals(unknownDesignation)) {
                hasIntermediateSteps = true;
                break;
            }
        }
        if (hasIntermediateSteps) {
            start = builder.length();
            builder.append("Промежуточные вычисления:\n");
            applyTypeface(builder, start, builder.length());
            for (Solver.Step step : result.getSteps()) {
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

        // финальный шаг
        Solver.Step finalStep = result.getSteps().isEmpty() ? null : result.getSteps().get(result.getSteps().size() - 1);
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

        // ответ
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

    // получение единицы измерения
    private String getUnit(String designation) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        return pq != null ? pq.getSiUnit() : "";
    }

    // применение шрифта
    private void applyTypeface(SpannableStringBuilder builder, int start, int end) {
        if (montserratAlternatesTypeface != null) {
            builder.setSpan(new CustomTypefaceSpan(montserratAlternatesTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}