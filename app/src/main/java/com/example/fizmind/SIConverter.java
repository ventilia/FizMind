package com.example.fizmind;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для перевода значений в систему СИ и генерации шагов перевода.
 */
public class SIConverter {

    // Словарь коэффициентов пересчета для различных единиц измерения
    private static final Map<String, Double> CONVERSION_FACTORS = new HashMap<>();

    static {
        // Инициализация коэффициентов пересчета
        CONVERSION_FACTORS.put("km", 1000.0);    // км -> м
        CONVERSION_FACTORS.put("m", 1.0);        // м -> м
        CONVERSION_FACTORS.put("cm", 0.01);      // см -> м
        CONVERSION_FACTORS.put("s", 1.0);        // с -> с
        CONVERSION_FACTORS.put("min", 60.0);     // мин -> с
        CONVERSION_FACTORS.put("h", 3600.0);     // ч -> с
        CONVERSION_FACTORS.put("kg", 1.0);       // кг -> кг
        CONVERSION_FACTORS.put("g", 0.001);      // г -> кг
        CONVERSION_FACTORS.put("mg", 0.000001);  // мг -> кг
        CONVERSION_FACTORS.put("m/s", 1.0);      // м/с -> м/с
        CONVERSION_FACTORS.put("km/h", 1000.0 / 3600.0); // км/ч -> м/с
    }

    /**
     * Переводит значение в систему СИ.
     *
     * @param pq    Физическая величина
     * @param value Значение
     * @param unit  Единица измерения
     * @return Массив [переведенное значение, SI-единица] или null при ошибке
     */
    public static Object[] convertToSI(PhysicalQuantity pq, double value, String unit) {
        if (pq.isConstant()) {
            Log.d("SIConverter", "Константа " + pq.getDesignation() + ", перевод не требуется");
            return new Object[]{pq.getConstantValue(), pq.getSiUnit()};
        }

        if (!pq.getAllowedUnits().contains(unit)) {
            Log.e("SIConverter", "Недопустимая единица для " + pq.getDesignation() + ": " + unit);
            return null;
        }

        Double factor = CONVERSION_FACTORS.get(unit);
        if (factor == null) {
            Log.e("SIConverter", "Коэффициент пересчета не найден для единицы: " + unit);
            return null;
        }

        double siValue = value * factor;
        Log.d("SIConverter", String.format("Перевод %s: %.2f %s -> %.2f %s",
                pq.getDesignation(), value, unit, siValue, pq.getSiUnit()));
        return new Object[]{siValue, pq.getSiUnit()};
    }

    /**
     * Генерирует шаги перевода в СИ.
     *
     * @param pq    Физическая величина
     * @param value Значение
     * @param unit  Единица измерения
     * @return Строка с шагами перевода
     */
    public static String getConversionSteps(PhysicalQuantity pq, double value, String unit) {
        if (pq.isConstant()) {
            return String.format("%s = %.2f %s (константа)",
                    pq.getDesignation(), pq.getConstantValue(), pq.getSiUnit());
        }

        Double factor = CONVERSION_FACTORS.get(unit);
        if (factor == null) {
            Log.e("SIConverter", "Ошибка генерации шагов: коэффициент не найден для " + unit);
            return "Ошибка: коэффициент пересчета не найден для " + unit;
        }

        double siValue = value * factor;
        String steps = String.format("%s = %.2f %s = %.2f × %.2f = %.2f %s",
                pq.getDesignation(), value, unit, value, factor, siValue, pq.getSiUnit());
        Log.d("SIConverter", "Сгенерированы шаги перевода: " + steps);
        return steps;
    }
}