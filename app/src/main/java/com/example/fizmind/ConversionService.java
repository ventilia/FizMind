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
            android.util.Log.d("ConversionService", "единица измерения уже является СИ: " + unit);
            return new Object[]{value, unit};
        }
        return SIConverter.convertToSI(pq, value, unit);
    }

    // возвращает шаги перевода в СИ с использованием отображаемого обозначения
    public String getSteps(String displayDesignation, PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            // для единиц в СИ не показываем шаги, возвращаем пустую строку
            return "";
        }
        return SIConverter.getConversionSteps(displayDesignation, pq, value, unit);
    }
}