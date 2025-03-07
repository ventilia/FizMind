package com.example.fizmind;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;



public class PhysicalQuantityRegistry {
    private static final Map<String, PhysicalQuantity> registry = new HashMap<>();

    static {
        // Ускорение: a
        registry.put("a_latin", new PhysicalQuantity("a_latin", "m/s²", Arrays.asList("m/s²")));

        // Скорость: v (латинская и нелатинская)
        registry.put("v_latin", new PhysicalQuantity("v_latin", "m/s", Arrays.asList("m/s", "km/h", "mi/h")));


        registry.put("v", new PhysicalQuantity("v", "м/с", Arrays.asList("м/с", "км/ч")));

        // Длина/расстояние: s (например, для перемещения)
        registry.put("s_latin", new PhysicalQuantity("s_latin", "m", Arrays.asList("m", "km", "cm")));

        // Время: t
        registry.put("t_latin", new PhysicalQuantity("t_latin", "s", Arrays.asList("s", "min", "h")));

        // Масса: m
        registry.put("m_latin", new PhysicalQuantity("m_latin", "kg", Arrays.asList("kg", "g", "mg")));

        // Сила: F
        registry.put("F_latin", new PhysicalQuantity("F_latin", "N", Arrays.asList("N", "kN")));

        // Гравитационное ускорение: g
        registry.put("designation_g", new PhysicalQuantity("designation_g", "m/s²", Arrays.asList("m/s²")));

        // Давление: P
        registry.put("P_latin", new PhysicalQuantity("P_latin", "Pa", Arrays.asList("Pa", "kPa", "atm")));

        // Энергия: E
        registry.put("E_latin", new PhysicalQuantity("E_latin", "J", Arrays.asList("J", "kJ", "cal")));
        // Альтернативное обозначение для энергии (страница 2)
        registry.put("designation_E", new PhysicalQuantity("designation_E", "J", Arrays.asList("J", "kJ", "cal")));

        // Мощность: W
        registry.put("designation_W", new PhysicalQuantity("designation_W", "W", Arrays.asList("W", "kW")));
        // Если имеется повторное обозначение W на другой странице, можно использовать то же определение

        // Плотность: ρ (rho)
        registry.put("designation_ρ", new PhysicalQuantity("designation_ρ", "kg/m³", Arrays.asList("kg/m³", "g/cm³")));
        registry.put("designation_rho", new PhysicalQuantity("designation_rho", "kg/m³", Arrays.asList("kg/m³", "g/cm³")));

        // Сила (дополнительно): N
        registry.put("N_latin", new PhysicalQuantity("N_latin", "N", Arrays.asList("N", "kN")));
        // Или, если N используется для другой физической величины, уточните назначение

        // Площадь: S (например, площадь поверхности)
        registry.put("S_latin", new PhysicalQuantity("S_latin", "m²", Arrays.asList("m²", "cm²")));

        // Высота: h
        registry.put("h_latin", new PhysicalQuantity("h_latin", "m", Arrays.asList("m", "cm")));

        // Электрический ток: I
        registry.put("designation_I", new PhysicalQuantity("designation_I", "A", Arrays.asList("A", "mA")));

        // Напряжение: U
        registry.put("U_latin", new PhysicalQuantity("U_latin", "V", Arrays.asList("V", "kV")));

        // Сопротивление: R
        registry.put("R_latin", new PhysicalQuantity("R_latin", "Ω", Arrays.asList("Ω", "kΩ")));

        // Емкость: C
        registry.put("C_latin", new PhysicalQuantity("C_latin", "F", Arrays.asList("F", "μF", "nF")));

        // Индуктивность: L
        registry.put("L_latin", new PhysicalQuantity("L_latin", "H", Arrays.asList("H", "mH")));

        // Магнитный поток: Φ
        registry.put("designation_Φ", new PhysicalQuantity("designation_Φ", "Wb", Arrays.asList("Wb")));

        // Магнитная индукция: B
        registry.put("B_latin", new PhysicalQuantity("B_latin", "T", Arrays.asList("T", "mT")));

        // Объём: V
        registry.put("designation_V", new PhysicalQuantity("designation_V", "m³", Arrays.asList("m³", "L")));

        // Дополнительное обозначение N (если используется как сила)
        registry.put("designation_N", new PhysicalQuantity("designation_N", "N", Arrays.asList("N", "kN")));

        // Коэффициент или константа: k (если безразмерный, можно оставить пустой базовой единицей)
        registry.put("designation_k", new PhysicalQuantity("designation_k", "", Arrays.asList("")));

        // Температура: T (на странице 2)
        registry.put("designation_T", new PhysicalQuantity("designation_T", "K", Arrays.asList("K", "°C")));
    }

    public static PhysicalQuantity getPhysicalQuantity(String designation) {
        return registry.get(designation);
    }
}
