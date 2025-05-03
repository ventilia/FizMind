package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;

// класс для представления неизвестной величины
public class UnknownQuantity {
    private final String displayDesignation;
    private final String logicalDesignation;
    private final String subscript;
    private final boolean usesStix;
    private final SpannableStringBuilder displayText;

    // конструктор
    public UnknownQuantity(
            String displayDesignation, String logicalDesignation,
            String subscript, boolean usesStix, SpannableStringBuilder displayText) {
        this.displayDesignation = displayDesignation;
        this.logicalDesignation = logicalDesignation;
        this.subscript = subscript;
        this.usesStix = usesStix;
        this.displayText = displayText;
    }

    // геттеры
    public String getDisplayDesignation() { return displayDesignation; }
    public String getLogicalDesignation() { return logicalDesignation; }
    public String getSubscript() { return subscript; }
    public boolean isUsesStix() { return usesStix; }
    public String getDisplayText() { return displayText.toString(); }
}