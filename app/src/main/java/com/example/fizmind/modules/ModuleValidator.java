package com.example.fizmind.modules;

import android.util.Log;

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
}