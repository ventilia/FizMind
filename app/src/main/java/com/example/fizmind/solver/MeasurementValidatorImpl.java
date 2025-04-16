package com.example.fizmind.solver;

import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.SIConverter;
import com.example.fizmind.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * реализация интерфейса MeasurementValidator
 */
public class MeasurementValidatorImpl implements MeasurementValidator {

    @Override
    public boolean requiresConversion(List<ConcreteMeasurement> measurements) {
        // проходим по всем измерениям
        for (ConcreteMeasurement measurement : measurements) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(measurement.getDesignation());
            // если физическая величина существует и единица не является СИ
            if (pq != null && !pq.getSiUnit().equals(measurement.getUnit())) {
                LogUtils.d("MeasurementValidatorImpl", "измерение требует конвертации: " + measurement);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ConcreteMeasurement> convertToSI(List<ConcreteMeasurement> measurements) {
        List<ConcreteMeasurement> siMeasurements = new ArrayList<>();
        for (ConcreteMeasurement measurement : measurements) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(measurement.getDesignation());
            if (pq != null && !pq.getSiUnit().equals(measurement.getUnit())) {
                // выполняем конвертацию в СИ
                Object[] siData = SIConverter.convertToSI(pq, measurement.getValue(), measurement.getUnit());
                if (siData != null) {
                    double siValue = (double) siData[0];
                    String siUnit = (String) siData[1];
                    // создаем новое измерение с конвертированными данными
                    ConcreteMeasurement siMeasurement = new ConcreteMeasurement(
                            measurement.getDesignation(), siValue, siUnit,
                            measurement.getDesignationOperations(), measurement.getValueOperations(),
                            measurement.getSubscript(), measurement.isConstant(),
                            measurement.getOriginalDisplay(), measurement.getOriginalValue(),
                            measurement.getOriginalUnit(), measurement.getConversionSteps(),
                            true, measurement.isConversionMode()
                    );
                    siMeasurements.add(siMeasurement);
                    LogUtils.d("MeasurementValidatorImpl", "конвертировано в СИ: " + siMeasurement);
                } else {
                    LogUtils.e("MeasurementValidatorImpl", "не удалось конвертировать: " + measurement);
                    siMeasurements.add(measurement); // оставляем как есть при ошибке
                }
            } else {
                siMeasurements.add(measurement); // если уже в СИ или нет данных, оставляем без изменений
            }
        }
        return siMeasurements;
    }
}