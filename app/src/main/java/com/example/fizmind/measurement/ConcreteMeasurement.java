package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;
import android.util.Log;
import com.example.fizmind.SIConverter;

public class ConcreteMeasurement extends Measurement {

    private final String exponent;              // степень
    private final String subscript;             // индекс
    private final boolean constant;             // флаг константы
    private final SpannableStringBuilder originalDisplay; // оригинальное представление для вывода
    private final double originalValue;         // исходное значение
    private final String originalUnit;          // исходная единица
    private final String conversionSteps;       // шаги конвертации
    private final String moduleSteps;           // шаги применения модуля
    private final boolean isSIUnit;             // флаг, указывающий, была ли единица уже в си
    private final boolean isConversionMode;     // режим: true — си, false — калькулятор

    // конструктор
    public ConcreteMeasurement(String designation, double value, String unit,
                               String designationOperations, String valueOperations,
                               String exponent, String subscript, boolean constant,
                               SpannableStringBuilder originalDisplay,
                               double originalValue, String originalUnit,
                               String conversionSteps, String moduleSteps,
                               boolean isSIUnit, boolean isConversionMode) {
        super(designation, value, unit, designationOperations, valueOperations);
        this.exponent = exponent;
        this.subscript = subscript;
        this.constant = constant;
        this.originalDisplay = originalDisplay;
        this.originalValue = originalValue;
        this.originalUnit = originalUnit;
        this.conversionSteps = conversionSteps != null ? conversionSteps : "";
        this.moduleSteps = moduleSteps != null ? moduleSteps : "";
        this.isSIUnit = isSIUnit;
        this.isConversionMode = isConversionMode;
        Log.d("ConcreteMeasurement", "создано измерение: " + toString());
    }

    // проверка корректности
    @Override
    public boolean validate() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            Log.e("ConcreteMeasurement", "недопустимое значение: " + value);
            return false;
        }
        if (unit == null || unit.isEmpty()) {
            Log.e("ConcreteMeasurement", "единица измерения не указана для " + designation);
            return false;
        }
        return true;
    }

    // строковое представление "под капотом"
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // формируем обозначение
        if (!designationOperations.isEmpty()) {
            sb.append(designationOperations).append("(").append(designation).append(")");
        } else {
            sb.append(designation);
        }
        if (subscript != null && !subscript.isEmpty()) {
            sb.append("_").append(subscript);
        }
        if (exponent != null && !exponent.isEmpty()) {
            sb.append("^").append(exponent);
        }

        // добавляем значение и единицу
        sb.append(" = ");
        if (!valueOperations.isEmpty()) {
            sb.append(valueOperations);
        } else {
            sb.append(SIConverter.formatValue(isConversionMode ? originalValue : value));
        }
        if (!originalUnit.isEmpty()) {
            sb.append(" ").append(isConversionMode ? originalUnit : unit);
        }

        // в режиме си добавляем шаги конвертации
        if (isConversionMode && !isSIUnit && !conversionSteps.isEmpty()) {
            sb.append(" = ").append(conversionSteps);
        }

        // добавляем шаги применения модуля
        if (isConversionMode && !moduleSteps.isEmpty()) {
            sb.append(" = ").append(moduleSteps);
        }

        return sb.toString();
    }

    // геттеры
    public boolean isConstant() { return constant; }
    public String getSubscript() { return subscript != null ? subscript : ""; }
    public SpannableStringBuilder getOriginalDisplay() { return originalDisplay; }
    public double getOriginalValue() { return originalValue; }
    public String getOriginalUnit() { return originalUnit; }
    public String getConversionSteps() { return conversionSteps; }
    public String getModuleSteps() { return moduleSteps; }
    public boolean isSIUnit() { return isSIUnit; }
}