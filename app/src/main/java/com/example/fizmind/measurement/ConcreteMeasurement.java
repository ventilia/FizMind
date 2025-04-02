package com.example.fizmind.measurement;

import android.util.Log;

// класс для измерений с переведенными в СИ значениями
public class ConcreteMeasurement extends Measurement {

    // поля для степени и индекса
    private final String exponent;
    private final String subscript;
    // флаг константы
    private final boolean constant;

    // конструктор
    public ConcreteMeasurement(String designation, double value, String unit,
                               String designationOperations, String valueOperations,
                               String exponent, String subscript, boolean constant) {
        super(designation, value, unit, designationOperations, valueOperations);
        this.exponent = exponent;
        this.subscript = subscript;
        this.constant = constant;
    }

    // проверяет корректность измерения
    @Override
    public boolean validate() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            Log.e("ConcreteMeasurement", "Недопустимое значение: " + value);
            return false;
        }
        if (unit == null || unit.isEmpty()) {
            Log.e("ConcreteMeasurement", "Единица измерения не указана для " + designation);
            return false;
        }
        return true;
    }

    // строковое представление
    @Override
    public String toString() {
        return String.format("%s%s%s = %.2f %s",
                designation,
                (subscript == null || subscript.isEmpty()) ? "" : "_" + subscript,
                (exponent == null || exponent.isEmpty()) ? "" : "^" + exponent,
                value,
                unit);
    }

    // является ли константой
    public boolean isConstant() {
        return constant;
    }

    // возвращает индекс
    public String getSubscript() {
        return subscript != null ? subscript : "";
    }
}