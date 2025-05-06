package com.example.fizmind.SI;

import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

// класс для конвертации единиц измерения в систему си
public class SIConverter {
    private final AppDatabase database;

    // конструктор с подключением к базе данных
    public SIConverter(AppDatabase database) {
        this.database = database;
    }

    // конвертация значения в си
    public Object[] convertToSI(String designation, double value, String unit) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            LogUtils.d("SIConverter", "физическая величина не найдена: " + designation);
            return null;
        }

        if (pq.isConstant()) {
            return new Object[]{pq.getConstantValue(), pq.getSiUnit()};
        }

        String unitLower = unit.toLowerCase();
        if (!pq.getAllowedUnits().contains(unitLower)) {
            LogUtils.e("SIConverter", "недопустимая единица для " + designation + ": " + unit);
            return null;
        }

        // обработка температуры
        if (designation.equals("designation_T")) {
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
            LogUtils.e("SIConverter", "коэффициент пересчета не найден для единицы: " + unit);
            return null;
        }

        double siValue = value * factor;
        return new Object[]{siValue, pq.getSiUnit()};
    }

    // получение шагов перевода в си
    public String getConversionSteps(String designation, double value, String unit) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            return "ошибка: физическая величина не найдена";
        }

        if (pq.isConstant()) {
            return "";
        }

        String unitLower = unit.toLowerCase();
        if (!pq.getAllowedUnits().contains(unitLower)) {
            return "ошибка: недопустимая единица измерения " + unit;
        }

        // обработка температуры
        if (designation.equals("designation_T")) {
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
        return String.format("%s %s × %s = %s %s",
                formatValue(value), unit, formatValue(factor), formatValue(siValue), pq.getSiUnit());
    }

    // форматирование числового значения
    public static String formatValue(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        } else {
            return String.format("%.10f", value).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }
}