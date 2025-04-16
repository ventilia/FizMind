package com.example.fizmind.solver;

import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.SIConverter;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * реализация интерфейса для проверки и конвертации измерений в СИ
 */
public class MeasurementValidatorImpl implements MeasurementValidator {

    /**
     * проверяет, требуют ли измерения конвертации в СИ
     * @param measurements список измерений
     * @return true, если хотя бы одно измерение не в СИ
     */
    @Override
    public boolean requiresConversion(List<ConcreteMeasurement> measurements) {
        for (ConcreteMeasurement measurement : measurements) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(measurement.getDesignation());
            if (pq != null && !pq.getSiUnit().equalsIgnoreCase(measurement.getUnit())) {
                LogUtils.d("MeasurementValidatorImpl", "измерение требует конвертации: " + measurement);
                return true;
            }
        }
        LogUtils.d("MeasurementValidatorImpl", "все измерения уже в СИ");
        return false;
    }

    /**
     * конвертирует измерения в СИ
     * @param measurements список исходных измерений
     * @return список измерений в СИ
     */
    @Override
    public List<ConcreteMeasurement> convertToSI(List<ConcreteMeasurement> measurements) {
        List<ConcreteMeasurement> siMeasurements = new ArrayList<>();
        for (ConcreteMeasurement measurement : measurements) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(measurement.getDesignation());
            if (pq != null && !pq.getSiUnit().equalsIgnoreCase(measurement.getUnit())) {
                Object[] siData = SIConverter.convertToSI(pq, measurement.getValue(), measurement.getUnit());
                if (siData != null) {
                    double siValue = (double) siData[0];
                    String siUnit = (String) siData[1];
                    ConcreteMeasurement siMeasurement = new ConcreteMeasurement(
                            measurement.getDesignation(), siValue, siUnit,
                            measurement.getDesignationOperations(), measurement.getValueOperations(),
                            measurement.getSubscript(), measurement.isConstant(),
                            measurement.getOriginalDisplay(), measurement.getOriginalValue(),
                            measurement.getOriginalUnit(), measurement.getConversionSteps(),
                            true, measurement.isConversionMode()
                    );
                    siMeasurements.add(siMeasurement);
                    LogUtils.d("MeasurementValidatorImpl", "успешно конвертировано в СИ: " + siMeasurement);
                } else {
                    LogUtils.w("MeasurementValidatorImpl", "не удалось конвертировать, используется исходное: " + measurement);
                    siMeasurements.add(measurement); // используем исходное измерение
                }
            } else {
                siMeasurements.add(measurement);
                LogUtils.d("MeasurementValidatorImpl", "измерение уже в СИ или не требует конвертации: " + measurement);
            }
        }
        return siMeasurements;
    }
}