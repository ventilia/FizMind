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

import java.util.List;

// менеджер отображения текста
public class DisplayManager {

    private final Typeface stixTypeface;

    public DisplayManager(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    // построение текста для измерений
    public SpannableStringBuilder buildDesignationsText(
            List<ConcreteMeasurement> measurements, StringBuilder designationBuffer,
            StringBuilder valueBuffer, StringBuilder unitBuffer, StringBuilder operationBuffer,
            StringBuilder valueOperationBuffer, String displayDesignation, Boolean designationUsesStix,
            InputModule designationSubscriptModule, InputController.FocusState focusState, String currentInputField) {

        SpannableStringBuilder designationsText = new SpannableStringBuilder();

        // отображение сохраненных измерений
        for (ConcreteMeasurement measurement : measurements) {
            designationsText.append(measurement.getOriginalDisplay()).append("\n\n");
        }

        // отображение текущего ввода
        if ("designations".equals(currentInputField) && designationBuffer.length() > 0) {
            int start = designationsText.length();
            designationsText.append(designationBuffer.toString());
            int end = designationsText.length();

            if (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) {
                String subscriptText = designationSubscriptModule.getContent();
                int subscriptStart = designationsText.length();
                designationsText.append(subscriptText);
                int subscriptEnd = designationsText.length();
                designationsText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                designationsText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (Boolean.TRUE.equals(designationUsesStix) && stixTypeface != null) {
                designationsText.setSpan(new CustomTypefaceSpan(stixTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            designationsText.append(" = ");
            int valueStart = designationsText.length();
            designationsText.append(valueBuffer.toString());
            int valueEnd = designationsText.length();
            designationsText.append(" ").append(unitBuffer.toString());

            // подсветка фокуса
            if (focusState == InputController.FocusState.DESIGNATION) {
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (focusState == InputController.FocusState.VALUE) {
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), valueStart, valueEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (focusState == InputController.FocusState.UNIT) {
                int unitStart = valueEnd + 1;
                designationsText.setSpan(new StyleSpan(Typeface.BOLD), unitStart, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return designationsText;
    }

    // построение текста для неизвестных величин
    public SpannableStringBuilder buildUnknownText(
            List<UnknownQuantity> unknowns, String unknownDisplayDesignation, Boolean unknownUsesStix,
            InputModule unknownSubscriptModule, String currentInputField) {

        SpannableStringBuilder unknownText = new SpannableStringBuilder();

        if ("unknown".equals(currentInputField) && unknownDisplayDesignation != null) {
            int start = unknownText.length();
            unknownText.append(unknownDisplayDesignation);
            int end = unknownText.length();

            if (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty()) {
                String subscriptText = unknownSubscriptModule.getContent();
                int subscriptStart = unknownText.length();
                unknownText.append(subscriptText);
                int subscriptEnd = unknownText.length();
                unknownText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                unknownText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (Boolean.TRUE.equals(unknownUsesStix) && stixTypeface != null) {
                unknownText.setSpan(new CustomTypefaceSpan(stixTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            unknownText.append(" = ?");
        } else if (!unknowns.isEmpty()) {
            unknownText.append(unknowns.get(unknowns.size() - 1).getDisplayText());
        } else {
            int start = unknownText.length();
            unknownText.append("Неизвестная величина");
            unknownText.setSpan(new ForegroundColorSpan(Color.GRAY), start, unknownText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return unknownText;
    }

    // получение отображаемого текста из логического идентификатора (заглушка, требует реализации)
    public String getDisplayTextFromLogicalId(String logicalId) {
        return logicalId; // заменить на реальную логику маппинга
    }

    // получение выражения формулы (заглушка, требует реализации)
    public String getDisplayExpression(Formula formula, String variable) {
        return formula.getBaseExpression(); // заменить на реальную логику форматирования
    }
}