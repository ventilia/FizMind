package com.example.fizmind;

// сервис для конвертации величин в СИ
public class ConversionService {

    private final SIConverter converter;

    public ConversionService() {
        this.converter = new SIConverter();
    }

    // проверяет, является ли единица SI-единицей для величины
    public boolean isSiUnit(PhysicalQuantity pq, String unit) {
        return pq.getSiUnit().equals(unit);
    }

    // переводит значение в СИ
    public Object[] convert(PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            android.util.Log.d("ConversionService", "Единица измерения уже является СИ: " + unit);
            return new Object[]{value, unit};
        }
        return SIConverter.convertToSI(pq, value, unit);
    }

    // возвращает шаги перевода в СИ
    public String getSteps(PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            return String.format("%s = %.2f %s (уже в СИ)", pq.getDesignation(), value, unit);
        }
        return SIConverter.getConversionSteps(pq, value, unit);
    }
}