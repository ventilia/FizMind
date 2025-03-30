package com.example.fizmind.measurement;

import android.util.Log;
import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;

/**
 * Класс, представляющий конкретное измерение с обозначением, значением и единицей измерения.
 */
public class ConcreteMeasurement extends Measurement {
    private String exponent;    // Степень значения
    private String subscript;   // Нижний индекс обозначения

    /**
     * Конструктор для измерения с полным набором параметров.
     *
     * @param designation          Логический идентификатор обозначения
     * @param value                Числовое значение
     * @param unit                 Единица измерения
     * @param designationOperations Операции над обозначением
     * @param valueOperations      Операции над значением
     * @param exponent             Степень значения
     * @param subscript            Нижний индекс обозначения
     */
    public ConcreteMeasurement(String designation, double value, String unit, String designationOperations,
                               String valueOperations, String exponent, String subscript) {
        super(designation, value, unit, designationOperations, valueOperations);
        this.exponent = exponent;
        this.subscript = subscript;
    }

    /**
     * Упрощенный конструктор для измерения без операций и модулей.
     *
     * @param designation Логический идентификатор обозначения
     * @param value       Числовое значение
     * @param unit        Единица измерения
     */
    public ConcreteMeasurement(String designation, double value, String unit) {
        this(designation, value, unit, "", "", "", "");
    }

    /**
     * Возвращает нижний индекс обозначения.
     *
     * @return Нижний индекс или пустая строка, если его нет
     */
    public String getSubscript() {
        return subscript != null ? subscript : "";
    }

    /**
     * Проверяет, является ли измерение константой.
     *
     * @return true, если измерение связано с константой, false — если нет
     */
    public boolean isConstant() {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        return pq != null && pq.isConstant();
    }

    /**
     * Валидирует измерение на корректность данных.
     *
     * @return true, если измерение валидно, false — если есть ошибки
     */
    @Override
    public boolean validate() {
        if (designation == null || designation.isEmpty()) {
            Log.e("ConcreteMeasurement", "Пустое обозначение");
            return false;
        }

        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            Log.e("ConcreteMeasurement", "Не найдена информация для физической величины: " + designation);
            return false;
        }

        if (unit.isEmpty() && !pq.getSiUnit().isEmpty()) {
            Log.e("ConcreteMeasurement", "Для " + designation + " требуется единица измерения");
            return false;
        } else if (!unit.isEmpty() && !pq.getAllowedUnits().contains(unit)) {
            Log.e("ConcreteMeasurement", "Неверная единица измерения для " + designation +
                    ". Ожидалось: " + pq.getAllowedUnits() + ", введено: " + unit);
            return false;
        }
        return true;
    }

    /**
     * Возвращает строковое представление измерения.
     *
     * @return Форматированная строка вида "обозначение = значение единица"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!designationOperations.isEmpty()) {
            sb.append(designationOperations).append("(").append(designation);
            if (subscript != null && !subscript.isEmpty()) {
                sb.append("_").append(subscript);
            }
            sb.append(")");
        } else {
            sb.append(designation);
            if (subscript != null && !subscript.isEmpty()) {
                sb.append("_").append(subscript);
            }
        }
        sb.append(" = ");
        if (!valueOperations.isEmpty()) {
            sb.append(valueOperations);
        } else {
            sb.append(value);
            if (exponent != null && !exponent.isEmpty()) {
                sb.append("^").append(exponent);
            }
        }
        if (!unit.isEmpty()) {
            sb.append(" ").append(unit);
        }
        return sb.toString();
    }
}