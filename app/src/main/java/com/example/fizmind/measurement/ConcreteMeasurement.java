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
        // Проверка на пустое обозначение
        if (designation == null || designation.isEmpty()) {
            Log.e("ConcreteMeasurement", "Пустое обозначение");
            return false;
        }

        // Получаем информацию о физической величине
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            Log.e("ConcreteMeasurement", "Не найдена информация для физической величины: " + designation);
            return false;
        }

        // Если единица измерения пуста
        if (unit.isEmpty()) {
            // Разрешаем пустую единицу только если SI-единица тоже пуста (безразмерная величина)
            if (!pq.getSiUnit().isEmpty()) {
                Log.e("ConcreteMeasurement", "Для " + designation + " требуется единица измерения");
                return false;
            }
        } else {
            // Проверяем, входит ли введенная единица в список разрешенных
            if (!pq.getAllowedUnits().contains(unit)) {
                Log.e("ConcreteMeasurement", "Неверная единица измерения для " + designation +
                        ". Ожидалось: " + pq.getAllowedUnits() + ", введено: " + unit);
                return false;
            }
        }
        return true;
    }
}