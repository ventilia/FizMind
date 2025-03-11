package com.example.fizmind.measurement;

import android.util.Log;

/**
 * Класс для представления неизвестной физической величины.
 * Содержит только обозначение и отображается как "обозначение = ?".
 */
public class UnknownQuantity {
    private final String designation; // Логический идентификатор обозначения

    /**
     * Конструктор класса.
     * @param designation Логический идентификатор обозначения (например, "m_latin")
     */
    public UnknownQuantity(String designation) {
        this.designation = designation;
        Log.d("UnknownQuantity", "Создано неизвестное: " + designation);
    }

    /**
     * Получение обозначения.
     * @return Логический идентификатор обозначения
     */
    public String getDesignation() {
        return designation;
    }

    /**
     * Проверка валидности неизвестной величины.
     * @return true, если обозначение не пустое, иначе false
     */
    public boolean validate() {
        if (designation == null || designation.isEmpty()) {
            Log.e("UnknownQuantity", "Пустое обозначение для неизвестного");
            return false;
        }
        return true;
    }

    /**
     * Строковое представление неизвестной величины.
     * @return Строка вида "обозначение = ?"
     */
    @Override
    public String toString() {
        return designation + " = ?";
    }
}