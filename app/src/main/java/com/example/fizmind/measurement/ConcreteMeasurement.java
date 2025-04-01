package com.example.fizmind.measurement;

import android.util.Log;

/**
 * Класс, представляющий конкретное измерение с переведенными в СИ значениями.
 * Наследуется от абстрактного класса Measurement.
 */
public class ConcreteMeasurement extends Measurement {

    // Дополнительные поля для отображения степени и индекса
    private final String exponent;
    private final String subscript;
    // Флаг, указывающий, является ли измерение константой
    private final boolean constant;

    /**
     * Конструктор ConcreteMeasurement.
     *
     * @param designation           Обозначение измерения.
     * @param value                 Значение измерения.
     * @param unit                  Единица измерения.
     * @param designationOperations Операции над обозначением.
     * @param valueOperations       Операции над значением.
     * @param exponent              Степень (если есть).
     * @param subscript             Индекс (если есть).
     * @param constant              Флаг, указывающий, является ли измерение константой.
     */
    public ConcreteMeasurement(String designation, double value, String unit,
                               String designationOperations, String valueOperations,
                               String exponent, String subscript, boolean constant) {
        super(designation, value, unit, designationOperations, valueOperations);
        this.exponent = exponent;
        this.subscript = subscript;
        this.constant = constant;
    }

    /**
     * Проверяет корректность измерения.
     *
     * @return true, если значение корректно и единица измерения задана, иначе false.
     */
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

    /**
     * Возвращает строковое представление измерения.
     *
     * @return Строка в формате "designation[subscript][^exponent] = value unit".
     */
    @Override
    public String toString() {
        return String.format("%s%s%s = %.2f %s",
                designation,
                (subscript == null || subscript.isEmpty()) ? "" : "_" + subscript,
                (exponent == null || exponent.isEmpty()) ? "" : "^" + exponent,
                value,
                unit);
    }

    /**
     * Возвращает флаг, указывающий, является ли измерение константой.
     *
     * @return true, если измерение константное; false иначе.
     */
    public boolean isConstant() {
        return constant;
    }

    /**
     * Возвращает индекс (subscript) измерения.
     *
     * @return строку с индексом, либо пустую строку, если индекс не задан.
     */
    public String getSubscript() {
        return subscript != null ? subscript : "";
    }
}
