package com.example.fizmind.measurement;

import android.text.SpannableStringBuilder;
import com.example.fizmind.SIConverter;
import com.example.fizmind.utils.LogUtils;

public class ConcreteMeasurement extends Measurement {

    private final String subscript;
    private final boolean constant;
    private final SpannableStringBuilder originalDisplay;
    private final double originalValue;
    private final String originalUnit;
    private final String conversionSteps;
    private final boolean isSIUnit;
    private final boolean isConversionMode;

    public ConcreteMeasurement(String designation, double value, String unit,
                               String designationOperations, String valueOperations,
                               String subscript, boolean constant,
                               SpannableStringBuilder originalDisplay,
                               double originalValue, String originalUnit,
                               String conversionSteps, boolean isSIUnit, boolean isConversionMode) {
        super(designation, value, unit, designationOperations, valueOperations);
        this.subscript = subscript;
        this.constant = constant;
        this.originalDisplay = originalDisplay;
        this.originalValue = originalValue;
        this.originalUnit = originalUnit;
        this.conversionSteps = conversionSteps != null ? conversionSteps : "";
        this.isSIUnit = isSIUnit;
        this.isConversionMode = isConversionMode;
        LogUtils.logMeasurementCreated("ConcreteMeasurement", toString());
    }

    @Override
    public boolean validate() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            LogUtils.e("ConcreteMeasurement", "недопустимое значение: " + value);
            return false;
        }
        if (unit == null || unit.isEmpty()) {
            LogUtils.e("ConcreteMeasurement", "единица измерения не указана для " + designation);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (!designationOperations.isEmpty()) {
            sb.append(designationOperations).append("(").append(designation).append(")");
        } else {
            sb.append(designation);
        }
        if (subscript != null && !subscript.isEmpty()) {
            sb.append("_").append(subscript);
        }

        sb.append(" = ");
        if (!valueOperations.isEmpty()) {
            sb.append(valueOperations);
        } else {
            sb.append(SIConverter.formatValue(isConversionMode ? originalValue : value));
        }
        if (!unit.isEmpty()) {
            sb.append(" ").append(isConversionMode ? originalUnit : unit);
        }

        if (isConversionMode && !isSIUnit && !conversionSteps.isEmpty()) {
            sb.append(" = ").append(conversionSteps);
        }

        return sb.toString();
    }

    public boolean isConstant() { return constant; }
    public String getSubscript() { return subscript != null ? subscript : ""; }
    public SpannableStringBuilder getOriginalDisplay() { return originalDisplay; }
    public double getOriginalValue() { return originalValue; }
    public String getOriginalUnit() { return originalUnit; }
    public String getConversionSteps() { return conversionSteps; }
    public boolean isSIUnit() { return isSIUnit; }
}