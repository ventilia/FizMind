package com.example.fizmind.solver;

import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.SI.SIConverter;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация валидатора измерений для проверки и конвертации измерений в систему СИ.
 */
public class MeasurementValidatorImpl implements MeasurementValidator {
    private final SIConverter siConverter;

    /**
     * Конструктор, принимающий экземпляр SIConverter для выполнения конвертации.
     * @param siConverter Экземпляр класса SIConverter
     */
    public MeasurementValidatorImpl(SIConverter siConverter) {
        this.siConverter = siConverter;
    }

    /**
     * Проверяет, требуют ли измерения конвертации в систему СИ.
     * @param measurements Список измерений для проверки
     * @return true, если хотя бы одно измерение требует конвертации, иначе false
     */
    @Override
    public boolean requiresConversion(List<ConcreteMeasurement> measurements) {
        for (ConcreteMeasurement measurement : measurements) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(measurement.getBaseDesignation());
            if (pq != null && !pq.getSiUnit().equalsIgnoreCase(measurement.getUnit())) {
                LogUtils.d("MeasurementValidatorImpl", "Измерение требует конвертации: " + measurement);
                return true;
            }
        }
        LogUtils.d("MeasurementValidatorImpl", "Все измерения уже в СИ");
        return false;
    }

    /**
     * Конвертирует список измерений в систему СИ, если это необходимо.
     * @param measurements Список измерений для конвертации
     * @return Новый список измерений, приведенных к системе СИ
     */
    @Override
    public List<ConcreteMeasurement> convertToSI(List<ConcreteMeasurement> measurements) {
        List<ConcreteMeasurement> siMeasurements = new ArrayList<>();
        for (ConcreteMeasurement measurement : measurements) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(measurement.getBaseDesignation());
            if (pq != null && !pq.getSiUnit().equalsIgnoreCase(measurement.getUnit())) {
                // Извлекаем строковый идентификатор физической величины
                String designation = pq.getId();
                // Вызываем метод convertToSI на экземпляре siConverter
                Object[] siData = siConverter.convertToSI(designation, measurement.getValue(), measurement.getUnit());
                if (siData != null) {
                    double siValue = (double) siData[0];
                    String siUnit = (String) siData[1];
                    ConcreteMeasurement siMeasurement = new ConcreteMeasurement(
                            measurement.getBaseDesignation(), siValue, siUnit,
                            measurement.getDesignationOperations(), measurement.getValueOperations(),
                            measurement.getSubscript(), measurement.isConstant(),
                            measurement.getOriginalDisplay(), measurement.getOriginalValue(),
                            measurement.getOriginalUnit(), measurement.getConversionSteps(),
                            true, measurement.isConversionMode()
                    );
                    siMeasurements.add(siMeasurement);
                    LogUtils.d("MeasurementValidatorImpl", "Успешно конвертировано в СИ: " + siMeasurement);
                } else {
                    LogUtils.w("MeasurementValidatorImpl", "Не удалось конвертировать, используется исходное: " + measurement);
                    siMeasurements.add(measurement);
                }
            } else {
                siMeasurements.add(measurement);
                LogUtils.d("MeasurementValidatorImpl", "Измерение уже в СИ или не требует конвертации: " + measurement);
            }
        }
        return siMeasurements;
    }
}