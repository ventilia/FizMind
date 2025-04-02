package com.example.fizmind.keyboard;

public class SymbolKey {
    private final String logicalId;
    private final String displayText;
    private final boolean useStixFont;
    private final boolean color;

    // поля
    public SymbolKey(String logicalId, String displayText, boolean useStixFont, boolean color) {
        this.logicalId = logicalId;
        this.displayText = displayText;
        this.useStixFont = useStixFont;
        this.color = color;
    }

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

    public boolean isColor() {
        return color;
    }
}