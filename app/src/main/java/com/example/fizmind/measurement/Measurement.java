package com.example.fizmind.measurement;

public abstract class Measurement {
    protected String designation;
    protected double value;
    protected String unit;

    public Measurement(String designation, double value, String unit) {
        this.designation = designation;
        this.value = value;
        this.unit = unit;
    }

    //  для проверки корректности введённых данных
    public abstract boolean validate();

    @Override
    public String toString() {
        return designation + " = " + value + " " + unit;
    }
}
