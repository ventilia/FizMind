package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;
import com.example.fizmind.utils.LogUtils;


public class UnknownQuantity {
    private final String displayDesignation;
    private final String logicalDesignation;
    private final String subscript;
    private final boolean usesStix;
    private final SpannableStringBuilder displayText;


    public UnknownQuantity(String displayDesignation, String logicalDesignation, String subscript, boolean usesStix, SpannableStringBuilder displayText) {
        this.displayDesignation = displayDesignation;
        this.logicalDesignation = logicalDesignation;
        this.subscript = subscript != null ? subscript : "";
        this.usesStix = usesStix;
        this.displayText = displayText;
        LogUtils.logUnknownCreated("UnknownQuantity", displayDesignation, subscript);
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

    public boolean usesStix() {
        return usesStix;
    }


    public boolean validate() {
        if (displayDesignation == null || displayDesignation.isEmpty()) {
            LogUtils.e("UnknownQuantity", "пустое обозначение для неизвестного");
            return false;
        }
        return true;
    }


    public SpannableStringBuilder getDisplayText() {
        return displayText;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(displayDesignation);
        if (!subscript.isEmpty()) {
            sb.append("_").append(subscript);
        }
        sb.append(" = ?");
        return sb.toString();
    }
}