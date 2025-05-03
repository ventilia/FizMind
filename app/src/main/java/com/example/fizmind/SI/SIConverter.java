package com.example.fizmind.SI;

import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

// класс для конвертации единиц измерения в систему СИ
public class SIConverter {
    private final AppDatabase database;

    // конструктор с подключением к базе данных
    public SIConverter(AppDatabase database) {
        this.database = database;
    }

    // конвертация значения в СИ
    public Object[] convertToSI(String designation, double value, String unit) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            LogUtils.e("SIConverter", "физическая величина не найдена: " + designation);
            return null;
        }

        // обработка констант
        if (pq.isConstant()) {
            LogUtils.d("SIConverter", "константа " + designation + ", перевод не требуется");
            return new Object[]{pq.getConstantValue(), pq.getSiUnit()};
        }

        String unitLower = unit.toLowerCase();
        boolean isValidUnit = pq.getAllowedUnits().stream()
                .anyMatch(allowed -> allowed.toLowerCase().equals(unitLower));
        if (!isValidUnit) {
            LogUtils.e("SIConverter", "недопустимая единица для " + designation + ": " + unit);
            return null;
        }

        // особая обработка температуры
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

        // получение коэффициента пересчета
        Double factor = getConversionFactor(pq, unitLower);
        if (factor == null) {
            LogUtils.e("SIConverter", "коэффициент пересчета не найден для единицы: " + unit + " в " + designation);
            return null;
        }

        double siValue = value * factor;
        LogUtils.d("SIConverter", String.format("перевод %s: %s %s -> %s %s, factor=%s",
                designation, formatValue(value), unit, formatValue(siValue), pq.getSiUnit(), formatValue(factor)));
        return new Object[]{siValue, pq.getSiUnit()};
    }

    // получение шагов перевода в СИ
    public String getConversionSteps(String designation, double value, String unit) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(designation);
        if (pq == null) {
            return "ошибка: физическая величина не найдена";
        }

        // для констант шаги не нужны
        if (pq.isConstant()) {
            return "";
        }

        String unitLower = unit.toLowerCase();
        boolean isValidUnit = pq.getAllowedUnits().stream()
                .anyMatch(allowed -> allowed.toLowerCase().equals(unitLower));
        if (!isValidUnit) {
            return "ошибка: недопустимая единица измерения " + unit;
        }

        // особая обработка температуры
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

        Double factor = getConversionFactor(pq, unitLower);
        if (factor == null) {
            return "ошибка: коэффициент пересчета не найден для " + unit;
        }

        double siValue = value * factor;
        String steps = String.format("%s %s × %s = %s %s",
                formatValue(value), unit, formatValue(factor), formatValue(siValue), pq.getSiUnit());
        LogUtils.d("SIConverter", "сгенерированы шаги перевода: " + steps);
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

    // получение коэффициента пересчета (заглушка, так как в исходном коде он берется из PhysicalQuantity)
    private Double getConversionFactor(PhysicalQuantity pq, String unit) {
        // в реальном проекте это может быть реализовано в PhysicalQuantity
        Map<String, Double> factors = new HashMap<>();
        factors.put("m", 1.0);
        factors.put("cm", 0.01);
        factors.put("km", 1000.0);
        factors.put("s", 1.0);
        factors.put("ms", 0.001);
        factors.put("min", 60.0);
        factors.put("kg", 1.0);
        factors.put("g", 0.001);
        factors.put("t", 1000.0);
        // добавьте другие единицы по необходимости
        return factors.getOrDefault(unit, pq.getSiUnit().equals(unit) ? 1.0 : null);
    }
}