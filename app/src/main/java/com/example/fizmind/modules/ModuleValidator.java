package com.example.fizmind.modules;

import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.utils.LogUtils;
import java.util.List;

/**
 * утилитный класс для валидации модулей и уникальности индексов в измерениях.
 */
public class ModuleValidator {

    public static boolean canAddModule(ModuleType type, InputModule existingSubscriptModule) {
        if (type == null) {
            LogUtils.w("ModuleValidator", "тип модуля не может быть null");
            return false;
        }
        if (type == ModuleType.SUBSCRIPT && existingSubscriptModule != null) {
            LogUtils.w("ModuleValidator", "индекс уже существует");
            return false;
        }
        return true;
    }

    public static boolean canApplyModuleToValue(StringBuilder valueBuffer, StringBuilder valueOperationBuffer) {
        if (valueBuffer == null || valueOperationBuffer == null) {
            LogUtils.w("ModuleValidator", "буферы значений не могут быть null");
            return false;
        }
        if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
            LogUtils.w("ModuleValidator", "нельзя применить модуль без значения");
            return false;
        }
        return true;
    }

    public static boolean canStartWithModule(StringBuilder designationBuffer) {
        if (designationBuffer == null || designationBuffer.length() == 0) {
            LogUtils.w("ModuleValidator", "нельзя начинать ввод с модуля");
            return false;
        }
        return true;
    }

    public static boolean isSubscriptUnique(String designation, String subscript, List<ConcreteMeasurement> measurements) {
        if (designation == null) {
            LogUtils.w("ModuleValidator", "обозначение не может быть null");
            return false;
        }
        if (subscript == null || subscript.isEmpty()) {
            return true;
        }
        if (measurements == null) {
            LogUtils.w("ModuleValidator", "список измерений не может быть null");
            return true;
        }
        for (ConcreteMeasurement cm : measurements) {
            if (cm == null) {
                continue;
            }
            if (cm.getDesignation().equals(designation) && subscript.equals(cm.getSubscript())) {
                LogUtils.w("ModuleValidator", "индекс '" + subscript + "' уже используется для обозначения: " + designation);
                return false;
            }
        }
        return true;
    }
}