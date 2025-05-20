package com.example.fizmind.modules;

import com.example.fizmind.measurement.ConcreteMeasurement;

import java.util.List;

public class ModuleValidator {
    // проверка, можно ли добавить модуль к обозначению
    public static boolean canAddModule(ModuleType type, InputModule currentModule, String logicalDesignation) {
        if (currentModule != null && !currentModule.isEmpty()) {
            return false; // уже есть модуль
        }
        if (type == ModuleType.SUBSCRIPT_P || type == ModuleType.SUBSCRIPT_K) {
            return "E_latin".equals(logicalDesignation); // "p" и "k" только для "E"
        }
        return true; // для SUBSCRIPT нет ограничений
    }

    // проверка уникальности индекса среди сохраненных измерений
    public static boolean isSubscriptUnique(String baseDesignation, String subscript, List<ConcreteMeasurement> measurements) {
        if (subscript.isEmpty()) {
            return true;
        }
        for (ConcreteMeasurement m : measurements) {
            if (m.getBaseDesignation().equals(baseDesignation) && m.getSubscript().equals(subscript)) {
                return false; // индекс уже используется
            }
        }
        return true;
    }

    // проверка, является ли модуль фиксированным ("p" или "k")
    public static boolean isModuleFixed(ModuleType type) {
        return type == ModuleType.SUBSCRIPT_P || type == ModuleType.SUBSCRIPT_K;
    }

    // проверка допустимости ввода символа в модуль
    public static boolean canApplyInput(ModuleType type, String currentContent, String input) {
        if (isModuleFixed(type)) {
            // для "p" и "k" разрешаем только первый символ "p" или "k"
            if (currentContent.isEmpty() && (input.equals("p") || input.equals("k"))) {
                return true;
            }
            return false; // дальнейший ввод запрещен
        } else if (type == ModuleType.SUBSCRIPT) {
            // для обычного индекса разрешаем буквы и цифры
            return input.matches("[a-zA-Z0-9]");
        }
        return false;
    }
}