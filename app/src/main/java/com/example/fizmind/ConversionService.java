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
     * Выполняет перевод значения в СИ.
     *
     * @param pq    Физическая величина
     * @param value Значение
     * @param unit  Единица измерения
     * @return Массив [переведенное значение, SI-единица] или null при ошибке
     */
    public Object[] convert(PhysicalQuantity pq, double value, String unit) {
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
        return SIConverter.getConversionSteps(pq, value, unit);
    }
}