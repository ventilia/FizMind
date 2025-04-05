package com.example.fizmind.modules;

import android.util.Log;
import com.example.fizmind.measurement.ConcreteMeasurement;
import java.util.List;

/**
 * Утилитный класс для валидации модулей и уникальности индексов в измерениях.
 */
public class ModuleValidator {

    /**
     * проверяет, можно ли добавить модуль указанного типа
     * @param type тип модуля (SUBSCRIPT)
     * @param existingSubscriptModule существующий модуль индекса (может быть null)
     * @return true, если модуль можно добавить, false — если модуль того же типа уже существует
     */
    public static boolean canAddModule(ModuleType type, InputModule existingSubscriptModule) {
        if (type == null) {
            Log.w("ModuleValidator", "тип модуля не может быть null");
            return false;
        }
        if (type == ModuleType.SUBSCRIPT && existingSubscriptModule != null) {
            Log.w("ModuleValidator", "индекс уже существует");
            return false;
        }
        return true;
    }

    /**
     * проверяет, можно ли применить модуль к значению
     * @param valueBuffer буфер значения
     * @param valueOperationBuffer буфер операций над значением
     * @return true, если значение не пустое, false — если пустое
     */
    public static boolean canApplyModuleToValue(StringBuilder valueBuffer, StringBuilder valueOperationBuffer) {
        if (valueBuffer == null || valueOperationBuffer == null) {
            Log.w("ModuleValidator", "буферы значений не могут быть null");
            return false;
        }
        if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
            Log.w("ModuleValidator", "нельзя применить модуль без значения");
            return false;
        }
        return true;
    }

    /**
     * проверяет, можно ли начать ввод с модуля
     * @param designationBuffer буфер обозначения
     * @return true, если обозначение не пустое, false — если пустое или null
     */
    public static boolean canStartWithModule(StringBuilder designationBuffer) {
        if (designationBuffer == null || designationBuffer.length() == 0) {
            Log.w("ModuleValidator", "нельзя начинать ввод с модуля");
            return false;
        }
        return true;
    }

    /**
     * проверяет, уникален ли индекс для данного обозначения среди всех измерений
     * @param designation логический идентификатор обозначения
     * @param subscript индекс для проверки
     * @param measurements список сохранённых измерений типа ConcreteMeasurement
     * @return true, если индекс уникален или пустой, false — если уже используется
     */
    public static boolean isSubscriptUnique(String designation, String subscript, List<ConcreteMeasurement> measurements) {
        if (designation == null) {
            Log.w("ModuleValidator", "обозначение не может быть null");
            return false;
        }
        if (subscript == null || subscript.isEmpty()) {
            return true; // пустой индекс считается уникальным
        }
        if (measurements == null) {
            Log.w("ModuleValidator", "список измерений не может быть null");
            return true; // если список null, считаем индекс уникальным
        }
        for (ConcreteMeasurement cm : measurements) {
            if (cm == null) {
                continue; // пропускаем null элементы
            }
            if (cm.getDesignation().equals(designation) && subscript.equals(cm.getSubscript())) {
                Log.w("ModuleValidator", "индекс '" + subscript + "' уже используется для обозначения: " + designation);
                return false;
            }
        }
        return true;
    }
}