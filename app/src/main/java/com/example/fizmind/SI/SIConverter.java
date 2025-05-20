package com.example.fizmind.SI;

import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

public class SIConverter {
    private final AppDatabase database;

    public SIConverter(AppDatabase database) {
        this.database = database;
    }

    // конвертация значения в единицы СИ
    public Object[] convertToSI(String designation, double value, String unit) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            LogUtils.e("SIConverter", "физическая величина не найдена: " + designation);
            return null;
        }

        if (pq.isConstant()) {
            LogUtils.d("SIConverter", "конвертация не требуется, это константа: " + designation);
            return new Object[]{pq.getConstantValue(), pq.getSiUnit()};
        }

        String unitLower = unit.toLowerCase();
        if (!pq.getAllowedUnits().contains(unitLower)) {
            LogUtils.e("SIConverter", "недопустимая единица измерения для " + designation + ": " + unit);
            return null;
        }

        // обработка температуры
        if (designation.equals("designation_T")) {
            LogUtils.d("SIConverter", "начало конвертации температуры из " + unitLower);
            if (unitLower.equals("k")) {
                LogUtils.d("SIConverter", designation + " " + formatValue(value) + " K");
                return new Object[]{value, "K"};
            } else if (unitLower.equals("°c")) {
                double kelvin = value + 273.15;
                LogUtils.d("SIConverter", designation + " " + formatValue(value) + " °C + 273.15 = " + formatValue(kelvin) + " K");
                return new Object[]{kelvin, "K"};
            } else if (unitLower.equals("°f")) {
                double kelvin = (value - 32) * 5 / 9 + 273.15;
                LogUtils.d("SIConverter", designation + " " + formatValue(value) + " °F - 32) * 5/9 + 273.15 = " + formatValue(kelvin) + " K");
                return new Object[]{kelvin, "K"};
            }
        }

        Double factor = pq.getConversionFactor(unitLower);
        if (factor == null) {
            LogUtils.e("SIConverter", "коэффициент пересчета не найден для единицы: " + unit);
            return null;
        }

        double siValue = value * factor;
        LogUtils.d("SIConverter", designation + " " + formatValue(value) + " " + unit + " = " + formatValue(siValue) + " " + pq.getSiUnit());
        return new Object[]{siValue, pq.getSiUnit()};
    }

    // получение шагов перевода в СИ
    public String getConversionSteps(String designation, double value, String unit) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            LogUtils.e("SIConverter", "ошибка в шагах: физическая величина не найдена: " + designation);
            return "Ошибка: физическая величина не найдена";
        }

        if (pq.isConstant()) {
            LogUtils.d("SIConverter", "шаги не требуются, это константа: " + designation);
            return "";
        }

        String unitLower = unit.toLowerCase();
        if (!pq.getAllowedUnits().contains(unitLower)) {
            LogUtils.e("SIConverter", "ошибка в шагах: недопустимая единица: " + unit);
            return "Ошибка: недопустимая единица измерения " + unit;
        }

        // обработка температуры
        if (designation.equals("designation_T")) {
            LogUtils.d("SIConverter", "генерация шагов для температуры из " + unitLower);
            if (unitLower.equals("k")) {
                String steps = formatValue(value) + " K";
                LogUtils.d("SIConverter", designation + " " + steps);
                return steps;
            } else if (unitLower.equals("°c")) {
                double kelvin = value + 273.15;
                String steps = formatValue(value) + " + 273.15 = " + formatValue(kelvin) + " K";
                LogUtils.d("SIConverter", designation + " " + steps);
                return steps;
            } else if (unitLower.equals("°f")) {
                double celsius = (value - 32) * 5 / 9;
                double kelvin = celsius + 273.15;
                String steps = "(" + formatValue(value) + " - 32) * 5/9 + 273.15 = " + formatValue(kelvin) + " K";
                LogUtils.d("SIConverter", designation + " " + steps);
                return steps;
            }
        }

        Double factor = pq.getConversionFactor(unitLower);
        if (factor == null) {
            LogUtils.e("SIConverter", "ошибка в шагах: коэффициент пересчета не найден для " + unit);
            return "Ошибка: коэффициент пересчета не найден для " + unit;
        }

        double siValue = value * factor;
        String steps = formatValue(value) + " × " + formatValue(factor) + " = " + formatValue(siValue) + " " + pq.getSiUnit();
        LogUtils.d("SIConverter", designation + " " + steps);
        return steps;
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