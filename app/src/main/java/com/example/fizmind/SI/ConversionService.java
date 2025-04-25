package com.example.fizmind.SI;

import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.utils.LogUtils;


public class ConversionService {

    private final SIConverter converter;

    public ConversionService() {
        this.converter = new SIConverter();
    }

    public boolean isSiUnit(PhysicalQuantity pq, String unit) {
        boolean isSi = pq.getSiUnit().equals(unit);
        if (isSi) {
            LogUtils.d("ConversionService", "единица измерения уже является СИ: " + unit);
        }
        return isSi;
    }

    public Object[] convert(PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            return new Object[]{value, unit};
        }
        return SIConverter.convertToSI(pq, value, unit);
    }

    public String getSteps(PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            return "";
        }
        return SIConverter.getConversionSteps(pq, value, unit);
    }
}