package com.example.fizmind.solver;

import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
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

        // заглушка для логики решения
        double result = 0.0;
        List<Step> steps = new ArrayList<>();
        // пример шага (заглушка, замените на реальную логику)
        if (!formulaDatabase.getFormulas().isEmpty()) {
            Formula exampleFormula = formulaDatabase.getFormulas().get(0);
            steps.add(new Step(unknownDesignation, exampleFormula, result));
        }

        LogUtils.d("Solver", "Решение для " + unknownDesignation + " с известными значениями: " + knownValues);
        return new SolutionResult(result, steps);
    }

    // внутренний класс для шага решения
    public static class Step {
        private final String variable; // переменная шага
        private final Formula formula; // формула шага
        private final double value;   // вычисленное значение

        public Step(String variable, Formula formula, double value) {
            this.variable = variable;
            this.formula = formula;
            this.value = value;
        }

        // геттеры
        public String getVariable() { return variable; }
        public Formula getFormula() { return formula; }
        public double getValue() { return value; }
    }

    // класс результата решения
    public static class SolutionResult {
        private final double result;  // итоговый результат
        private final List<Step> steps; // список шагов

        public SolutionResult(double result, List<Step> steps) {
            this.result = result;
            this.steps = steps;
        }

        // геттеры
        public double getResult() { return result; }
        public List<Step> getSteps() { return steps; }
    }
}