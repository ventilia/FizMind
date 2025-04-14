package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.graphics.Typeface;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.utils.LogUtils;

public class UnknownQuantity {
    private final String displayDesignation;  // Отображаемое обозначение (например, "E")
    private final String logicalDesignation;  // Логическое обозначение (например, "E_latin")
    private final String subscript;           // Индекс (если есть)
    private final boolean usesStix;           // Использование шрифта STIX

    public UnknownQuantity(String displayDesignation, String logicalDesignation, String subscript, boolean usesStix) {
        this.displayDesignation = displayDesignation;
        this.logicalDesignation = logicalDesignation;
        this.subscript = subscript;
        this.usesStix = usesStix;
        LogUtils.logUnknownCreated("UnknownQuantity", displayDesignation, subscript);
    }

    public UnknownQuantity(String displayDesignation, String logicalDesignation, boolean usesStix) {
        this(displayDesignation, logicalDesignation, "", usesStix);
    }

    public String getDisplayDesignation() {
        return displayDesignation;
    }

    public String getLogicalDesignation() {
        return logicalDesignation;
    }

    public String getSubscript() {
        return subscript;
    }

    public boolean validate() {
        if (displayDesignation == null || displayDesignation.isEmpty()) {
            LogUtils.e("UnknownQuantity", "пустое обозначение для неизвестного");
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