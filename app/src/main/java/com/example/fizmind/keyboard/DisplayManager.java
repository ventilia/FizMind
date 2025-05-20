package com.example.fizmind.keyboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;

import com.example.fizmind.SI.SIConverter;
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

// менеджер отображения текста в интерфейсе
public class DisplayManager {

    private final Typeface stixTypeface; // шрифт STIX для математических символов
    private final AppDatabase database;  // база данных приложения

    // конструктор с инициализацией шрифта и базы данных
    public DisplayManager(Typeface stixTypeface, AppDatabase database) {
        this.stixTypeface = stixTypeface;
        this.database = database;
        LogUtils.d("DisplayManager", "инициализирован менеджер отображения");
    }

    // построение текста для поля "Введите обозначение" с форматированием
    public SpannableStringBuilder buildDesignationsText(
            List<ConcreteMeasurementEntity> measurements,      // список сохранённых измерений
            StringBuilder designationBuffer,                   // буфер текущего обозначения
            StringBuilder valueBuffer,                         // буфер текущего значения
            StringBuilder unitBuffer,                          // буфер текущих единиц
            StringBuilder operationBuffer,                     // буфер операции (если есть)
            StringBuilder valueOperationBuffer,                // буфер операции над значением
            String displayDesignation,                         // отображаемое обозначение
            Boolean designationUsesStix,                       // использовать ли STIX для обозначения
            InputModule designationSubscriptModule,            // модуль подстрочного индекса
            InputController.FocusState focusState,             // состояние фокуса ввода
            String currentInputField                           // текущее поле ввода
    ) {
        SpannableStringBuilder designationsText = new SpannableStringBuilder();

        // отображение сохранённых измерений с форматированием
        for (int i = 0; i < measurements.size(); i++) {
            ConcreteMeasurementEntity measurement = measurements.get(i);
            String baseDesignation = measurement.getBaseDesignation(); // базовое обозначение (например, "E")
            String subscript = measurement.getSubscript();             // подстрочный индекс (например, "p")
            boolean usesStix = measurement.isUsesStix();               // использовать ли STIX
            String displayText = measurement.getOriginalDisplay();     // полный текст измерения

            // форматируем обозначение
            int start = designationsText.length();
            designationsText.append(getDisplayTextFromLogicalId(baseDesignation));
            int baseEnd = designationsText.length();

            // добавляем подстрочный индекс, если он есть
            if (!subscript.isEmpty()) {
                int subscriptStart = designationsText.length();
                designationsText.append(subscript);
                int subscriptEnd = designationsText.length();
                designationsText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                designationsText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (baseDesignation.equals("E_latin_p") || baseDesignation.equals("E_latin_k")) {
                // специальные случаи для E_p и E_k
                int subscriptStart = designationsText.length();
                designationsText.append(baseDesignation.endsWith("_p") ? "p" : "k");
                int subscriptEnd = designationsText.length();
                designationsText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                designationsText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // применяем шрифт STIX, если нужно
            if (usesStix && stixTypeface != null) {
                designationsText.setSpan(new CustomTypefaceSpan(stixTypeface), start, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // добавляем значение и единицы из originalDisplay
            int equalIndex = displayText.indexOf(" = ");
            if (equalIndex != -1) {
                String valuePart = displayText.substring(equalIndex);
                designationsText.append(valuePart);
            }

            // добавляем перенос строки между измерениями
            if (i < measurements.size() - 1) {
                designationsText.append("\n\n");
            }
        }
        if (!measurements.isEmpty()) {
            designationsText.append("\n\n");
        }

        // отображение текущего ввода
        if (designationBuffer.length() > 0 || valueBuffer.length() > 0 || unitBuffer.length() > 0 || designationSubscriptModule != null) {
            int start = designationsText.length();
            if (operationBuffer.length() > 0) {
                designationsText.append(operationBuffer).append("(").append(displayDesignation).append(")");
            } else {
                designationsText.append(displayDesignation);
            }
            int end = designationsText.length();

            // добавляем подстрочный индекс для текущего ввода
            if (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) {
                String subscriptText = designationSubscriptModule.getDisplayText().toString();
                int subscriptStart = designationsText.length();
                designationsText.append(subscriptText);
                int subscriptEnd = designationsText.length();
                designationsText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                designationsText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // применяем STIX для текущего ввода
            if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                designationsText.setSpan(new CustomTypefaceSpan(stixTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // добавляем значение и единицы
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

            // применяем стили для активного поля ввода
            if (focusState == InputController.FocusState.MODULE && designationSubscriptModule != null) {
                int modStart = end;
                int modEnd = modStart + designationSubscriptModule.getDisplayText().length();
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), modStart, modEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (focusState == InputController.FocusState.DESIGNATION) {
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (focusState == InputController.FocusState.VALUE && valueStart < valueEnd) {
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), valueStart, valueEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (focusState == InputController.FocusState.UNIT && unitPos < designationsText.length()) {
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), unitPos + 1, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            // placeholder, если ничего не введено
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
            List<UnknownQuantityEntity> unknowns,       // список неизвестных величин
            String unknownDisplayDesignation,           // отображаемое обозначение неизвестного
            Boolean unknownUsesStix,                    // использовать ли STIX
            InputModule unknownSubscriptModule,         // модуль подстрочного индекса
            String currentInputField,                   // текущее поле ввода
            boolean isConversionMode                    // режим перевода в СИ
    ) {
        SpannableStringBuilder unknownText = new SpannableStringBuilder();

        if (isConversionMode) {
            int start = unknownText.length();
            unknownText.append("Введите неизвестное");
            int color = "unknown".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            unknownText.setSpan(new ForegroundColorSpan(color), start, unknownText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            if (unknownDisplayDesignation != null) {
                int start = unknownText.length();
                unknownText.append(unknownDisplayDesignation);
                int end = unknownText.length();
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
                String displayText = lastUnknown.getDisplayText();
                String designationPart = extractDesignation(displayText);
                SpannableStringBuilder formattedDesignation = formatDesignation(designationPart, lastUnknown.isUsesStix());
                int equalIndex = displayText.indexOf(" = ");
                if (equalIndex != -1) {
                    String valuePart = displayText.substring(equalIndex);
                    unknownText.append(formattedDesignation).append(valuePart);
                } else {
                    unknownText.append(formattedDesignation);
                }
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

    // извлечение обозначения из строки
    private String extractDesignation(String displayText) {
        int equalIndex = displayText.indexOf(" = ");
        if (equalIndex != -1) {
            return displayText.substring(0, equalIndex).trim();
        }
        return displayText.trim();
    }

    // форматирование обозначения с подстрочным индексом
    public SpannableStringBuilder formatDesignation(String designation, boolean usesStix) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (designation.contains("_")) {
            String[] parts = designation.split("_", 2);
            builder.append(parts[0]);
            int start = builder.length();
            builder.append(parts[1]);
            int end = builder.length();
            builder.setSpan(new SubscriptSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(0.75f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            builder.append(designation);
        }
        if (usesStix && stixTypeface != null) {
            builder.setSpan(new CustomTypefaceSpan(stixTypeface), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    // получение отображаемого текста из логического идентификатора
    public String getDisplayTextFromLogicalId(String logicalId) {
        if (logicalId == null) return "";
        String displayText = logicalId.replace("designation_", "")
                .replace("_latin", "")
                .replace("_power", "")
                .replace("_p", "") // убираем _p для отображения только E
                .replace("_k", ""); // убираем _k для отображения только E
        LogUtils.d("DisplayManager", "получен отображаемый текст: " + displayText + " из " + logicalId);
        return displayText;
    }

    // получение выражения для отображения
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

    // извлечение переменных из строки
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

    // получение шрифта STIX
    public Typeface getStixTypeface() {
        return stixTypeface;
    }
}