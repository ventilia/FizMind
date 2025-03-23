package com.example.fizmind.measurement;

import android.util.Log;
import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;

public class ConcreteMeasurement extends Measurement {
    private String exponent;
    private String subscript;

    public ConcreteMeasurement(String designation, double value, String unit, String designationOperations,
                               String valueOperations, String exponent, String subscript) {
        super(designation, value, unit, designationOperations, valueOperations);
        this.exponent = exponent;
        this.subscript = subscript;
    }

    public ConcreteMeasurement(String designation, double value, String unit) {
        this(designation, value, unit, "", "", "", "");
    }

    // Добавлен геттер для subscript
    public String getSubscript() {
        return subscript;
    }

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