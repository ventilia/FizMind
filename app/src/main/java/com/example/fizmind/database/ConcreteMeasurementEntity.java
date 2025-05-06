package com.example.fizmind.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// сущность для измерений в базе данных
@Entity(tableName = "concrete_measurements")
public class ConcreteMeasurementEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String baseDesignation;
    public double value;
    public String unit;
    public String designationOperations;
    public String valueOperations;
    public String subscript;
    public boolean constant;
    public String originalDisplay;
    public double originalValue;
    public String originalUnit;
    public String conversionSteps;
    public boolean isSIUnit;
    public boolean isConversionMode;
    public boolean usesStix;

    // конструктор по умолчанию для room
    public ConcreteMeasurementEntity() {}

    // конструктор с параметрами
    public ConcreteMeasurementEntity(
            String baseDesignation, double value, String unit,
            String designationOperations, String valueOperations,
            String subscript, boolean constant, String originalDisplay,
            double originalValue, String originalUnit, String conversionSteps,
            boolean isSIUnit, boolean isConversionMode, boolean usesStix) {
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
        this.usesStix = usesStix;
    }

    // геттеры
    public int getId() { return id; }
    public String getBaseDesignation() { return baseDesignation; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public String getDesignationOperations() { return designationOperations; }
    public String getValueOperations() { return valueOperations; }
    public String getSubscript() { return subscript; }
    public boolean isConstant() { return constant; }
    public String getOriginalDisplay() { return originalDisplay; }
    public double getOriginalValue() { return originalValue; }
    public String getOriginalUnit() { return originalUnit; }
    public String getConversionSteps() { return conversionSteps; }
    public boolean isSIUnit() { return isSIUnit; }
    public boolean isConversionMode() { return isConversionMode; }
    public boolean isUsesStix() { return usesStix; }

    // сеттеры
    public void setId(int id) { this.id = id; }
    public void setBaseDesignation(String baseDesignation) { this.baseDesignation = baseDesignation; }
    public void setValue(double value) { this.value = value; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setDesignationOperations(String designationOperations) { this.designationOperations = designationOperations; }
    public void setValueOperations(String valueOperations) { this.valueOperations = valueOperations; }
    public void setSubscript(String subscript) { this.subscript = subscript; }
    public void setConstant(boolean constant) { this.constant = constant; }
    public void setOriginalDisplay(String originalDisplay) { this.originalDisplay = originalDisplay; }
    public void setOriginalValue(double originalValue) { this.originalValue = originalValue; }
    public void setOriginalUnit(String originalUnit) { this.originalUnit = originalUnit; }
    public void setConversionSteps(String conversionSteps) { this.conversionSteps = conversionSteps; }
    public void setSIUnit(boolean isSIUnit) { this.isSIUnit = isSIUnit; }
    public void setConversionMode(boolean isConversionMode) { this.isConversionMode = isConversionMode; }
    public void setUsesStix(boolean usesStix) { this.usesStix = usesStix; }

    @Override
    public String toString() {
        return "ConcreteMeasurementEntity{" +
                "id=" + id +
                ", baseDesignation='" + baseDesignation + '\'' +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", designationOperations='" + designationOperations + '\'' +
                ", valueOperations='" + valueOperations + '\'' +
                ", subscript='" + subscript + '\'' +
                ", constant=" + constant +
                ", originalDisplay='" + originalDisplay + '\'' +
                ", originalValue=" + originalValue +
                ", originalUnit='" + originalUnit + '\'' +
                ", conversionSteps='" + conversionSteps + '\'' +
                ", isSIUnit=" + isSIUnit +
                ", isConversionMode=" + isConversionMode +
                ", usesStix=" + usesStix +
                '}';
    }
}