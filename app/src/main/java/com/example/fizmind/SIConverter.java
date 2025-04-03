package com.example.fizmind;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

// класс для конвертации значений в СИ и создания шагов конвертации
public class SIConverter {

    private static final Map<String, Double> CONVERSION_FACTORS = new HashMap<>();

    static {
        // длина
        CONVERSION_FACTORS.put("m", 1.0);
        CONVERSION_FACTORS.put("km", 1000.0);
        CONVERSION_FACTORS.put("cm", 0.01);

        // время
        CONVERSION_FACTORS.put("s", 1.0);
        CONVERSION_FACTORS.put("min", 60.0);
        CONVERSION_FACTORS.put("h", 3600.0);

        // масса
        CONVERSION_FACTORS.put("kg", 1.0);
        CONVERSION_FACTORS.put("g", 0.001);
        CONVERSION_FACTORS.put("t", 1000.0);

        // скорость
        CONVERSION_FACTORS.put("m/s", 1.0);
        CONVERSION_FACTORS.put("km/h", 0.277778);
        CONVERSION_FACTORS.put("cm/s", 0.01);
        CONVERSION_FACTORS.put("м/с", 1.0);
        CONVERSION_FACTORS.put("км/ч", 0.277778);
        CONVERSION_FACTORS.put("см/с", 0.01);

        // ускорение
        CONVERSION_FACTORS.put("m/s²", 1.0);
        CONVERSION_FACTORS.put("cm/s²", 0.01);
        CONVERSION_FACTORS.put("km/h²", 0.00007716049);

        // сила
        CONVERSION_FACTORS.put("N", 1.0);
        CONVERSION_FACTORS.put("kN", 1000.0);
        CONVERSION_FACTORS.put("dyne", 0.00001);

        // давление
        CONVERSION_FACTORS.put("Pa", 1.0);
        CONVERSION_FACTORS.put("kPa", 1000.0);
        CONVERSION_FACTORS.put("atm", 101325.0);

        // энергия
        CONVERSION_FACTORS.put("J", 1.0);
        CONVERSION_FACTORS.put("kJ", 1000.0);
        CONVERSION_FACTORS.put("cal", 4.184);

        // мощность
        CONVERSION_FACTORS.put("W", 1.0);
        CONVERSION_FACTORS.put("kW", 1000.0);
        CONVERSION_FACTORS.put("hp", 745.7);

        // плотность
        CONVERSION_FACTORS.put("kg/m³", 1.0);
        CONVERSION_FACTORS.put("g/cm³", 1000.0);
        CONVERSION_FACTORS.put("g/mL", 1000.0);

        // площадь
        CONVERSION_FACTORS.put("m²", 1.0);
        CONVERSION_FACTORS.put("cm²", 0.0001);
        CONVERSION_FACTORS.put("km²", 1000000.0);

        // электрический ток
        CONVERSION_FACTORS.put("A", 1.0);
        CONVERSION_FACTORS.put("mA", 0.001);
        CONVERSION_FACTORS.put("kA", 1000.0);

        // напряжение
        CONVERSION_FACTORS.put("V", 1.0);
        CONVERSION_FACTORS.put("kV", 1000.0);
        CONVERSION_FACTORS.put("mV", 0.001);

        // сопротивление
        CONVERSION_FACTORS.put("Ω", 1.0);
        CONVERSION_FACTORS.put("kΩ", 1000.0);
        CONVERSION_FACTORS.put("MΩ", 1000000.0);

        // емкость
        CONVERSION_FACTORS.put("F", 1.0);
        CONVERSION_FACTORS.put("μF", 0.000001);
        CONVERSION_FACTORS.put("nF", 0.000000001);

        // индуктивность
        CONVERSION_FACTORS.put("H", 1.0);
        CONVERSION_FACTORS.put("mH", 0.001);
        CONVERSION_FACTORS.put("μH", 0.000001);

        // магнитный поток
        CONVERSION_FACTORS.put("Wb", 1.0);
        CONVERSION_FACTORS.put("Mx", 0.00000001);
        CONVERSION_FACTORS.put("T·m²", 1.0);

        // магнитная индукция
        CONVERSION_FACTORS.put("T", 1.0);
        CONVERSION_FACTORS.put("mT", 0.001);
        CONVERSION_FACTORS.put("G", 0.0001);

        // объём
        CONVERSION_FACTORS.put("m³", 1.0);
        CONVERSION_FACTORS.put("L", 0.001);
        CONVERSION_FACTORS.put("cm³", 0.000001);
    }

    // переводит значение в СИ. возвращает [значение, единица] или null при ошибке
    public static Object[] convertToSI(PhysicalQuantity pq, double value, String unit) {
        if (pq.isConstant()) {
            Log.d("SIConverter", "константа " + pq.getDesignation() + ", перевод не требуется");
            return new Object[]{pq.getConstantValue(), pq.getSiUnit()};
        }

        if (!pq.getAllowedUnits().contains(unit)) {
            Log.e("SIConverter", "недопустимая единица для " + pq.getDesignation() + ": " + unit);
            return null;
        }

        // обработка температуры
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
            Log.e("SIConverter", "коэффициент пересчета не найден для единицы: " + unit);
            return null;
        }

        double siValue = value * factor;
        Log.d("SIConverter", String.format("перевод %s: %s %s -> %s %s",
                pq.getDesignation(), formatValue(value), unit, formatValue(siValue), pq.getSiUnit()));
        return new Object[]{siValue, pq.getSiUnit()};
    }

    // вспомогательный метод для форматирования чисел
    private static String formatValue(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value); // целое число без десятичных знаков
        } else {
            // отображаем с достаточной точностью, удаляя лишние нули
            return String.format("%.10f", value).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }

    // создает шаги для перевода в СИ, используя отображаемое обозначение
    public static String getConversionSteps(String displayDesignation, PhysicalQuantity pq, double value, String unit) {
        if (pq.isConstant()) {
            return String.format("%s = %s %s (константа)",
                    displayDesignation, formatValue(pq.getConstantValue()), pq.getSiUnit());
        }

        if (!pq.getAllowedUnits().contains(unit)) {
            return "Ошибка: недопустимая единица измерения " + unit + " для " + displayDesignation;
        }

        // обработка температуры
        if (pq.getDesignation().equals("designation_T")) {
            if (unit.equals("K")) {
                return String.format("%s = %s K = %s K",
                        displayDesignation, formatValue(value), formatValue(value));
            } else if (unit.equals("°C")) {
                double kelvin = value + 273.15;
                return String.format("%s = %s °C = %s + 273.15 = %s K",
                        displayDesignation, formatValue(value), formatValue(value), formatValue(kelvin));
            } else if (unit.equals("°F")) {
                double celsius = (value - 32) * 5 / 9;
                double kelvin = celsius + 273.15;
                return String.format("%s = %s °F = (%s - 32) × 5/9 + 273.15 = %s K",
                        displayDesignation, formatValue(value), formatValue(value), formatValue(kelvin));
            }
        }

        Double factor = CONVERSION_FACTORS.get(unit);
        if (factor == null) {
            return "Ошибка: коэффициент пересчета не найден для " + unit;
        }

        double siValue = value * factor;
        String steps = String.format("%s = %s %s = %s × %s = %s %s",
                displayDesignation, formatValue(value), unit, formatValue(value), formatValue(factor), formatValue(siValue), pq.getSiUnit());
        Log.d("SIConverter", "сгенерированы шаги перевода: " + steps);
        return steps;
    }
}