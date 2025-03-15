package com.example.fizmind.keyboard;


public class SymbolKey {
    private final String logicalId;
    private final String displayText;
    private final boolean useStixFont;
    private final boolean explanation;

    //  конструктор
    public SymbolKey(String logicalId, String displayText, boolean useStixFont, boolean explanation) {
        this.logicalId = logicalId;
        this.displayText = displayText;
        this.useStixFont = useStixFont;
        this.explanation = explanation;
    }

    // по умолчанию showRedCircle = false. сделать потом
    public SymbolKey(String logicalId, String displayText, boolean useStixFont) {
        this(logicalId, displayText, useStixFont, false);
    }

    public String getLogicalId() {
        return logicalId;
    }

    public String getDisplayText() {
        return displayText;
    }

    public boolean shouldUseStixFont() {
        return useStixFont;
    }

    public boolean shouldShowRedCircle() {
        return explanation;
    }
}
