package com.example.fizmind.measurement;

import android.util.Log;

import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;

public class ConcreteMeasurement extends Measurement {

    public ConcreteMeasurement(String designation, double value, String unit) {
        super(designation, value, unit);
    }

    @Override
    public boolean validate() {
        //  обозначение не должно быть пустым
        if (designation == null || designation.isEmpty()) {
            Log.e("ConcreteMeasurement", "Пустое обозначение");
            return false;
        }

        // получаем информацию о физической величине по обозначению
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            Log.e("ConcreteMeasurement", "Не найдена информация для физической величины: " + designation);
            return false;
        }

        // проверка базы
        if (!pq.getAllowedUnits().contains(unit)) {
            Log.e("ConcreteMeasurement", "Неверная единица измерения для " + designation +
                    ". Ожидалось: " + pq.getAllowedUnits() + ", введено: " + unit);
            return false;
        }
        return true;
    }
}
