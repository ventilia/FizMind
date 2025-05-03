package com.example.fizmind.quantly;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// класс для управления реестром физических величин
public class PhysicalQuantityRegistry {
    // статическая карта для хранения физических величин
    private static final Map<String, PhysicalQuantity> registry = new HashMap<>();
    // значение ускорения свободного падения по умолчанию
    private static double gravityValue = 9.8;

    // статический блок инициализации реестра
    static {
        // длина: s
        registry.put("s_latin", new PhysicalQuantity("s_latin", "длина", "m",
                Arrays.asList("cm", "m", "km"), false, 0.0));

        // время: t
        registry.put("designation_t", new PhysicalQuantity("designation_t", "время", "s",
                Arrays.asList("ms", "s", "min"), false, 0.0));

        // скорость: v
        registry.put("v_latin", new PhysicalQuantity("v_latin", "скорость", "m/s",
                Arrays.asList("cm/s", "m/s", "km/h"), false, 0.0));

        // ускорение: a
        registry.put("a_latin", new PhysicalQuantity("a_latin", "ускорение", "m/s²",
                Arrays.asList("cm/s²", "m/s²", "km/h²"), false, 0.0));

        // масса: m
        registry.put("m_latin", new PhysicalQuantity("m_latin", "масса", "kg",
                Arrays.asList("g", "kg", "t"), false, 0.0));

        // сила: F
        registry.put("F_latin", new PhysicalQuantity("F_latin", "сила", "N",
                Arrays.asList("dyne", "N", "kN"), false, 0.0));

        // вес: P
        registry.put("designation_P", new PhysicalQuantity("designation_P", "вес", "N",
                Arrays.asList("dyne", "N", "kN"), false, 0.0));

        // плотность: ρ
        registry.put("designation_ρ", new PhysicalQuantity("designation_ρ", "плотность", "kg/m³",
                Arrays.asList("g/cm³", "kg/m³", "t/m³"), false, 0.0));

        // давление: p
        registry.put("designation_p", new PhysicalQuantity("designation_p", "давление", "Pa",
                Arrays.asList("mmHg", "Pa", "atm"), false, 0.0));

        // работа: A
        registry.put("designation_A", new PhysicalQuantity("designation_A", "работа", "J",
                Arrays.asList("erg", "J", "kJ"), false, 0.0));

        // мощность: N
        registry.put("designation_N", new PhysicalQuantity("designation_N", "мощность", "W",
                Arrays.asList("erg/s", "W", "kW"), false, 0.0));

        // энергия: E
        registry.put("E_latin", new PhysicalQuantity("E_latin", "энергия", "J",
                Arrays.asList("J", "kJ", "cal"), false, 0.0));
        registry.put("E_latin_p", new PhysicalQuantity("E_latin_p", "потенциальная энергия", "J",
                Arrays.asList("J", "kJ", "cal"), false, 0.0));
        registry.put("E_latin_k", new PhysicalQuantity("E_latin_k", "кинетическая энергия", "J",
                Arrays.asList("J", "kJ", "cal"), false, 0.0));

        // температура: T
        registry.put("designation_T", new PhysicalQuantity("designation_T", "температура", "K",
                Arrays.asList("°C", "K", "°F"), false, 0.0));

        // количество теплоты: Q
        registry.put("designation_Q", new PhysicalQuantity("designation_Q", "количество теплоты", "J",
                Arrays.asList("cal", "J", "kJ"), false, 0.0));

        // электрический ток: I
        registry.put("designation_I", new PhysicalQuantity("designation_I", "электрический ток", "A",
                Arrays.asList("mA", "A", "kA"), false, 0.0));

        // напряжение: U
        registry.put("U_latin", new PhysicalQuantity("U_latin", "напряжение", "V",
                Arrays.asList("mV", "V", "kV"), false, 0.0));

        // сопротивление: R
        registry.put("R_latin", new PhysicalQuantity("R_latin", "сопротивление", "Ω",
                Arrays.asList("mΩ", "Ω", "kΩ"), false, 0.0));

        // мощность электрического тока: P
        registry.put("P_power", new PhysicalQuantity("P_power", "мощность электрического тока", "W",
                Arrays.asList("mW", "W", "kW"), false, 0.0));

        // скорость света: c
        registry.put("designation_c", new PhysicalQuantity("designation_c", "скорость света", "m/s",
                Arrays.asList("cm/s", "m/s", "km/s"), false, 0.0));

        // длина волны: λ
        registry.put("designation_λ", new PhysicalQuantity("designation_λ", "длина волны", "m",
                Arrays.asList("nm", "m", "km"), false, 0.0));

        // частота: f
        registry.put("designation_f", new PhysicalQuantity("designation_f", "частота", "Hz",
                Arrays.asList("mHz", "Hz", "kHz"), false, 0.0));

        // объем: V
        registry.put("designation_V", new PhysicalQuantity("designation_V", "объем", "m³",
                Arrays.asList("cm³", "m³", "L"), false, 0.0));

        // площадь: S
        registry.put("S_latin", new PhysicalQuantity("S_latin", "площадь", "m²",
                Arrays.asList("cm²", "m²", "km²"), false, 0.0));

        // высота: h
        registry.put("h_latin", new PhysicalQuantity("h_latin", "высота", "m",
                Arrays.asList("cm", "m", "km"), false, 0.0));

        // ускорение свободного падения: g
        registry.put("designation_g", new PhysicalQuantity("designation_g", "ускорение свободного падения", "m/s²",
                Arrays.asList("cm/s²", "m/s²", "km/s²"), true, gravityValue));

        // скорость света как константа: c
        registry.put("c_latin", new PhysicalQuantity("c_latin", "скорость света", "m/s",
                Arrays.asList("cm/s", "m/s", "km/s"), true, 299792458.0));
    }

    // обновление значения ускорения свободного падения из настроек
    public static void updateGravityValue(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useTen = prefs.getBoolean("gravity_value", false);
        gravityValue = useTen ? 10.0 : 9.8;
        registry.put("designation_g", new PhysicalQuantity("designation_g", "ускорение свободного падения", "m/s²",
                Arrays.asList("cm/s²", "m/s²", "km/s²"), true, gravityValue));
    }

    // получение физической величины по обозначению
    public static PhysicalQuantity getPhysicalQuantity(String designation) {
        return registry.get(designation);
    }

    // получение текущего значения ускорения свободного падения
    public static double getGravityValue() {
        return gravityValue;
    }

    // очистка реестра (если потребуется для новой логики)
    public static void clearRegistry() {
        registry.clear();
    }

    // добавление новой физической величины в реестр
    public static void addPhysicalQuantity(String key, PhysicalQuantity quantity) {
        registry.put(key, quantity);
    }
}