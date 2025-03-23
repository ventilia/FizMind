package com.example.fizmind.modules;

import android.util.Log;

import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.Measurement;

import java.util.List;

/**
 * Класс для валидации модулей (степени и индекса).
 */
public class ModuleValidator {

    /**
     * Проверяет, можно ли добавить модуль указанного типа.
     * @param type Тип модуля (EXPONENT или SUBSCRIPT)
     * @param existingExponentModule Существующий модуль степени (может быть null)
     * @param existingSubscriptModule Существующий модуль индекса (может быть null)
     * @return true, если можно добавить модуль, false — если модуль того же типа уже существует
     */
    public static boolean canAddModule(ModuleType type, InputModule existingExponentModule, InputModule existingSubscriptModule) {
        if (type == ModuleType.EXPONENT && existingExponentModule != null) {
            Log.w("ModuleValidator", "Степень уже существует");
            return false;
        }
        if (type == ModuleType.SUBSCRIPT && existingSubscriptModule != null) {
            Log.w("ModuleValidator", "Индекс уже существует");
            return false;
        }
        return true;
    }

    /**
     * Проверяет, можно ли применить модуль к значению.
     * @param valueBuffer Буфер значения
     * @param valueOperationBuffer Буфер операций над значением
     * @return true, если значение не пустое, false — если пустое
     */
    public static boolean canApplyModuleToValue(StringBuilder valueBuffer, StringBuilder valueOperationBuffer) {
        if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
            Log.w("ModuleValidator", "Нельзя применить модуль без значения");
            return false;
        }
        return true;
    }

    /**
     * Проверяет, можно ли начать ввод с модуля.
     * @param designationBuffer Буфер обозначения
     * @return true, если обозначение не пустое, false — если пустое
     */
    public static boolean canStartWithModule(StringBuilder designationBuffer) {
        if (designationBuffer.length() == 0) {
            Log.w("ModuleValidator", "Нельзя начинать ввод с модуля");
            return false;
        }
        return true;
    }

    /**
     * Проверяет, уникален ли индекс для данного обозначения среди всех измерений.
     * @param designation Логический идентификатор обозначения
     * @param subscript Индекс для проверки
     * @param measurements Список сохраненных измерений
     * @return true, если индекс уникален, false — если уже используется
     */
    public static boolean isSubscriptUnique(String designation, String subscript, List<Measurement> measurements) {
        if (subscript == null || subscript.isEmpty()) {
            return true; // Пустой индекс считается уникальным
        }
        for (Measurement m : measurements) {
            if (m instanceof ConcreteMeasurement) {
                ConcreteMeasurement cm = (ConcreteMeasurement) m;
                if (cm.getDesignation().equals(designation) && subscript.equals(cm.getSubscript())) {
                    Log.w("ModuleValidator", "Индекс '" + subscript + "' уже используется для обозначения: " + designation);
                    return false;
                }
            }
        }
        return true;
    }
}