package com.example.fizmind.solver;

import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.utils.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// класс для решения уравнений на основе данных из БД
public class Solver {
    private final FormulaDatabase formulaDatabase;
    private final AppDatabase appDatabase;

    // конструктор
    public Solver(FormulaDatabase formulaDatabase, AppDatabase appDatabase) {
        this.formulaDatabase = formulaDatabase;
        this.appDatabase = appDatabase;
    }

    // решение задачи
    public SolutionResult solve() {
        List<ConcreteMeasurementEntity> measurements = appDatabase.measurementDao().getAllMeasurements();
        List<UnknownQuantityEntity> unknowns = appDatabase.unknownQuantityDao().getAllUnknowns();

        if (unknowns.isEmpty()) {
            throw new IllegalStateException("Нет неизвестных величин для решения");
        }

        String unknownDesignation = unknowns.get(0).getLogicalDesignation();
        Map<String, Double> knownValues = new HashMap<>();
        for (ConcreteMeasurementEntity measurement : measurements) {
            knownValues.put(measurement.getBaseDesignation(), measurement.getValue());
        }

        // заглушка для логики решения (замените на реальную реализацию)
        double result = 0.0;
        String steps = "Решение пока не реализовано";

        LogUtils.d("Solver", "Решение для " + unknownDesignation + " с известными значениями: " + knownValues);
        return new SolutionResult(result, steps);
    }
}

// класс результата решения
class SolutionResult {
    private final double result;
    private final String steps;

    public SolutionResult(double result, String steps) {
        this.result = result;
        this.steps = steps;
    }

    public double getResult() { return result; }
    public String getSteps() { return steps; }
}