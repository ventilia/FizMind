package com.example.fizmind;

public abstract class Measurement {
    protected String designation;
    protected double value;
    protected String unit;

    public Measurement(String designation, double value, String unit) {
        this.designation = designation;
        this.value = value;
        this.unit = unit;
    }

    // проверка введеных данных
    public abstract boolean validate();

    @Override
    public String toString() {
        return designation + " = " + value + " " + unit;
    }
}
