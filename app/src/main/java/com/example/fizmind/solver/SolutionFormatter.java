package com.example.fizmind.solver;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// форматировщик решения задачи на основе данных из базы данных
public class SolutionFormatter {
    private final Typeface montserratAlternatesTypeface; // шрифт для заголовков и текста
    private final Typeface stixTypeface; // шрифт STIX для физических обозначений
    private final DisplayManager displayManager; // менеджер отображения текста
    private final AppDatabase database; // база данных приложения

    // конструктор с добавлением шрифта STIX
    public SolutionFormatter(Typeface montserratAlternatesTypeface, Typeface stixTypeface, DisplayManager displayManager, AppDatabase database) {
        this.montserratAlternatesTypeface = montserratAlternatesTypeface;
        this.stixTypeface = stixTypeface;
        this.displayManager = displayManager;
        this.database = database;
    }

    // метод для сокращения дроби
    private String simplifyFraction(int numerator, int denominator) {
        if (denominator == 0) return numerator + "/" + denominator; // избежание деления на ноль
        int gcd = Solver.gcd(numerator, denominator);
        int simplifiedNumerator = numerator / gcd;
        int simplifiedDenominator = denominator / gcd;
        // если знаменатель стал 1, возвращаем целое число
        if (simplifiedDenominator == 1) {
            return String.valueOf(simplifiedNumerator);
        }
        return simplifiedNumerator + "/" + simplifiedDenominator;
    }

    // метод для формирования html-строки сокращённой дроби или целого числа
    private String formatSimplifiedFraction(String simplified) {
        // если simplified — целое число, возвращаем его без форматирования дроби
        if (!simplified.contains("/")) {
            return simplified;
        }
        // иначе форматируем как дробь в html
        String[] parts = simplified.split("/");
        return "<sup>" + parts[0] + "</sup>/<sub>" + parts[1] + "</sub>";
    }

    // форматирование решения с учетом шрифта STIX
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
        applyTypeface(builder, start, builder.length(), montserratAlternatesTypeface);
        for (ConcreteMeasurementEntity measurement : measurements) {
            String baseDesignation = measurement.getBaseDesignation();
            String displayDesignation = displayManager.getDisplayTextFromLogicalId(baseDesignation);
            boolean usesStix = measurement.isUsesStix(); // флаг использования STIX

            int designationStart = builder.length();
            builder.append(displayDesignation);
            int designationEnd = builder.length();

            // применение шрифта STIX к обозначению, если требуется
            if (usesStix && stixTypeface != null) {
                builder.setSpan(new CustomTypefaceSpan(stixTypeface), designationStart, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            builder.append(" = ")
                    .append(SIConverter.formatValue(measurement.getOriginalValue()))
                    .append(" ")
                    .append(measurement.getOriginalUnit())
                    .append("\n");
        }
        builder.append("\n");

        // раздел "Перевод в СИ"
        start = builder.length();
        builder.append("Перевод в СИ:\n");
        applyTypeface(builder, start, builder.length(), montserratAlternatesTypeface);
        for (ConcreteMeasurementEntity measurement : measurements) {
            String displayDesignation = displayManager.getDisplayTextFromLogicalId(measurement.getBaseDesignation());
            boolean usesStix = measurement.isUsesStix();

            int designationStart = builder.length();
            builder.append(displayDesignation);
            int designationEnd = builder.length();

            // применение шрифта STIX к обозначению
            if (usesStix && stixTypeface != null) {
                builder.setSpan(new CustomTypefaceSpan(stixTypeface), designationStart, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (!measurement.isSIUnit()) {
                String steps = measurement.getConversionSteps();
                if (!steps.isEmpty()) {
                    builder.append(" = ")
                            .append(SIConverter.formatValue(measurement.getOriginalValue()))
                            .append(measurement.getOriginalUnit())
                            .append(" = ")
                            .append(steps)
                            .append("\n");
                } else {
                    builder.append(" = ")
                            .append(SIConverter.formatValue(measurement.getValue()))
                            .append(" ")
                            .append(measurement.getUnit())
                            .append(" (нет шагов конвертации)\n");
                }
            } else {
                builder.append(" = ")
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
            applyTypeface(builder, start, builder.length(), montserratAlternatesTypeface);
            for (Solver.Step step : result.getSteps()) {
                if (!step.getVariable().equals(unknownDesignation)) {
                    Formula formula = step.getFormula();
                    String displayExpression = displayManager.getDisplayExpression(formula, step.getVariable());
                    builder.append(Html.fromHtml(displayExpression)).append("\n");
                    String substitution = buildSubstitution(displayExpression, formula, knownValues, step.getVariable());
                    builder.append(Html.fromHtml(substitution)).append("\n");

                    String displayVariable = displayManager.getDisplayTextFromLogicalId(step.getVariable());
                    boolean usesStix = isUsesStix(step.getVariable(), measurements); // определение флага STIX

                    int varStart = builder.length();
                    builder.append(displayVariable);
                    int varEnd = builder.length();

                    // применение шрифта STIX к промежуточной переменной
                    if (usesStix && stixTypeface != null) {
                        builder.setSpan(new CustomTypefaceSpan(stixTypeface), varStart, varEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    builder.append(" = ")
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
            applyTypeface(builder, start, builder.length(), montserratAlternatesTypeface);
            Formula formula = finalStep.getFormula();
            String displayExpression = displayManager.getDisplayExpression(formula, unknownDesignation);
            builder.append(Html.fromHtml(displayExpression)).append("\n\n");

            start = builder.length();
            builder.append("Подставим значения:\n");
            applyTypeface(builder, start, builder.length(), montserratAlternatesTypeface);
            String substitution = buildSubstitution(displayExpression, formula, knownValues, unknownDesignation);
            builder.append(Html.fromHtml(substitution)).append("\n\n");
        }

        // результат
        start = builder.length();
        builder.append("Результат:\n");
        applyTypeface(builder, start, builder.length(), montserratAlternatesTypeface);
        String displayUnknown = displayManager.getDisplayTextFromLogicalId(unknownDesignation);
        boolean usesStix = unknowns.get(0).isUsesStix(); // флаг STIX для неизвестной величины

        int unknownStart = builder.length();
        builder.append(displayUnknown);
        int unknownEnd = builder.length();

        // применение шрифта STIX к неизвестной величине
        if (usesStix && stixTypeface != null) {
            builder.setSpan(new CustomTypefaceSpan(stixTypeface), unknownStart, unknownEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        String unit = getUnit(unknownDesignation);
        builder.append(" = ")
                .append(SIConverter.formatValue(result.getResult()))
                .append(" ")
                .append(unit)
                .append("\n\n");

        // ответ
        start = builder.length();
        builder.append("Ответ: ");
        applyTypeface(builder, start, builder.length(), montserratAlternatesTypeface);
        double roundedResult = Math.round(result.getResult() * 100.0) / 100.0;
        builder.append(displayUnknown)
                .append(" ≈ ")
                .append(SIConverter.formatValue(roundedResult))
                .append(" ")
                .append(unit);

        // применение стиля полужирного шрифта к тексту после "Ответ: "
        int boldStart = start + "Ответ: ".length();
        builder.setSpan(new StyleSpan(Typeface.BOLD), boldStart, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // применение подчеркивания ко всей строке "Ответ: "
        int answerStart = start; // начинаем с "Ответ: "
        int answerEnd = builder.length();
        builder.setSpan(new UnderlineSpan(), answerStart, answerEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // применение шрифта STIX к обозначению в ответе
        if (usesStix && stixTypeface != null) {
            int displayUnknownStart = start + "Ответ: ".length();
            int displayUnknownEnd = displayUnknownStart + displayUnknown.length();
            builder.setSpan(new CustomTypefaceSpan(stixTypeface), displayUnknownStart, displayUnknownEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        LogUtils.d("SolutionFormatter", "решение:\n" + builder.toString());
        return builder;
    }

    // метод для проверки, нужно ли использовать шрифт STIX для обозначения
    private boolean isUsesStix(String designation, List<ConcreteMeasurementEntity> measurements) {
        // поиск измерения с данным обозначением
        for (ConcreteMeasurementEntity measurement : measurements) {
            String fullDesignation = measurement.getSubscript().isEmpty() ?
                    measurement.getBaseDesignation() : measurement.getBaseDesignation() + "_" + measurement.getSubscript();
            if (fullDesignation.equals(designation)) {
                return measurement.isUsesStix();
            }
        }
        return false; // по умолчанию STIX не используется, если обозначение не найдено
    }

    // построение подстановки значений в формулу с сокращением
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

        // подстановка значений
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

        String substitution = left + " = " + right;
        LogUtils.d("SolutionFormatter", "подстановка: " + substitution);

        Pattern fractionPattern = Pattern.compile("<sup>(\\d+)</sup>\\s*/\\s*<sub>(\\d+)</sub>");
        Matcher matcher = fractionPattern.matcher(right);
        if (matcher.find()) {
            int numerator = Integer.parseInt(matcher.group(1));
            int denominator = Integer.parseInt(matcher.group(2));
            int gcd = Solver.gcd(numerator, denominator);
            if (gcd > 1) {
                String simplified = simplifyFraction(numerator, denominator);
                LogUtils.d("SolutionFormatter", "найдена дробь: " + numerator + "/" + denominator + " → сокращена до: " + simplified);
                String simplifiedHtml = formatSimplifiedFraction(simplified);
                String reductionStep = "Сократим на " + gcd;
                substitution = left + " = " + right + "<br>" + reductionStep + "<br>" + left + " = " + simplifiedHtml;
            }
        } else {
            Pattern plainFractionPattern = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");
            Matcher plainMatcher = plainFractionPattern.matcher(right);
            if (plainMatcher.find()) {
                int numerator = Integer.parseInt(plainMatcher.group(1));
                int denominator = Integer.parseInt(plainMatcher.group(2));
                int gcd = Solver.gcd(numerator, denominator);
                if (gcd > 1) {
                    String simplified = simplifyFraction(numerator, denominator);
                    LogUtils.d("SolutionFormatter", "найдена обычная дробь: " + numerator + "/" + denominator + " → сокращена до: " + simplified);
                    String simplifiedHtml = formatSimplifiedFraction(simplified);
                    String reductionStep = "Сократим на " + gcd;
                    substitution = left + " = " + right + "<br>" + reductionStep + "<br>" + left + " = " + simplifiedHtml;
                }
            }
        }

        return substitution;
    }

    // получение единицы измерения
    private String getUnit(String designation) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        return pq != null ? pq.getSiUnit() : "";
    }

    // применение шрифта
    private void applyTypeface(SpannableStringBuilder builder, int start, int end, Typeface typeface) {
        if (typeface != null) {
            builder.setSpan(new CustomTypefaceSpan(typeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}