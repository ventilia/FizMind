package com.example.fizmind.modules;

import com.example.fizmind.measurement.ConcreteMeasurement;

import java.util.List;

// валидатор модулей
public class ModuleValidator {
    // проверка возможности добавления модуля
    public static boolean canAddModule(ModuleType type, InputModule currentModule, String logicalDesignation) {
        if (currentModule != null && !currentModule.isEmpty()) {
            return false;
        }
        if (type == ModuleType.SUBSCRIPT_P || type == ModuleType.SUBSCRIPT_K) {
            return "E_latin".equals(logicalDesignation);
        }
        return true;
    }

    // проверка уникальности индекса
    public static boolean isSubscriptUnique(String baseDesignation, String subscript, List<ConcreteMeasurement> measurements) {
        if (subscript.isEmpty()) {
            return true;
        }
        for (ConcreteMeasurement m : measurements) {
            if (m.getBaseDesignation().equals(baseDesignation) && m.getSubscript().equals(subscript)) {
                return false;
            }
        }
        return true;
    }
}