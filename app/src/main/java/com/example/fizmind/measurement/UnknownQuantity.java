package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.util.Log;

public class UnknownQuantity {
    private final String designation; // Логический идентификатор обозначения
    private String subscript; // Индекс для обозначения

    public UnknownQuantity(String designation, String subscript) {
        this.designation = designation;
        this.subscript = subscript;
        Log.d("UnknownQuantity", "Создано неизвестное: " + designation + (subscript != null ? "_" + subscript : ""));
    }

    public UnknownQuantity(String designation) {
        this(designation, "");
    }

    public String getDesignation() {
        return designation;
    }

    public String getSubscript() {
        return subscript;
    }

    public boolean validate() {
        if (designation == null || designation.isEmpty()) {
            Log.e("UnknownQuantity", "Пустое обозначение для неизвестного");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(designation);
        if (subscript != null && !subscript.isEmpty()) {
            sb.append("_").append(subscript);
        }
        sb.append(" = ?");
        return sb.toString();
    }

    /**
     * Returns a SpannableStringBuilder with proper subscript formatting for display.
     */
    public SpannableStringBuilder getDisplayText() {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int start = sb.length();
        sb.append(designation);
        int end = sb.length();
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