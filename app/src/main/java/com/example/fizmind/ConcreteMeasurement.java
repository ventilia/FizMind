package com.example.fizmind;

import android.util.Log;

public class ConcreteMeasurement extends Measurement {

    public ConcreteMeasurement(String designation, double value, String unit) {
        super(designation, value, unit);
    }

    @Override
    public boolean validate() {
        // проверка
        if (designation == null || designation.isEmpty()) {
            Log.e("ConcreteMeasurement", "Пустое обозначение");
            return false;
        }
        //
        return true;
    }
}
