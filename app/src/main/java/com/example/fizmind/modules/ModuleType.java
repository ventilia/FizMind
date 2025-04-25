package com.example.fizmind.modules;

public enum ModuleType {
    SUBSCRIPT("нижний индекс", "_", false),
    SUBSCRIPT_P("индекс потенциальной энергии", "p", false),
    SUBSCRIPT_K("индекс кинетической энергии", "k", false);

    private final String description;
    private final String symbol;
    private final boolean appliesToValue;

    ModuleType(String description, String symbol, boolean appliesToValue) {
        this.description = description;
        this.symbol = symbol;
        this.appliesToValue = appliesToValue;
    }

    public String getDescription() {
        return description;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean appliesToValue() {
        return appliesToValue;
    }
}