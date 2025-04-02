package com.example.fizmind;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для перевода значений в систему СИ и генерации шагов перевода.
 */
public class SIConverter {

    private static final Map<String, Double> CONVERSION_FACTORS = new HashMap<>();

    static {
        // Длина
        CONVERSION_FACTORS.put("m", 1.0);
        CONVERSION_FACTORS.put("km", 1000.0);
        CONVERSION_FACTORS.put("cm", 0.01);

        // Время
        CONVERSION_FACTORS.put("s", 1.0);
        CONVERSION_FACTORS.put("min", 60.0);
        CONVERSION_FACTORS.put("h", 3600.0);

        // Масса
        CONVERSION_FACTORS.put("kg", 1.0);
        CONVERSION_FACTORS.put("g", 0.001);
        CONVERSION_FACTORS.put("t", 1000.0);

        // Скорость
        CONVERSION_FACTORS.put("m/s", 1.0);
        CONVERSION_FACTORS.put("km/h", 0.277778); // 1000 / 3600
        CONVERSION_FACTORS.put("cm/s", 0.01);
        CONVERSION_FACTORS.put("м/с", 1.0);
        CONVERSION_FACTORS.put("км/ч", 0.277778);
        CONVERSION_FACTORS.put("см/с", 0.01);

        // Ускорение
        CONVERSION_FACTORS.put("m/s²", 1.0);
        CONVERSION_FACTORS.put("cm/s²", 0.01);
        CONVERSION_FACTORS.put("km/h²", 0.00007716049); // (1000 / 3600²)

        // Сила
        CONVERSION_FACTORS.put("N", 1.0);
        CONVERSION_FACTORS.put("kN", 1000.0);
        CONVERSION_FACTORS.put("dyne", 0.00001);

        // Давление
        CONVERSION_FACTORS.put("Pa", 1.0);
        CONVERSION_FACTORS.put("kPa", 1000.0);
        CONVERSION_FACTORS.put("atm", 101325.0);

        // Энергия
        CONVERSION_FACTORS.put("J", 1.0);
        CONVERSION_FACTORS.put("kJ", 1000.0);
        CONVERSION_FACTORS.put("cal", 4.184);

        // Мощность
        CONVERSION_FACTORS.put("W", 1.0);
        CONVERSION_FACTORS.put("kW", 1000.0);
        CONVERSION_FACTORS.put("hp", 745.7);

        // Плотность
        CONVERSION_FACTORS.put("kg/m³", 1.0);
        CONVERSION_FACTORS.put("g/cm³", 1000.0);
        CONVERSION_FACTORS.put("g/mL", 1000.0);

        // Площадь
        CONVERSION_FACTORS.put("m²", 1.0);
        CONVERSION_FACTORS.put("cm²", 0.0001);
        CONVERSION_FACTORS.put("km²", 1000000.0);

        // Электрический ток
        CONVERSION_FACTORS.put("A", 1.0);
        CONVERSION_FACTORS.put("mA", 0.001);
        CONVERSION_FACTORS.put("kA", 1000.0);

        // Напряжение
        CONVERSION_FACTORS.put("V", 1.0);
        CONVERSION_FACTORS.put("kV", 1000.0);
        CONVERSION_FACTORS.put("mV", 0.001);

        // Сопротивление
        CONVERSION_FACTORS.put("Ω", 1.0);
        CONVERSION_FACTORS.put("kΩ", 1000.0);
        CONVERSION_FACTORS.put("MΩ", 1000000.0);

        // Емкость
        CONVERSION_FACTORS.put("F", 1.0);
        CONVERSION_FACTORS.put("μF", 0.000001);
        CONVERSION_FACTORS.put("nF", 0.000000001);

        // Индуктивность
        CONVERSION_FACTORS.put("H", 1.0);
        CONVERSION_FACTORS.put("mH", 0.001);
        CONVERSION_FACTORS.put("μH", 0.000001);

        // Магнитный поток
        CONVERSION_FACTORS.put("Wb", 1.0);
        CONVERSION_FACTORS.put("Mx", 0.00000001);
        CONVERSION_FACTORS.put("T·m²", 1.0);

        // Магнитная индукция
        CONVERSION_FACTORS.put("T", 1.0);
        CONVERSION_FACTORS.put("mT", 0.001);
        CONVERSION_FACTORS.put("G", 0.0001);

        // Объём
        CONVERSION_FACTORS.put("m³", 1.0);
        CONVERSION_FACTORS.put("L", 0.001);
        CONVERSION_FACTORS.put("cm³", 0.000001);
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

        // Специальная обработка температуры
        if (pq.getDesignation().equals("designation_T")) {
            if (unit.equals("K")) {
                return new Object[]{value, "K"};
            } else if (unit.equals("°C")) {
                double kelvin = value + 273.15;
                return new Object[]{kelvin, "K"};
            } else if (unit.equals("°F")) {
                double kelvin = (value - 32) * 5 / 9 + 273.15;
                return new Object[]{kelvin, "K"};
            }
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

        if (!pq.getAllowedUnits().contains(unit)) {
            return "Ошибка: недопустимая единица измерения " + unit + " для " + pq.getDesignation();
        }

        // Специальная обработка температуры
        if (pq.getDesignation().equals("designation_T")) {
            if (unit.equals("K")) {
                return String.format("%s = %.2f K = %.2f K",
                        pq.getDesignation(), value, value);
            } else if (unit.equals("°C")) {
                double kelvin = value + 273.15;
                return String.format("%s = %.2f °C = %.2f + 273.15 = %.2f K",
                        pq.getDesignation(), value, value, kelvin);
            } else if (unit.equals("°F")) {
                double celsius = (value - 32) * 5 / 9;
                double kelvin = celsius + 273.15;
                return String.format("%s = %.2f °F = (%.2f - 32) × 5/9 + 273.15 = %.2f K",
                        pq.getDesignation(), value, value, kelvin);
            }
        }

        Double factor = CONVERSION_FACTORS.get(unit);
        if (factor == null) {
            return "Ошибка: коэффициент пересчета не найден для " + unit;
        }

        double siValue = value * factor;
        String steps = String.format("%s = %.2f %s = %.2f × %.2f = %.2f %s",
                pq.getDesignation(), value, unit, value, factor, siValue, pq.getSiUnit());
        Log.d("SIConverter", "Сгенерированы шаги перевода: " + steps);
        return steps;
    }
}