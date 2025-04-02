package com.example.fizmind;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PhysicalQuantityRegistry {
    private static final Map<String, PhysicalQuantity> registry = new HashMap<>();

    static {
        // Ускорение: a
        registry.put("a_latin", new PhysicalQuantity("a_latin", "m/s²",
                Arrays.asList("m/s²", "cm/s²", "km/h²")));

        // Скорость: v (латинская и нелатинская)
        registry.put("v_latin", new PhysicalQuantity("v_latin", "m/s",
                Arrays.asList("m/s", "km/h", "cm/s")));
        registry.put("v", new PhysicalQuantity("v", "m/s", // Исправлено с "м/с" на "m/s" для консистентности
                Arrays.asList("m/s", "km/h", "cm/s")));

        // Длина/расстояние: s
        registry.put("s_latin", new PhysicalQuantity("s_latin", "m",
                Arrays.asList("m", "km", "cm")));

        // Время: t
        registry.put("t_latin", new PhysicalQuantity("t_latin", "s",
                Arrays.asList("s", "min", "h")));

        // Масса: m
        registry.put("m_latin", new PhysicalQuantity("m_latin", "kg",
                Arrays.asList("kg", "g", "t")));

        // Сила: F
        registry.put("F_latin", new PhysicalQuantity("F_latin", "N",
                Arrays.asList("N", "kN", "dyne")));

        // Гравитационное ускорение: g (константа)
        registry.put("designation_g", new PhysicalQuantity("designation_g", "m/s²",
                Arrays.asList("m/s²"), true, 9.81));

        // Давление: P
        registry.put("P_latin", new PhysicalQuantity("P_latin", "Pa",
                Arrays.asList("Pa", "kPa", "atm")));

        // Энергия: E
        registry.put("E_latin", new PhysicalQuantity("E_latin", "J",
                Arrays.asList("J", "kJ", "cal")));
        registry.put("designation_E", new PhysicalQuantity("designation_E", "J",
                Arrays.asList("J", "kJ", "cal")));

        // Мощность: W
        registry.put("designation_W", new PhysicalQuantity("designation_W", "W",
                Arrays.asList("W", "kW", "hp")));

        // Плотность: ρ (rho)
        registry.put("designation_ρ", new PhysicalQuantity("designation_ρ", "kg/m³",
                Arrays.asList("kg/m³", "g/cm³", "g/mL")));
        registry.put("designation_rho", new PhysicalQuantity("designation_rho", "kg/m³",
                Arrays.asList("kg/m³", "g/cm³", "g/mL")));

        // Сила: N
        registry.put("N_latin", new PhysicalQuantity("N_latin", "N",
                Arrays.asList("N", "kN", "dyne")));
        registry.put("designation_N", new PhysicalQuantity("designation_N", "N",
                Arrays.asList("N", "kN", "dyne")));

        // Площадь: S
        registry.put("S_latin", new PhysicalQuantity("S_latin", "m²",
                Arrays.asList("m²", "cm²", "km²")));

        // Высота: h
        registry.put("h_latin", new PhysicalQuantity("h_latin", "m",
                Arrays.asList("m", "cm", "km")));

        // Электрический ток: I
        registry.put("designation_I", new PhysicalQuantity("designation_I", "A",
                Arrays.asList("A", "mA", "kA")));

        // Напряжение: U
        registry.put("U_latin", new PhysicalQuantity("U_latin", "V",
                Arrays.asList("V", "kV", "mV")));

        // Сопротивление: R
        registry.put("R_latin", new PhysicalQuantity("R_latin", "Ω",
                Arrays.asList("Ω", "kΩ", "MΩ")));

        // Емкость: C
        registry.put("C_latin", new PhysicalQuantity("C_latin", "F",
                Arrays.asList("F", "μF", "nF")));

        // Индуктивность: L
        registry.put("L_latin", new PhysicalQuantity("L_latin", "H",
                Arrays.asList("H", "mH", "μH")));

        // Магнитный поток: Φ
        registry.put("designation_Φ", new PhysicalQuantity("designation_Φ", "Wb",
                Arrays.asList("Wb", "Mx", "T·m²")));

        // Магнитная индукция: B
        registry.put("B_latin", new PhysicalQuantity("B_latin", "T",
                Arrays.asList("T", "mT", "G")));

        // Объём: V
        registry.put("designation_V", new PhysicalQuantity("designation_V", "m³",
                Arrays.asList("m³", "L", "cm³")));

        // Коэффициент: k (безразмерный)
        registry.put("designation_k", new PhysicalQuantity("designation_k", "",
                Arrays.asList("")));

        // Температура: T
        registry.put("designation_T", new PhysicalQuantity("designation_T", "K",
                Arrays.asList("K", "°C", "°F")));
    }

    public static PhysicalQuantity getPhysicalQuantity(String designation) {
        return registry.get(designation);
    }
}