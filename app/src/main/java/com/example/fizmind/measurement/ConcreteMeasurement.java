package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;
import android.util.Log;

import com.example.fizmind.SIConverter;

// класс для измерений с переведенными в СИ значениями
public class ConcreteMeasurement extends Measurement {

    // поля для степени и индекса
    private final String exponent;
    private final String subscript;
    // флаг константы
    private final boolean constant;
    // оригинальное форматированное представление
    private final SpannableStringBuilder originalDisplay;
    // исходные данные
    private final double originalValue;
    private final String originalUnit;
    // шаги конвертации
    private final String conversionSteps;
    // флаг, указывающий, была ли исходная единица уже в СИ
    private final boolean isSIUnit;

    // конструктор с полной информацией
    public ConcreteMeasurement(String designation, double value, String unit,
                               String designationOperations, String valueOperations,
                               String exponent, String subscript, boolean constant,
                               SpannableStringBuilder originalDisplay,
                               double originalValue, String originalUnit,
                               String conversionSteps, boolean isSIUnit) {
        super(designation, value, unit, designationOperations, valueOperations);
        this.exponent = exponent;
        this.subscript = subscript;
        this.constant = constant;
        this.originalDisplay = originalDisplay;
        this.originalValue = originalValue;
        this.originalUnit = originalUnit;
        this.conversionSteps = conversionSteps != null ? conversionSteps : "";
        this.isSIUnit = isSIUnit;
        Log.d("ConcreteMeasurement", "создано измерение: " + toString());
    }

    // проверяет корректность измерения
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

    // строковое представление с учетом режима и состояния единицы
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // добавляем обозначение с учетом операций, индекса и степени
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

        // формируем результат в зависимости от контекста
        sb.append(" = ");
        if (!valueOperations.isEmpty()) {
            sb.append(valueOperations);
        } else {
            sb.append(SIConverter.formatValue(value));
        }
        if (!unit.isEmpty()) {
            sb.append(" ").append(unit);
        }

        // для режима конвертации СИ добавляем цепочку
        String baseString = sb.toString();
        if (!conversionSteps.isEmpty() || isSIUnit) {
            sb.setLength(0); // очищаем для построения цепочки
            // исходные данные
            sb.append(designation);
            if (subscript != null && !subscript.isEmpty()) {
                sb.append("_").append(subscript);
            }
            if (exponent != null && !exponent.isEmpty()) {
                sb.append("^").append(exponent);
            }
            sb.append(" = ").append(SIConverter.formatValue(originalValue)).append(" ").append(originalUnit);

            // шаги конвертации или указание, что единица уже в СИ
            if (isSIUnit) {
                sb.append(" - уже в СИ");
            } else if (!conversionSteps.isEmpty()) {
                sb.append(" - ").append(conversionSteps);
            }

            // итоговый результат
            sb.append(" - ").append(baseString);
        }

        return sb.toString();
    }

    // является ли константой
    public boolean isConstant() {
        return constant;
    }

    // возвращает индекс
    public String getSubscript() {
        return subscript != null ? subscript : "";
    }

    // возвращает оригинальное форматированное представление
    public SpannableStringBuilder getOriginalDisplay() {
        return originalDisplay;
    }

    // возвращает исходное значение
    public double getOriginalValue() {
        return originalValue;
    }

    // возвращает исходную единицу
    public String getOriginalUnit() {
        return originalUnit;
    }

    // возвращает шаги конвертации
    public String getConversionSteps() {
        return conversionSteps;
    }

    // возвращает флаг единицы СИ
    public boolean isSIUnit() {
        return isSIUnit;
    }
}