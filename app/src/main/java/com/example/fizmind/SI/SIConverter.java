package com.example.fizmind.SI;

import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.utils.LogUtils;

public class SIConverter {


    public static Object[] convertToSI(PhysicalQuantity pq, double value, String unit) {
        if (pq.isConstant()) {
            LogUtils.d("SIConverter", "константа " + pq.getDesignation() + ", перевод не требуется");
            return new Object[]{pq.getConstantValue(), pq.getSiUnit()};
        }

        String unitLower = unit.toLowerCase();
        boolean isValidUnit = pq.getAllowedUnits().stream()
                .anyMatch(allowed -> allowed.toLowerCase().equals(unitLower));
        if (!isValidUnit) {
            LogUtils.e("SIConverter", "недопустимая единица для " + pq.getDesignation() + ": " + unit);
            return null;
        }


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

        Double factor = pq.getConversionFactor(unitLower);
        if (factor == null) {
            LogUtils.e("SIConverter", "коэффициент пересчета не найден для единицы: " + unit + " в " + pq.getDesignation());
            return null;
        }

        double siValue = value * factor;
        LogUtils.d("SIConverter", String.format("перевод %s: %s %s -> %s %s, factor=%s",
                pq.getDesignation(), formatValue(value), unit, formatValue(siValue), pq.getSiUnit(), formatValue(factor)));
        return new Object[]{siValue, pq.getSiUnit()};
    }

    public static String formatValue(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        } else {
            return String.format("%.10f", value).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }


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
                return formatValue(value) + " °C + 273.15 = " + formatValue(kelvin) + " K";
            } else if (unitLower.equals("°f")) {
                double celsius = (value - 32) * 5 / 9;
                double kelvin = celsius + 273.15;
                return "(" + formatValue(value) + " °F - 32) × 5/9 + 273.15 = " + formatValue(kelvin) + " K";
            }
        }

        Double factor = pq.getConversionFactor(unitLower);
        if (factor == null) {
            return "ошибка: коэффициент пересчета не найден для " + unit;
        }

        double siValue = value * factor;
        String steps = String.format("%s %s × %s = %s %s",
                formatValue(value), unit, formatValue(factor), formatValue(siValue), pq.getSiUnit());
        LogUtils.d("SIConverter", "сгенерированы шаги перевода: " + steps);
        return steps;
    }
}