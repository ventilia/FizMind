package com.example.fizmind.modules;

import android.util.Log;
import com.example.fizmind.measurement.ConcreteMeasurement;
import java.util.List;

/**
 * Утилитный класс для валидации модулей и уникальности индексов в измерениях.
 */
public class ModuleValidator {

    /**
     * Проверяет, можно ли добавить модуль указанного типа.
     *
     * @param type                   Тип модуля (EXPONENT или SUBSCRIPT).
     * @param existingExponentModule Существующий модуль степени (может быть null).
     * @param existingSubscriptModule Существующий модуль индекса (может быть null).
     * @return true, если модуль можно добавить, false — если модуль того же типа уже существует.
     */
    public static boolean canAddModule(ModuleType type, InputModule existingExponentModule, InputModule existingSubscriptModule) {
        if (type == null) {
            Log.w("ModuleValidator", "Тип модуля не может быть null");
            return false;
        }
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
     *
     * @param valueBuffer         Буфер значения.
     * @param valueOperationBuffer Буфер операций над значением.
     * @return true, если значение не пустое, false — если пустое.
     */
    public static boolean canApplyModuleToValue(StringBuilder valueBuffer, StringBuilder valueOperationBuffer) {
        if (valueBuffer == null || valueOperationBuffer == null) {
            Log.w("ModuleValidator", "Буферы значений не могут быть null");
            return false;
        }
        if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
            Log.w("ModuleValidator", "Нельзя применить модуль без значения");
            return false;
        }
        return true;
    }

    /**
     * Проверяет, можно ли начать ввод с модуля.
     *
     * @param designationBuffer Буфер обозначения.
     * @return true, если обозначение не пустое, false — если пустое или null.
     */
    public static boolean canStartWithModule(StringBuilder designationBuffer) {
        if (designationBuffer == null || designationBuffer.length() == 0) {
            Log.w("ModuleValidator", "Нельзя начинать ввод с модуля");
            return false;
        }
        return true;
    }

    /**
     * Проверяет, уникален ли индекс для данного обозначения среди всех измерений.
     *
     * @param designation Логический идентификатор обозначения.
     * @param subscript   Индекс для проверки.
     * @param measurements Список сохранённых измерений типа ConcreteMeasurement.
     * @return true, если индекс уникален или пустой, false — если уже используется.
     */
    public static boolean isSubscriptUnique(String designation, String subscript, List<ConcreteMeasurement> measurements) {
        if (designation == null) {
            Log.w("ModuleValidator", "Обозначение не может быть null");
            return false;
        }
        if (subscript == null || subscript.isEmpty()) {
            return true; // Пустой индекс считается уникальным
        }
        if (measurements == null) {
            Log.w("ModuleValidator", "Список измерений не может быть null");
            return true; // Если список null, считаем индекс уникальным
        }
        for (ConcreteMeasurement cm : measurements) {
            if (cm == null) {
                continue; // Пропускаем null элементы
            }
            if (cm.getDesignation().equals(designation) && subscript.equals(cm.getSubscript())) {
                Log.w("ModuleValidator", "Индекс '" + subscript + "' уже используется для обозначения: " + designation);
                return false;
            }
        }
        return true;
    }
}