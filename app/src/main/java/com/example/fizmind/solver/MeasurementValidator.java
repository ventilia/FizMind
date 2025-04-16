package com.example.fizmind.solver;

import com.example.fizmind.measurement.ConcreteMeasurement;
import java.util.List;

/**
 * интерфейс для проверки и конвертации измерений в СИ
 */
public interface MeasurementValidator {
    /**
     * проверяет, требуют ли измерения конвертации в СИ
     * @param measurements список измерений
     * @return true, если хотя бы одно измерение требует конвертации
     */
    boolean requiresConversion(List<ConcreteMeasurement> measurements);

    /**
     * выполняет конвертацию измерений в СИ
     * @param measurements список измерений
     * @return список измерений в СИ
     */
    List<ConcreteMeasurement> convertToSI(List<ConcreteMeasurement> measurements);
}