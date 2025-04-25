package com.example.fizmind.solver;

import com.example.fizmind.measurement.ConcreteMeasurement;
import java.util.List;

//проверка си вродн
public interface MeasurementValidator {

    boolean requiresConversion(List<ConcreteMeasurement> measurements);

    List<ConcreteMeasurement> convertToSI(List<ConcreteMeasurement> measurements);
}