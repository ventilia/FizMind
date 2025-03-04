package com.example.fizmind;



import java.util.List;


public class PhysicalQuantity {
    private final String designation;
    private final String siUnit;
    private final List<String> allowedUnits;

    public PhysicalQuantity(String designation, String siUnit, List<String> allowedUnits) {
        this.designation = designation;
        this.siUnit = siUnit;
        this.allowedUnits = allowedUnits;
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
}
