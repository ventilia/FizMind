package com.example.fizmind.quantly;

import java.util.Arrays;
import java.util.List;

// класс для представления физической величины
public class PhysicalQuantity {
    private final String id;
    private final String type;
    private final String siUnit;
    private final List<String> allowedUnits;
    private final boolean isConstant;
    private final double constantValue;

    // конструктор
    public PhysicalQuantity(String id, String type, String siUnit, List<String> allowedUnits, boolean isConstant, double constantValue) {
        this.id = id;
        this.type = type;
        this.siUnit = siUnit;
        this.allowedUnits = allowedUnits;
        this.isConstant = isConstant;
        this.constantValue = constantValue;
    }

    // геттеры
    public String getId() { return id; }
    public String getType() { return type; }
    public String getSiUnit() { return siUnit; }
    public List<String> getAllowedUnits() { return allowedUnits; }
    public boolean isConstant() { return isConstant; }
    public double getConstantValue() { return constantValue; }
}