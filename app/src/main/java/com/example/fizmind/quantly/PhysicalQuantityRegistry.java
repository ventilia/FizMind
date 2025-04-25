package com.example.fizmind.quantly;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PhysicalQuantityRegistry {
    public static final Map<String, PhysicalQuantity> registry = new HashMap<>();
    private static double gravityValue = 9.8;

    static {
        // Длина: s
        registry.put("s_latin", new PhysicalQuantity("s_latin", "m",
                Arrays.asList("cm", "m", "km"), "длина"));

        // Время: t
        registry.put("designation_t", new PhysicalQuantity("designation_t", "s",
                Arrays.asList("ms", "s", "min"), "время"));

        // Скорость: v
        registry.put("v_latin", new PhysicalQuantity("v_latin", "m/s",
                Arrays.asList("cm/s", "m/s", "km/h"), "скорость"));

        // Ускорение: a
        registry.put("a_latin", new PhysicalQuantity("a_latin", "m/s²",
                Arrays.asList("cm/s²", "m/s²", "km/h²"), "ускорение"));

        // Масса: m
        registry.put("m_latin", new PhysicalQuantity("m_latin", "kg",
                Arrays.asList("g", "kg", "t"), "масса"));

        // Сила: F
        registry.put("F_latin", new PhysicalQuantity("F_latin", "N",
                Arrays.asList("dyne", "N", "kN"), "сила"));

        // Вес: P
        registry.put("designation_P", new PhysicalQuantity("designation_P", "N",
                Arrays.asList("dyne", "N", "kN"), "вес"));

        // Плотность: ρ
        registry.put("designation_ρ", new PhysicalQuantity("designation_ρ", "kg/m³",
                Arrays.asList("g/cm³", "kg/m³", "t/m³"), "плотность"));

        // Давление: p
        registry.put("designation_p", new PhysicalQuantity("designation_p", "Pa",
                Arrays.asList("mmHg", "Pa", "atm"), "давление"));

        // Работа: A
        registry.put("designation_A", new PhysicalQuantity("designation_A", "J",
                Arrays.asList("erg", "J", "kJ"), "работа"));

        // Мощность: N
        registry.put("designation_N", new PhysicalQuantity("designation_N", "W",
                Arrays.asList("erg/s", "W", "kW"), "мощность"));

        // Энергия: E
        registry.put("E_latin", new PhysicalQuantity("E_latin", "J",
                Arrays.asList("J", "kJ", "cal"), "энергия"));
        registry.put("E_latin_p", new PhysicalQuantity("E_latin_p", "J",
                Arrays.asList("J", "kJ", "cal"), "потенциальная энергия"));
        registry.put("E_latin_k", new PhysicalQuantity("E_latin_k", "J",
                Arrays.asList("J", "kJ", "cal"), "кинетическая энергия"));

        // Температура: T
        registry.put("designation_T", new PhysicalQuantity("designation_T", "K",
                Arrays.asList("°C", "K", "°F"), "температура"));

        // Количество теплоты: Q
        registry.put("designation_Q", new PhysicalQuantity("designation_Q", "J",
                Arrays.asList("cal", "J", "kJ"), "количество теплоты"));

        // Электрический ток: I
        registry.put("designation_I", new PhysicalQuantity("designation_I", "A",
                Arrays.asList("mA", "A", "kA"), "электрический ток"));

        // Напряжение: U
        registry.put("U_latin", new PhysicalQuantity("U_latin", "V",
                Arrays.asList("mV", "V", "kV"), "напряжение"));

        // Сопротивление: R
        registry.put("R_latin", new PhysicalQuantity("R_latin", "Ω",
                Arrays.asList("mΩ", "Ω", "kΩ"), "сопротивление"));

        // Мощность электрического тока: P
        registry.put("P_power", new PhysicalQuantity("P_power", "W",
                Arrays.asList("mW", "W", "kW"), "мощность электрического тока"));

        // Скорость света: c
        registry.put("designation_c", new PhysicalQuantity("designation_c", "m/s",
                Arrays.asList("cm/s", "m/s", "km/s"), "скорость света"));

        // Длина волны: λ
        registry.put("designation_λ", new PhysicalQuantity("designation_λ", "m",
                Arrays.asList("nm", "m", "km"), "длина волны"));

        // Частота: f
        registry.put("designation_f", new PhysicalQuantity("designation_f", "Hz",
                Arrays.asList("mHz", "Hz", "kHz"), "частота"));

        // Объем: V
        registry.put("designation_V", new PhysicalQuantity("designation_V", "m³",
                Arrays.asList("cm³", "m³", "L"), "объем"));

        // Площадь: S
        registry.put("S_latin", new PhysicalQuantity("S_latin", "m²",
                Arrays.asList("cm²", "m²", "km²"), "площадь"));

        // Высота: h
        registry.put("h_latin", new PhysicalQuantity("h_latin", "m",
                Arrays.asList("cm", "m", "km"), "высота"));

        // Ускорение свободного падения: g
        registry.put("designation_g", new PhysicalQuantity("designation_g", "m/s²",
                Arrays.asList("cm/s²", "m/s²", "km/s²"), true, gravityValue, "ускорение свободного падения"));

        registry.put("c_latin", new PhysicalQuantity("c_latin", "m/s",
                Arrays.asList("cm/s", "m/s", "km/s"), true, 299792458.0, "скорость света"));
        
    }




    // обновляет g
    public static void updateGravityValue(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useTen = prefs.getBoolean("gravity_value", false);
        gravityValue = useTen ? 10.0 : 9.8;
        registry.put("designation_g", new PhysicalQuantity("designation_g", "m/s²",
                Arrays.asList("cm/s²", "m/s²", "km/s²"), true, gravityValue, "ускорение свободного падения"));
    }

    public static PhysicalQuantity getPhysicalQuantity(String designation) {
        return registry.get(designation);
    }


    public static double getGravityValue() {
        return gravityValue;
    }
}