package com.example.fizmind.modules;

/**
 * Типы модулей, которые могут быть применены к числу или обозначению.
 */
public enum ModuleType {
    EXPONENT("степень", "^", true),  // Применимо к числу
    SUBSCRIPT("нижний индекс", "_", false);  // Применимо к обозначению

    private final String description;
    private final String symbol;
    private final boolean appliesToValue;  // true, если модуль для чисел, false — для обозначений

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