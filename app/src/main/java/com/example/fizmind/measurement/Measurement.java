package com.example.fizmind.measurement;

public abstract class Measurement {
    protected String designation;
    protected double value;
    protected String unit;
    protected String designationOperations;
    protected String valueOperations;


    public Measurement(String designation, double value, String unit, String designationOperations, String valueOperations) {
        this.designation = designation;
        this.value = value;
        this.unit = unit;
        this.designationOperations = designationOperations;
        this.valueOperations = valueOperations;
    }

    public Measurement(String designation, double value, String unit) {
        this(designation, value, unit, "", "");
    }


    public String getDesignation() {
        return designation;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getDesignationOperations() {
        return designationOperations;
    }

    public String getValueOperations() {
        return valueOperations;
    }

    public abstract boolean validate();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!designationOperations.isEmpty()) {
            sb.append(designationOperations).append("(").append(designation).append(")");
        } else {
            sb.append(designation);
        }
        sb.append(" = ");
        if (!valueOperations.isEmpty()) {
            sb.append(valueOperations);
        } else {
            sb.append(value);
        }
        if (!unit.isEmpty()) {
            sb.append(" ").append(unit);
        }
        return sb.toString();
    }
}