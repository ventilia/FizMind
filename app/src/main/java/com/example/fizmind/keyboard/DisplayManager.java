package com.example.fizmind.keyboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.modules.InputModule;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// менеджер отображения для преобразования данных в видимый формат
public class DisplayManager {

    private final Typeface stixTypeface;
    private final AppDatabase database;

    // конструктор с подключением к базе данных
    public DisplayManager(Typeface stixTypeface, AppDatabase database) {
        this.stixTypeface = stixTypeface;
        this.database = database;
        LogUtils.d("DisplayManager", "инициализирован менеджер отображения");
    }

    // построение текста для поля "Введите обозначение"
    public SpannableStringBuilder buildDesignationsText(
            List<ConcreteMeasurementEntity> measurements,
            StringBuilder designationBuffer,
            StringBuilder valueBuffer,
            StringBuilder unitBuffer,
            StringBuilder operationBuffer,
            StringBuilder valueOperationBuffer,
            String displayDesignation,
            Boolean designationUsesStix,
            InputModule designationSubscriptModule,
            InputController.FocusState focusState,
            String currentInputField
    ) {
        SpannableStringBuilder designationsText = new SpannableStringBuilder();

        // вывод сохраненных измерений из базы данных с восстановлением форматирования
        for (int i = 0; i < measurements.size(); i++) {
            ConcreteMeasurementEntity measurement = measurements.get(i);
            String originalDisplay = measurement.getOriginalDisplay();
            SpannableStringBuilder formattedText = new SpannableStringBuilder(originalDisplay);

            // восстановление шрифта stix для обозначения, если указано
            if (measurement.isUsesStix() && stixTypeface != null) {
                int designationEnd = originalDisplay.indexOf(" = ");
                if (designationEnd != -1) {
                    formattedText.setSpan(new CustomTypefaceSpan(stixTypeface), 0, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // восстановление индексов
            String subscript = measurement.getSubscript();
            if (!subscript.isEmpty()) {
                String designationPart = measurement.getDesignationOperations().isEmpty() ?
                        measurement.getBaseDesignation() :
                        measurement.getDesignationOperations() + "(" + measurement.getBaseDesignation() + ")";
                int subscriptStart = originalDisplay.indexOf(subscript);
                if (subscriptStart != -1) {
                    int subscriptEnd = subscriptStart + subscript.length();
                    formattedText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    formattedText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // восстановление жирного шрифта для результата конверсии
            if (measurement.isConversionMode() && !measurement.isSIUnit() && !measurement.getConversionSteps().isEmpty()) {
                int lastEqualIndex = originalDisplay.lastIndexOf("= ");
                if (lastEqualIndex != -1) {
                    int resultStart = lastEqualIndex + 2;
                    int resultEnd = originalDisplay.length();
                    formattedText.setSpan(new StyleSpan(Typeface.BOLD), resultStart, resultEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            designationsText.append(formattedText);
            if (i < measurements.size() - 1) {
                designationsText.append("\n\n");
            }
        }
        if (!measurements.isEmpty()) {
            designationsText.append("\n\n");
        }

        // рендер текущего ввода или плейсхолдера
        if (designationBuffer.length() > 0 || valueBuffer.length() > 0 || unitBuffer.length() > 0 || designationSubscriptModule != null) {
            int start = designationsText.length();
            if (operationBuffer.length() > 0) {
                designationsText.append(operationBuffer).append("(").append(displayDesignation).append(")");
            } else {
                designationsText.append(displayDesignation);
            }
            int end = designationsText.length();

            if (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) {
                String subscriptText = designationSubscriptModule.getDisplayText().toString();
                int subscriptStart = designationsText.length();
                designationsText.append(subscriptText);
                int subscriptEnd = designationsText.length();
                designationsText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                designationsText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // применение шрифта stix только если designationUsesStix установлен
            if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                designationsText.setSpan(new CustomTypefaceSpan(stixTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            designationsText.append(" = ");
            int valueStart = designationsText.length();
            if (valueOperationBuffer.length() > 0) {
                designationsText.append(valueOperationBuffer);
            } else {
                designationsText.append(valueBuffer);
            }
            int valueEnd = designationsText.length();
            int unitPos = designationsText.length();
            if (unitBuffer.length() > 0) {
                designationsText.append(" ").append(unitBuffer);
            } else {
                designationsText.append(" ?");
            }

            // подсветка активного элемента
            if (focusState == InputController.FocusState.MODULE && designationSubscriptModule != null) {
                int modStart = end;
                int modEnd = modStart + (designationSubscriptModule.getDisplayText().length());
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), modStart, modEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (focusState == InputController.FocusState.DESIGNATION) {
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (focusState == InputController.FocusState.VALUE && valueStart < valueEnd) {
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), valueStart, valueEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (focusState == InputController.FocusState.UNIT && unitPos < designationsText.length()) {
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), unitPos + 1, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            // плейсхолдер
            int start = designationsText.length();
            designationsText.append("Введите обозначение");
            int color = "designations".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            designationsText.setSpan(new ForegroundColorSpan(color), start, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        LogUtils.d("DisplayManager", "построен текст для поля 'Введите обозначение'");
        return designationsText;
    }

    // построение текста для поля "Введите неизвестное"
    public SpannableStringBuilder buildUnknownText(
            List<UnknownQuantityEntity> unknowns,
            String unknownDisplayDesignation,
            Boolean unknownUsesStix,
            InputModule unknownSubscriptModule,
            String currentInputField,
            boolean isConversionMode
    ) {
        SpannableStringBuilder unknownText = new SpannableStringBuilder();

        if (isConversionMode) {
            // в режиме "перевод в си" показываем только плейсхолдер
            int start = unknownText.length();
            unknownText.append("Введите неизвестное");
            int color = "unknown".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            unknownText.setSpan(new ForegroundColorSpan(color), start, unknownText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            // в режиме "физический калькулятор" отображаем данные
            if (unknownDisplayDesignation != null) {
                int start = unknownText.length();
                unknownText.append(unknownDisplayDesignation);
                int end = unknownText.length();
                // применение шрифта stix только если unknownUsesStix установлен
                if (unknownUsesStix != null && unknownUsesStix && stixTypeface != null) {
                    unknownText.setSpan(new CustomTypefaceSpan(stixTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty()) {
                    String subscriptText = unknownSubscriptModule.getDisplayText().toString();
                    int subscriptStart = unknownText.length();
                    unknownText.append(subscriptText);
                    int subscriptEnd = unknownText.length();
                    unknownText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    unknownText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                unknownText.append(" = ?");
            } else if (!unknowns.isEmpty()) {
                UnknownQuantityEntity lastUnknown = unknowns.get(unknowns.size() - 1);
                SpannableStringBuilder formattedText = new SpannableStringBuilder(lastUnknown.getDisplayText());

                // восстановление шрифта stix, если указано
                if (lastUnknown.isUsesStix() && stixTypeface != null) {
                    int designationEnd = formattedText.toString().indexOf(" = ");
                    if (designationEnd != -1) {
                        formattedText.setSpan(new CustomTypefaceSpan(stixTypeface), 0, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                // восстановление индексов
                String subscript = lastUnknown.getSubscript();
                if (!subscript.isEmpty()) {
                    int subscriptStart = formattedText.toString().indexOf(subscript);
                    if (subscriptStart != -1) {
                        int subscriptEnd = subscriptStart + subscript.length();
                        formattedText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        formattedText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                unknownText.append(formattedText);
            } else {
                int start = unknownText.length();
                unknownText.append("Введите неизвестное");
                int color = "unknown".equals(currentInputField) ? Color.BLACK : Color.GRAY;
                unknownText.setSpan(new ForegroundColorSpan(color), start, unknownText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        LogUtils.d("DisplayManager", "построен текст для поля 'Введите неизвестное'");
        return unknownText;
    }

    // получение отображаемого текста из логического идентификатора
    public String getDisplayTextFromLogicalId(String logicalId) {
        if (logicalId == null) return "";
        String displayText = logicalId.replace("designation_", "")
                .replace("_latin", "")
                .replace("_power", "");
        LogUtils.d("DisplayManager", "получен отображаемый текст: " + displayText + " из " + logicalId);
        return displayText;
    }

    // получение отображаемого выражения для формулы
    public String getDisplayExpression(Formula formula, String targetVariable) {
        String expression = formula.getExpressionFor(targetVariable);
        if (expression.contains("/")) {
            String[] parts = expression.split("=");
            if (parts.length == 2) {
                String left = parts[0].trim();
                String right = parts[1].trim();
                String[] fraction = right.split("/");

                if (fraction.length == 2) {
                    left = formatExpression(left);
                    fraction[0] = formatExpression(fraction[0]);
                    fraction[1] = formatExpression(fraction[1]);
                    String result = left + " = <sup>" + fraction[0].trim() + "</sup>/<sub>" + fraction[1].trim() + "</sub>";
                    LogUtils.d("DisplayManager", "сформировано выражение с дробью: " + result);
                    return result;
                }
            }
        }
        String formatted = formatExpression(expression);
        LogUtils.d("DisplayManager", "сформировано выражение: " + formatted);
        return formatted;
    }

    // форматирование выражения
    public String formatExpression(String expression) {
        for (String variable : getVariablesInLine(expression)) {
            String displayVar = getDisplayTextFromLogicalId(variable);
            expression = expression.replace(variable, displayVar);
        }
        return expression;
    }

    // извлечение переменных из строки выражения
    private List<String> getVariablesInLine(String line) {
        List<String> variables = new ArrayList<>();
        List<PhysicalQuantity> quantities = PhysicalQuantityRegistry.getAllQuantities();
        List<String> keys = quantities.stream().map(PhysicalQuantity::getId).collect(Collectors.toList());
        for (String key : keys) {
            if (line.contains(key)) {
                variables.add(key);
            }
        }
        LogUtils.d("DisplayManager", "извлечены переменные из выражения: " + variables);
        return variables;
    }
}