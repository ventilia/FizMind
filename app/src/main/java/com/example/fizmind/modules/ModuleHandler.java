package com.example.fizmind.modules;

import android.util.Log;

import com.example.fizmind.PhysicalQuantityRegistry;

/**
 * Обработчик модулей, реализующий общую логику для всех типов модулей.
 */
public class ModuleHandler implements Module {
    /**
     * Типы модулей, поддерживаемые системой.
     */
    public enum ModuleType {
        EXPONENT("^", true),  // Степень, применяется к числу
        SUBSCRIPT("_", false); // Нижний индекс, применяется к обозначению

        private final String symbol;
        private final boolean appliesToValue;

        ModuleType(String symbol, boolean appliesToValue) {
            this.symbol = symbol;
            this.appliesToValue = appliesToValue;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean appliesToValue() {
            return appliesToValue;
        }
    }

    private final ModuleType type;
    private final String argument; // Аргумент модуля (например, "2" для степени)

    public ModuleHandler(ModuleType type, String argument) {
        this.type = type;
        this.argument = argument;
    }

    @Override
    public String apply(String base) {
        if (argument == null || argument.isEmpty()) {
            return base + type.getSymbol(); // Отображение в процессе ввода
        }
        // Финальное отображение будет в updateDisplay с использованием Spannable
        return base + type.getSymbol() + argument;
    }

    @Override
    public boolean validate(String base) {
        if (base == null || base.isEmpty()) {
            Log.w("ModuleHandler", "Базовое значение не может быть пустым для модуля " + type.getSymbol());
            return false;
        }
        if (type.appliesToValue()) {
            try {
                Double.parseDouble(base);
                return true;
            } catch (NumberFormatException e) {
                Log.w("ModuleHandler", "Степень применима только к числам: " + base);
                return false;
            }
        } else {
            // Проверка, что base - это допустимое обозначение
            return PhysicalQuantityRegistry.getPhysicalQuantity(base) != null;
        }
    }

    @Override
    public String getSymbol() {
        return type.getSymbol();
    }

    @Override
    public boolean appliesToValue() {
        return type.appliesToValue();
    }

    public ModuleType getType() {
        return type;
    }

    public String getArgument() {
        return argument;
    }
}