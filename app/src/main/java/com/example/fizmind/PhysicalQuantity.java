package com.example.fizmind;

import java.util.List;

public class PhysicalQuantity {
    private final String designation;
    private final String siUnit;
    private final List<String> allowedUnits;
    private final boolean isConstant;        // флаг, указывающий, является ли величина константой
    private final double constantValue;      // знач константы в SI-единицах

    // конструктор для обычных величин
    public PhysicalQuantity(String designation, String siUnit, List<String> allowedUnits) {
        this(designation, siUnit, allowedUnits, false, 0.0);
    }

    //уонструктор для констант
    public PhysicalQuantity(String designation, String siUnit, List<String> allowedUnits, boolean isConstant, double constantValue) {
        this.designation = designation;
        this.siUnit = siUnit;
        this.allowedUnits = allowedUnits;
        this.isConstant = isConstant;
        this.constantValue = constantValue;
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
}