package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;

// класс для представления измерения
public class ConcreteMeasurement {
    private final String baseDesignation;
    private final double value;
    private final String unit;
    private final String designationOperations;
    private final String valueOperations;
    private final String subscript;
    private final boolean constant;
    private final SpannableStringBuilder originalDisplay;
    private final double originalValue;
    private final String originalUnit;
    private final String conversionSteps;
    private final boolean isSIUnit;
    private final boolean isConversionMode;


    public ConcreteMeasurement(
            String baseDesignation, double value, String unit,
            String designationOperations, String valueOperations,
            String subscript, boolean constant, SpannableStringBuilder originalDisplay,
            double originalValue, String originalUnit, String conversionSteps,
            boolean isSIUnit, boolean isConversionMode) {
        this.baseDesignation = baseDesignation;
        this.value = value;
        this.unit = unit;
        this.designationOperations = designationOperations;
        this.valueOperations = valueOperations;
        this.subscript = subscript;
        this.constant = constant;
        this.originalDisplay = originalDisplay;
        this.originalValue = originalValue;
        this.originalUnit = originalUnit;
        this.conversionSteps = conversionSteps;
        this.isSIUnit = isSIUnit;
        this.isConversionMode = isConversionMode;
    }


    public String getBaseDesignation() { return baseDesignation; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public String getDesignationOperations() { return designationOperations; }
    public String getValueOperations() { return valueOperations; }
    public String getSubscript() { return subscript; }
    public boolean isConstant() { return constant; }
    public SpannableStringBuilder getOriginalDisplay() { return originalDisplay; }
    public double getOriginalValue() { return originalValue; }
    public String getOriginalUnit() { return originalUnit; }
    public String getConversionSteps() { return conversionSteps; }
    public boolean isSIUnit() { return isSIUnit; }
    public boolean isConversionMode() { return isConversionMode; }
}