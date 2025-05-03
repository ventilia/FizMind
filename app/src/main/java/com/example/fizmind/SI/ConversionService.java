package com.example.fizmind.SI;

import com.example.fizmind.quantly.PhysicalQuantity;

import java.util.HashMap;
import java.util.Map;

// сервис для конверсии единиц измерения
public class ConversionService {
    private final Map<String, Map<String, Double>> conversionFactors;

    // конструктор
    public ConversionService() {
        conversionFactors = new HashMap<>();
        initializeConversionFactors();
    }

    // инициализация факторов конверсии
    private void initializeConversionFactors() {
        Map<String, Double> lengthFactors = new HashMap<>();
        lengthFactors.put("m", 1.0);
        lengthFactors.put("km", 1000.0);
        lengthFactors.put("cm", 0.01);
        conversionFactors.put("length", lengthFactors);
        // добавьте другие величины по необходимости
    }

    // проверка, является ли единица СИ
    public boolean isSiUnit(PhysicalQuantity pq, String unit) {
        return pq.getSiUnit().equals(unit);
    }

    // получение шагов конверсии
    public String getSteps(PhysicalQuantity pq, double value, String unit) {
        return "Конверсия: " + value + " " + unit + " -> СИ";
    }

    // конверсия в СИ
    public Object[] convert(PhysicalQuantity pq, double value, String unit) {
        Map<String, Double> factors = conversionFactors.get(pq.getType());
        if (factors != null && factors.containsKey(unit)) {
            double factor = factors.get(unit);
            return new Object[]{value * factor, pq.getSiUnit()};
        }
        return null;
    }
}