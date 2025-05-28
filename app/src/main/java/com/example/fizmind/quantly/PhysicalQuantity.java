package com.example.fizmind.quantly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalQuantity {
    private final String id;
    private final String type;
    private final String siUnit;
    private final List<String> allowedUnits;
    private final boolean isConstant;
    private final double constantValue;
    private final Map<String, Double> conversionFactors;

    public PhysicalQuantity(String id, String type, String siUnit, List<String> allowedUnits, boolean isConstant, double constantValue) {
        this.id = id;
        this.type = type;
        this.siUnit = siUnit;
        this.allowedUnits = allowedUnits;
        this.isConstant = isConstant;
        this.constantValue = constantValue;
        this.conversionFactors = new HashMap<>();
        initConversionFactors();
    }

    //  коэффициенты пересчета
    private void initConversionFactors() {
        switch (id) {
            case "m_latin":
                conversionFactors.put("kg", 1.0);
                conversionFactors.put("g", 0.001);
                conversionFactors.put("t", 1000.0);
                break;
            case "s_latin":
            case "h_latin":
            case "designation_λ":
                conversionFactors.put("m", 1.0);
                conversionFactors.put("cm", 0.01);
                conversionFactors.put("km", 1000.0);
                conversionFactors.put("nm", 1e-9);
                break;
            case "designation_t":
                conversionFactors.put("s", 1.0);
                conversionFactors.put("ms", 0.001);
                conversionFactors.put("min", 60.0);
                break;
            case "v_latin":
            case "c_latin":
                conversionFactors.put("m/s", 1.0);
                conversionFactors.put("cm/s", 0.01);
                conversionFactors.put("km/s", 1000.0);
                conversionFactors.put("km/h", 0.27777778);
                break;
            case "a_latin":
            case "designation_g":
                conversionFactors.put("m/s²", 1.0);
                conversionFactors.put("cm/s²", 0.01);
                conversionFactors.put("km/s²", 1000.0);
                conversionFactors.put("km/h²", 7.716049e-5);
                break;
            case "F_latin":
            case "designation_P":
                conversionFactors.put("n", 1.0);
                conversionFactors.put("dyne", 1e-5);
                conversionFactors.put("kn", 1000.0);
                break;
            case "designation_ρ":
                conversionFactors.put("kg/m³", 1.0);
                conversionFactors.put("g/cm³", 1000.0);
                conversionFactors.put("t/m³", 1000.0);
                break;
            case "designation_p":
                conversionFactors.put("pa", 1.0);
                conversionFactors.put("mmhg", 133.322);
                conversionFactors.put("atm", 101325.0);
                break;
            case "designation_A":
            case "designation_Q":
            case "E_latin":
            case "E_latin_p":
            case "E_latin_k":
                conversionFactors.put("j", 1.0);
                conversionFactors.put("erg", 1e-7);
                conversionFactors.put("kj", 1000.0);
                conversionFactors.put("cal", 4.184);
                break;
            case "designation_N":
            case "P_power":
                conversionFactors.put("w", 1.0);
                conversionFactors.put("erg/s", 1e-7);
                conversionFactors.put("kw", 1000.0);
                conversionFactors.put("mw", 0.001);
                break;
            case "designation_I":
                conversionFactors.put("a", 1.0);
                conversionFactors.put("ma", 0.001);
                conversionFactors.put("ka", 1000.0);
                break;
            case "U_latin":
                conversionFactors.put("v", 1.0);
                conversionFactors.put("mv", 0.001);
                conversionFactors.put("kv", 1000.0);
                break;
            case "R_latin":
                conversionFactors.put("ω", 1.0);
                conversionFactors.put("mω", 0.001);
                conversionFactors.put("kω", 1000.0);
                break;
            case "designation_f":
                conversionFactors.put("hz", 1.0);
                conversionFactors.put("mhz", 0.001);
                conversionFactors.put("khz", 1000.0);
                break;
            case "designation_V":
                conversionFactors.put("m³", 1.0);
                conversionFactors.put("cm³", 1e-6);
                conversionFactors.put("l", 0.001);
                break;
            case "S_latin":
                conversionFactors.put("m²", 1.0);
                conversionFactors.put("cm²", 0.0001);
                conversionFactors.put("km²", 1000000.0);
                break;
            default:
                for (String unit : allowedUnits) {
                    conversionFactors.put(unit, 1.0);
                }
                break;
        }
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getSiUnit() { return siUnit; }
    public List<String> getAllowedUnits() { return allowedUnits; }
    public boolean isConstant() { return isConstant; }
    public double getConstantValue() { return constantValue; }
    public Double getConversionFactor(String unit) { return conversionFactors.get(unit); }
}