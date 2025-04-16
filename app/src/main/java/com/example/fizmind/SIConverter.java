package com.example.fizmind;

import com.example.fizmind.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * конвертер единиц измерения в систему СИ
 */
public class SIConverter {

    private static final Map<String, Double> CONVERSION_FACTORS = new HashMap<>();

    static {
        // расстояние
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
        // объем
        CONVERSION_FACTORS.put("m³", 1.0);
        CONVERSION_FACTORS.put("L", 0.001);
        CONVERSION_FACTORS.put("cm³", 0.000001);
    }

    /**
     * конвертирует значение в СИ
     * @param pq физическая величина
     * @param value значение
     * @param unit единица измерения
     * @return массив [значение в СИ, единица СИ] или null при ошибке
     */
    public static Object[] convertToSI(PhysicalQuantity pq, double value, String unit) {
        if (pq.isConstant()) {
            LogUtils.d("SIConverter", "константа " + pq.getDesignation() + ", перевод не требуется");
            return new Object[]{pq.getConstantValue(), pq.getSiUnit()};
        }

        // проверка единицы без учета регистра
        String unitLower = unit.toLowerCase();
        boolean isValidUnit = pq.getAllowedUnits().stream()
                .anyMatch(allowed -> allowed.toLowerCase().equals(unitLower));
        if (!isValidUnit) {
            LogUtils.e("SIConverter", "недопустимая единица для " + pq.getDesignation() + ": " + unit);
            return null;
        }

        // обработка температуры отдельно
        if (pq.getDesignation().equals("designation_T")) {
            if (unitLower.equals("k")) {
                return new Object[]{value, "K"};
            } else if (unitLower.equals("°c")) {
                double kelvin = value + 273.15;
                return new Object[]{kelvin, "K"};
            } else if (unitLower.equals("°f")) {
                double kelvin = (value - 32) * 5 / 9 + 273.15;
                return new Object[]{kelvin, "K"};
            }
        }

        // поиск коэффициента без учета регистра
        Double factor = null;
        for (Map.Entry<String, Double> entry : CONVERSION_FACTORS.entrySet()) {
            if (entry.getKey().toLowerCase().equals(unitLower)) {
                factor = entry.getValue();
                break;
            }
        }

        if (factor == null) {
            LogUtils.e("SIConverter", "коэффициент пересчета не найден для единицы: " + unit);
            return null;
        }

        double siValue = value * factor;
        LogUtils.d("SIConverter", String.format("перевод %s: %s %s -> %s %s",
                pq.getDesignation(), formatValue(value), unit, formatValue(siValue), pq.getSiUnit()));
        return new Object[]{siValue, pq.getSiUnit()};
    }

    /**
     * форматирует число для отображения
     * @param value число
     * @return отформатированная строка
     */
    public static String formatValue(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        } else {
            return String.format("%.10f", value).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }

    /**
     * возвращает шаги конвертации
     * @param pq физическая величина
     * @param value значение
     * @param unit единица измерения
     * @return строка с шагами или ошибка
     */
    public static String getConversionSteps(PhysicalQuantity pq, double value, String unit) {
        if (pq.isConstant()) {
            return "";
        }

        String unitLower = unit.toLowerCase();
        boolean isValidUnit = pq.getAllowedUnits().stream()
                .anyMatch(allowed -> allowed.toLowerCase().equals(unitLower));
        if (!isValidUnit) {
            return "ошибка: недопустимая единица измерения " + unit;
        }

        if (pq.getDesignation().equals("designation_T")) {
            if (unitLower.equals("k")) {
                return formatValue(value) + " K";
            } else if (unitLower.equals("°c")) {
                double kelvin = value + 273.15;
                return formatValue(value) + " + 273.15 = " + formatValue(kelvin) + " K";
            } else if (unitLower.equals("°f")) {
                double celsius = (value - 32) * 5 / 9;
                double kelvin = celsius + 273.15;
                return "(" + formatValue(value) + " - 32) × 5/9 + 273.15 = " + formatValue(kelvin) + " K";
            }
        }

        Double factor = null;
        for (Map.Entry<String, Double> entry : CONVERSION_FACTORS.entrySet()) {
            if (entry.getKey().toLowerCase().equals(unitLower)) {
                factor = entry.getValue();
                break;
            }
        }

        if (factor == null) {
            return "ошибка: коэффициент пересчета не найден для " + unit;
        }

        double siValue = value * factor;
        String steps = String.format("%s × %s = %s %s",
                formatValue(value), formatValue(factor), formatValue(siValue), pq.getSiUnit());
        LogUtils.d("SIConverter", "сгенерированы шаги перевода: " + steps);
        return steps;
    }
}