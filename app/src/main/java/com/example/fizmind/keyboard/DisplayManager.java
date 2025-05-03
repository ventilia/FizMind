package com.example.fizmind.keyboard;

import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;

import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.UnknownQuantity;
import com.example.fizmind.modules.InputModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// менеджер отображения текста
public class DisplayManager {
    private final Typeface stixTypeface;
    private final Map<String, String> logicalToDisplayMap;

    // конструктор
    public DisplayManager(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
        this.logicalToDisplayMap = new HashMap<>();
        initializeDisplayMap();
    }

    // инициализация карты отображения
    private void initializeDisplayMap() {
        logicalToDisplayMap.put("E_latin", "E");
        logicalToDisplayMap.put("E_latin_p", "E_p");
        logicalToDisplayMap.put("E_latin_k", "E_k");
        // добавьте другие обозначения по необходимости
    }

    // получение отображаемого текста по логическому ID
    public String getDisplayTextFromLogicalId(String logicalId) {
        return logicalToDisplayMap.getOrDefault(logicalId, logicalId);
    }

    // построение текста для измерений
    public SpannableStringBuilder buildDesignationsText(
            List<ConcreteMeasurement> measurements, List<SpannableStringBuilder> history,
            StringBuilder designationBuffer, StringBuilder valueBuffer, StringBuilder unitBuffer,
            StringBuilder operationBuffer, StringBuilder valueOperationBuffer, String displayDesignation,
            Boolean designationUsesStix, InputModule designationSubscriptModule, InputController.FocusState focusState,
            String currentInputField) {
        SpannableStringBuilder result = new SpannableStringBuilder();
        for (SpannableStringBuilder entry : history) {
            result.append(entry).append("\n");
        }
        if ("designations".equals(currentInputField) && (!designationBuffer.toString().isEmpty() || !valueBuffer.toString().isEmpty() || !unitBuffer.toString().isEmpty())) {
            if (!operationBuffer.toString().isEmpty()) {
                result.append(operationBuffer).append("(").append(displayDesignation != null ? displayDesignation : "");
            } else {
                result.append(displayDesignation != null ? displayDesignation : "");
            }
            if (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) {
                result.append(designationSubscriptModule.getDisplayText());
            }
            if (!valueBuffer.toString().isEmpty() || !valueOperationBuffer.toString().isEmpty()) {
                result.append(" = ").append(valueOperationBuffer).append(valueBuffer);
            }
            if (!unitBuffer.toString().isEmpty()) {
                result.append(" ").append(unitBuffer);
            }
        }
        return result;
    }

    // построение текста для неизвестных
    public SpannableStringBuilder buildUnknownText(
            List<UnknownQuantity> unknowns, String unknownDisplayDesignation, Boolean unknownUsesStix,
            InputModule unknownSubscriptModule, String currentInputField) {
        SpannableStringBuilder result = new SpannableStringBuilder();
        for (UnknownQuantity unknown : unknowns) {
            result.append(Html.fromHtml(unknown.getDisplayText())).append("\n");
        }
        if ("unknown".equals(currentInputField) && unknownDisplayDesignation != null) {
            result.append(unknownDisplayDesignation);
            if (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty()) {
                result.append(unknownSubscriptModule.getDisplayText());
            }
        }
        return result;
    }
}