package com.example.fizmind;

// сервис для конвертации величин в си
public class ConversionService {

    private final SIConverter converter;

    public ConversionService() {
        this.converter = new SIConverter();
    }

    // проверяет, является ли единица si-единицей для величины
    public boolean isSiUnit(PhysicalQuantity pq, String unit) {
        return pq.getSiUnit().equals(unit);
    }

    // переводит значение в си
    public Object[] convert(PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            android.util.Log.d("ConversionService", "единица измерения уже является СИ: " + unit);
            return new Object[]{value, unit};
        }
        return SIConverter.convertToSI(pq, value, unit);
    }

    // возвращает шаги перевода в си
    public String getSteps(PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            // для единиц в си не показываем шаги, возвращаем пустую строку
            return "";
        }
        return SIConverter.getConversionSteps(pq, value, unit);
    }
}