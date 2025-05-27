package com.example.fizmind.modules;

import com.example.fizmind.measurement.ConcreteMeasurement;

import java.util.List;

public class ModuleValidator {

    public static boolean canAddModule(ModuleType type, InputModule currentModule, String logicalDesignation) {
        if (currentModule != null && !currentModule.isEmpty()) {
            return false;
        }
        if (type == ModuleType.SUBSCRIPT_P || type == ModuleType.SUBSCRIPT_K) {
            return "E_latin".equals(logicalDesignation);
        }
        return true;
    }

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


    public static boolean isModuleFixed(ModuleType type) {
        return type == ModuleType.SUBSCRIPT_P || type == ModuleType.SUBSCRIPT_K;
    }


    public static boolean canApplyInput(ModuleType type, String currentContent, String input) {
        if (isModuleFixed(type)) {

            if (currentContent.isEmpty() && (input.equals("p") || input.equals("k"))) {
                return true;
            }
            return false;
        } else if (type == ModuleType.SUBSCRIPT) {

            return input.matches("[0-9]");
        }
        return false;
    }
}