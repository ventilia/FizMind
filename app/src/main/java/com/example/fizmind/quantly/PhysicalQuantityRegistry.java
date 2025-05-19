package com.example.fizmind.quantly;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// класс для управления реестром физических величин
public class PhysicalQuantityRegistry {
    private static final Map<String, PhysicalQuantity> registry = new HashMap<>();
    private static double gravityValue = 9.8; // значение ускорения свободного падения по умолчанию

    static {
        // длина
        registry.put("s_latin", new PhysicalQuantity("s_latin", "длина", "m",
                Arrays.asList("cm", "m", "km"), false, 0.0));
        // время
        registry.put("designation_t", new PhysicalQuantity("designation_t", "время", "s",
                Arrays.asList("ms", "s", "min"), false, 0.0));
        // скорость
        registry.put("v_latin", new PhysicalQuantity("v_latin", "скорость", "m/s",
                Arrays.asList("cm/s", "m/s", "km/h"), false, 0.0));
        // ускорение
        registry.put("a_latin", new PhysicalQuantity("a_latin", "ускорение", "m/s²",
                Arrays.asList("cm/s²", "m/s²", "km/h²"), false, 0.0));
        // масса
        registry.put("m_latin", new PhysicalQuantity("m_latin", "масса", "kg",
                Arrays.asList("g", "kg", "t"), false, 0.0));
        // сила
        registry.put("F_latin", new PhysicalQuantity("F_latin", "сила", "N",
                Arrays.asList("dyne", "n", "kn"), false, 0.0));
        // вес
        registry.put("designation_P", new PhysicalQuantity("designation_P", "вес", "N",
                Arrays.asList("dyne", "n", "kn"), false, 0.0));
        // плотность
        registry.put("designation_ρ", new PhysicalQuantity("designation_ρ", "плотность", "kg/m³",
                Arrays.asList("g/cm³", "kg/m³", "t/m³"), false, 0.0));
        // давление
        registry.put("designation_p", new PhysicalQuantity("designation_p", "давление", "Pa",
                Arrays.asList("mmhg", "pa", "atm"), false, 0.0));
        // работа
        registry.put("designation_A", new PhysicalQuantity("designation_A", "работа", "J",
                Arrays.asList("j", "kj", "cal"), false, 0.0));
        // мощность
        registry.put("designation_N", new PhysicalQuantity("designation_N", "мощность", "W",
                Arrays.asList("w", "kw", "mw"), false, 0.0));
        // энергияя
        registry.put("E_latin", new PhysicalQuantity("E_latin", "энергия", "J",
                Arrays.asList("j", "kJ", "cal"), false, 0.0));
        // потенциальная энергия
        registry.put("E_latin_p", new PhysicalQuantity("E_latin_p", "потенциальная энергия", "J",
                Arrays.asList("j", "kJ", "cal"), false, 0.0));
        // кинетическая энергия
        registry.put("E_latin_k", new PhysicalQuantity("E_latin_k", "кинетическая энергия", "J",
                Arrays.asList("j", "kJ", "cal"), false, 0.0));
        // температура
        registry.put("designation_T", new PhysicalQuantity("designation_T", "температура", "K",
                Arrays.asList("°c", "k", "°f"), false, 0.0));
        // количество теплоты
        registry.put("designation_Q", new PhysicalQuantity("designation_Q", "количество теплоты", "J",
                Arrays.asList("j", "kj", "cal"), false, 0.0));
        // электрический ток
        registry.put("designation_I", new PhysicalQuantity("designation_I", "электрический ток", "A",
                Arrays.asList("ma", "a", "ka"), false, 0.0));
        // напряжение
        registry.put("U_latin", new PhysicalQuantity("U_latin", "напряжение", "V",
                Arrays.asList("mv", "v", "kv"), false, 0.0));
        // сопротивление
        registry.put("R_latin", new PhysicalQuantity("R_latin", "сопротивление", "Ω",
                Arrays.asList("mω", "ω", "kω"), false, 0.0));
        // мощность электрического тока
        registry.put("P_power", new PhysicalQuantity("P_power", "мощность электрического тока", "W",
                Arrays.asList("w", "kw", "mw"), false, 0.0));
        // скорость света
        registry.put("designation_c", new PhysicalQuantity("designation_c", "скорость света", "m/s",
                Arrays.asList("cm/s", "m/s", "km/s"), false, 0.0));
        // длина волны
        registry.put("designation_λ", new PhysicalQuantity("designation_λ", "длина волны", "m",
                Arrays.asList("nm", "m", "km"), false, 0.0));
        // частота
        registry.put("designation_f", new PhysicalQuantity("designation_f", "частота", "Hz",
                Arrays.asList("mhz", "hz", "khz"), false, 0.0));
        // объем
        registry.put("designation_V", new PhysicalQuantity("designation_V", "объем", "m³",
                Arrays.asList("cm³", "m³", "l"), false, 0.0));
        // площадь
        registry.put("S_latin", new PhysicalQuantity("S_latin", "площадь", "m²",
                Arrays.asList("cm²", "m²", "km²"), false, 0.0));
        // высота
        registry.put("h_latin", new PhysicalQuantity("h_latin", "высота", "m",
                Arrays.asList("cm", "m", "km"), false, 0.0));
        // ускорение свободного падения (константа)
        registry.put("designation_g", new PhysicalQuantity("designation_g", "ускорение свободного падения", "m/s²",
                Arrays.asList("cm/s²", "m/s²", "km/s²"), true, gravityValue));
        // скорость света (константа)
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

    // очистка реестра
    public static void clearRegistry() {
        registry.clear();
    }

    // добавление новой физической величины в реестр
    public static void addPhysicalQuantity(String key, PhysicalQuantity quantity) {
        registry.put(key, quantity);
    }

    // получение списка всех физических величин
    public static List<PhysicalQuantity> getAllQuantities() {
        return new ArrayList<>(registry.values());
    }
}