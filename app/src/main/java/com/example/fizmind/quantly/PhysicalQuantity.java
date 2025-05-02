package com.example.fizmind.quantly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalQuantity {
    private final String designation;
    private final String siUnit;
    private final List<String> allowedUnits;
    private final boolean isConstant;
    private final double constantValue;
    private final Map<String, Double> conversionFactors;
    private final String description;
    //

    // конструктор для обычных величин
    public PhysicalQuantity(String designation, String siUnit, List<String> allowedUnits, String description) {
        this(designation, siUnit, allowedUnits, false, 0.0, new HashMap<>(), description);
        initConversionFactors();
    }

    // конструктор для констант
    public PhysicalQuantity(String designation, String siUnit, List<String> allowedUnits,
                            boolean isConstant, double constantValue, String description) {
        this(designation, siUnit, allowedUnits, isConstant, constantValue, new HashMap<>(), description);
        initConversionFactors();
    }

    // полный конструктор
    private PhysicalQuantity(String designation, String siUnit, List<String> allowedUnits,
                             boolean isConstant, double constantValue, Map<String, Double> conversionFactors,
                             String description) {
        this.designation = designation;
        this.siUnit = siUnit;
        this.allowedUnits = allowedUnits;
        this.isConstant = isConstant;
        this.constantValue = constantValue;
        this.conversionFactors = conversionFactors;
        this.description = description;
    }

    //  коэфф пересчета
    private void initConversionFactors() {
        switch (designation) {
            case "m_latin": // масса
                conversionFactors.put("kg", 1.0);
                conversionFactors.put("g", 0.001); // 1 г = 0.001 кг
                conversionFactors.put("t", 1000.0); // 1 т = 1000 кг
                break;
            case "s_latin": // длина
            case "h_latin": // высота
            case "designation_λ": // длина волны
                conversionFactors.put("m", 1.0);
                conversionFactors.put("cm", 0.01); // 1 см = 0.01 м
                conversionFactors.put("km", 1000.0); // 1 км = 1000 м
                conversionFactors.put("nm", 1e-9); // 1 нм = 10^-9 м
                break;
            case "designation_t": // время
                conversionFactors.put("s", 1.0);
                conversionFactors.put("ms", 0.001); // 1 мс = 0.001 с
                conversionFactors.put("min", 60.0); // 1 мин = 60 с
                break;
            case "v_latin": // скорость
            case "c_latin": // скорость света
                conversionFactors.put("m/s", 1.0);
                conversionFactors.put("cm/s", 0.01); // 1 см/с = 0.01 м/с
                conversionFactors.put("km/s", 1000.0); // 1 км/с = 1000 м/с
                conversionFactors.put("km/h", 0.27777778); // 1 км/ч ≈ 0.277778 м/с
                break;
            case "a_latin": // ускорение
            case "designation_g": // ускорение свободного падения
                conversionFactors.put("m/s²", 1.0);
                conversionFactors.put("cm/s²", 0.01); // 1 см/с² = 0.01 м/с²
                conversionFactors.put("km/s²", 1000.0); // 1 км/с² = 1000 м/с²
                conversionFactors.put("km/h²", 7.716049e-5); // 1 км/ч² ≈ 7.716049e-5 м/с²
                break;
            case "F_latin": // сила
            case "designation_P": // вес
                conversionFactors.put("N", 1.0);
                conversionFactors.put("dyne", 1e-5); // 1 дин = 10^-5 Н
                conversionFactors.put("kN", 1000.0); // 1 кН = 1000 Н
                break;
            case "designation_ρ": // плотность
                conversionFactors.put("kg/m³", 1.0);
                conversionFactors.put("g/cm³", 1000.0); // 1 г/см³ = 1000 кг/м³
                conversionFactors.put("t/m³", 1000.0); // 1 т/м³ = 1000 кг/м³
                break;
            case "designation_p": // давление
                conversionFactors.put("Pa", 1.0);
                conversionFactors.put("mmHg", 133.322); // 1 мм рт. ст. ≈ 133.322 Па
                conversionFactors.put("atm", 101325.0); // 1 атм = 101325 Па
                break;
            case "designation_A": // работа
            case "designation_Q": // количество теплоты
            case "E_latin": // энергия
            case "E_latin_p": // потенциальная энергия
            case "E_latin_k": // кинетическая энергия
                conversionFactors.put("J", 1.0);
                conversionFactors.put("erg", 1e-7); // 1 эрг = 10^-7 Дж
                conversionFactors.put("kJ", 1000.0); // 1 кДж = 1000 Дж
                conversionFactors.put("cal", 4.184); // 1 кал = 4.184 Дж
                break;
            case "designation_N": // мощность
            case "P_power": // мощность электрического тока
                conversionFactors.put("W", 1.0);
                conversionFactors.put("erg/s", 1e-7); // 1 эрг/с = 10^-7 Вт
                conversionFactors.put("kW", 1000.0); // 1 кВт = 1000 Вт
                conversionFactors.put("mW", 0.001); // 1 мВт = 0.001 Вт
                break;
            case "designation_I": // электрический ток
                conversionFactors.put("A", 1.0);
                conversionFactors.put("mA", 0.001); // 1 мА = 0.001 А
                conversionFactors.put("kA", 1000.0); // 1 кА = 1000 А
                break;
            case "U_latin": // напряжение
                conversionFactors.put("V", 1.0);
                conversionFactors.put("mV", 0.001); // 1 мВ = 0.001 В
                conversionFactors.put("kV", 1000.0); // 1 кВ = 1000 В
                break;
            case "R_latin": // сопротивление
                conversionFactors.put("Ω", 1.0);
                conversionFactors.put("mΩ", 0.001); // 1 мОм = 0.001 Ом
                conversionFactors.put("kΩ", 1000.0); // 1 кОм = 1000 Ом
                break;
            case "designation_f": // частота
                conversionFactors.put("Hz", 1.0);
                conversionFactors.put("mHz", 0.001); // 1 мГц = 0.001 Гц
                conversionFactors.put("kHz", 1000.0); // 1 кГц = 1000 Гц
                break;
            case "designation_V": // объем
                conversionFactors.put("m³", 1.0);
                conversionFactors.put("cm³", 1e-6); // 1 см³ = 10^-6 м³
                conversionFactors.put("L", 0.001); // 1 л = 0.001 м³
                break;
            case "S_latin": // площадь
                conversionFactors.put("m²", 1.0);
                conversionFactors.put("cm²", 0.0001); // 1 см² = 0.0001 м²
                conversionFactors.put("km²", 1000000.0); // 1 км² = 1,000,000 м²
                break;
            default:
                for (String unit : allowedUnits) {
                    conversionFactors.put(unit, 1.0); // по умолчанию, если не указано
                }
                break;
        }
    }


    public String getDesignation() {
        return designation;
    }

    public String getSiUnit() {
        return siUnit;
    }

    public List<String> getAllowedUnits() {
        return allowedUnits;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public double getConstantValue() {
        return constantValue;
    }

    public Double getConversionFactor(String unit) {
        return conversionFactors.get(unit.toLowerCase());
    }

    public String getDescription() {
        return description;
    }
}