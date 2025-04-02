package com.example.fizmind;

/**
 * Сервис для управления конвертацией величин в СИ.
 */
public class ConversionService {

    private final SIConverter converter;

    public ConversionService() {
        this.converter = new SIConverter();
    }

    /**
     * Проверяет, является ли единица измерения SI-единицей для данной физической величины.
     *
     * @param pq   Физическая величина
     * @param unit Единица измерения
     * @return true, если единица является SI-единицей, иначе false
     */
    public boolean isSiUnit(PhysicalQuantity pq, String unit) {
        return pq.getSiUnit().equals(unit);
    }

    /**
     * Выполняет перевод значения в СИ.
     *
     * @param pq    Физическая величина
     * @param value Значение
     * @param unit  Единица измерения
     * @return Массив [переведенное значение, SI-единица] или null при ошибке
     */
    public Object[] convert(PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            android.util.Log.d("ConversionService", "Единица измерения уже является СИ: " + unit);
            return new Object[]{value, unit};
        }
        return SIConverter.convertToSI(pq, value, unit);
    }

    /**
     * Возвращает шаги перевода в СИ.
     *
     * @param pq    Физическая величина
     * @param value Значение
     * @param unit  Единица измерения
     * @return Строка с шагами перевода
     */
    public String getSteps(PhysicalQuantity pq, double value, String unit) {
        if (isSiUnit(pq, unit)) {
            return String.format("%s = %.2f %s (уже в СИ)", pq.getDesignation(), value, unit);
        }
        return SIConverter.getConversionSteps(pq, value, unit);
    }
}