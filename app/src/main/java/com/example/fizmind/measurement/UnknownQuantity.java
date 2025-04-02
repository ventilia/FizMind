package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.graphics.Typeface;
import android.util.Log;

import com.example.fizmind.animation.CustomTypefaceSpan;

public class UnknownQuantity {
    private final String displayDesignation; // отображаемый текст
    private final String subscript; // индекс
    private final boolean usesStix; // флаг шрифта STIX

    // конструктор
    public UnknownQuantity(String displayDesignation, String subscript, boolean usesStix) {
        this.displayDesignation = displayDesignation;
        this.subscript = subscript;
        this.usesStix = usesStix;
        Log.d("UnknownQuantity", "Создано неизвестное: " + displayDesignation + (subscript != null ? "_" + subscript : ""));
    }

    public UnknownQuantity(String displayDesignation, boolean usesStix) {
        this(displayDesignation, "", usesStix);
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

    public SpannableStringBuilder getDisplayText(Typeface stixTypeface) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int start = sb.length();
        sb.append(displayDesignation);
        int end = sb.length();
        if (usesStix && stixTypeface != null) {
            sb.setSpan(new CustomTypefaceSpan(stixTypeface), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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