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
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.UnknownQuantity;
import com.example.fizmind.modules.InputModule;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.keyboard.InputController;

import java.util.ArrayList;
import java.util.List;


 //менеджер отображения для преобразования внутренних данных в видимый пользователю формат

public class DisplayManager {

    private final Typeface stixTypeface;

    public DisplayManager(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }


     // для поля "введите обозначение"

    public SpannableStringBuilder buildDesignationsText(
            List<ConcreteMeasurement> measurements,
            List<SpannableStringBuilder> history,
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

        // вывод сохраненных измерений из history
        for (int i = 0; i < history.size(); i++) {
            designationsText.append(history.get(i));
            if (i < history.size() - 1) {
                designationsText.append("\n\n");
            }
        }
        if (!history.isEmpty()) {
            designationsText.append("\n\n");
        }

        // рендер  ввода или плейсхолдера
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

       //подсветка
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
            // плейсзолдер
            int start = designationsText.length();
            designationsText.append("Введите обозначение");
            int color = "designations".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            designationsText.setSpan(new ForegroundColorSpan(color), start, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return designationsText;
    }


     //  для поля "введите неизвестное"

    public SpannableStringBuilder buildUnknownText(
            List<UnknownQuantity> unknowns,
            String unknownDisplayDesignation,
            Boolean unknownUsesStix,
            InputModule unknownSubscriptModule,
            String currentInputField
    ) {
        SpannableStringBuilder unknownText = new SpannableStringBuilder();

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
            unknownText.append(unknowns.get(unknowns.size() - 1).getDisplayText());
        } else {
            int start = unknownText.length();
            unknownText.append("Введите неизвестное");
            int color = "unknown".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            unknownText.setSpan(new ForegroundColorSpan(color), start, unknownText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return unknownText;
    }

    //индекс в получение
    public String getDisplayTextFromLogicalId(String logicalId) {
        if (logicalId == null) return "";
        return logicalId.replace("designation_", "")
                .replace("_latin", "")
                .replace("_power", "");
    }

   //дробь

    public String getDisplayExpression(Formula formula, String targetVariable) {
        String expression = formula.getExpressionFor(targetVariable);
        if (expression.contains("/")) {
            String[] parts = expression.split("=");
            if (parts.length == 2) {
                String left = parts[0].trim();
                String right = parts[1].trim();
                String[] fraction = right.split("/");

                if (fraction.length == 2) {
                    // преобразуем логические идентификаторы в отображаемые символы
                    left = formatExpression(left);
                    fraction[0] = formatExpression(fraction[0]);
                    fraction[1] = formatExpression(fraction[1]);

                    // html
                    return left + " = <sup>" + fraction[0].trim() + "</sup>/<sub>" + fraction[1].trim() + "</sub>";
                }
            }
        }
        return formatExpression(expression);
    }


    public String formatExpression(String expression) {
        for (String variable : getVariablesInLine(expression)) {
            String displayVar = getDisplayTextFromLogicalId(variable);
            expression = expression.replace(variable, displayVar);
        }
        return expression;
    }


     // извлекает переменные из строки выражения

    private List<String> getVariablesInLine(String line) {
        List<String> variables = new ArrayList<>();
        for (String key : PhysicalQuantityRegistry.registry.keySet()) {
            if (line.contains(key)) {
                variables.add(key);
            }
        }
        return variables;
    }
}