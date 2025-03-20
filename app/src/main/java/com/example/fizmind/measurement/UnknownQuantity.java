package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.util.Log;

public class UnknownQuantity {
    private final String displayDesignation; // Отображаемый текст обозначения (например, "F")
    private String subscript; // Индекс для обозначения

    public UnknownQuantity(String displayDesignation, String subscript) {
        this.displayDesignation = displayDesignation;
        this.subscript = subscript;
        Log.d("UnknownQuantity", "Создано неизвестное: " + displayDesignation + (subscript != null ? "_" + subscript : ""));
    }

    public UnknownQuantity(String displayDesignation) {
        this(displayDesignation, "");
    }

    public String getDisplayDesignation() {
        return displayDesignation;
    }

    public String getSubscript() {
        return subscript;
    }

    public boolean validate() {
        if (displayDesignation == null || displayDesignation.isEmpty()) {
            Log.e("UnknownQuantity", "Пустое обозначение для неизвестного");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(displayDesignation);
        if (subscript != null && !subscript.isEmpty()) {
            sb.append("_").append(subscript);
        }
        sb.append(" = ?");
        return sb.toString();
    }

    public SpannableStringBuilder getDisplayText() {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(displayDesignation);
        if (subscript != null && !subscript.isEmpty()) {
            int subscriptStart = sb.length();
            sb.append(subscript);
            int subscriptEnd = sb.length();
            sb.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        sb.append(" = ?");
        return sb;
    }
}