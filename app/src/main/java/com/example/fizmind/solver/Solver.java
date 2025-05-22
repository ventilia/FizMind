package com.example.fizmind.solver;

import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// решатель уравнений на основе данных из базы данных
public class Solver {
    private final FormulaDatabase formulaDatabase;
    private final AppDatabase appDatabase;

    // конструктор
    public Solver(FormulaDatabase formulaDatabase, AppDatabase appDatabase) {
        this.formulaDatabase = formulaDatabase;
        this.appDatabase = appDatabase;
    }

    // класс результата решения
    public static class SolutionResult {
        private final double result;
        private final List<Step> steps;

        public SolutionResult(double result, List<Step> steps) {
            this.result = result;
            this.steps = steps;
        }

        public double getResult() {
            return result;
        }

        public List<Step> getSteps() {
            return steps;
        }
    }

    // класс шага вычисления
    public static class Step {
        private final String variable;
        private final double value;
        private final Formula formula;

        public Step(String variable, double value, Formula formula) {
            this.variable = variable;
            this.value = value;
            this.formula = formula;
        }

        public String getVariable() {
            return variable;
        }

        public double getValue() {
            return value;
        }

        public Formula getFormula() {
            return formula;
        }
    }

    // метод для вычисления НОД двух чисел
    public static int gcd(int a, int b) {
        // используем алгоритм Евклида
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    // решение задачи на основе данных из БД
    public SolutionResult solve() {
        List<ConcreteMeasurementEntity> measurements = appDatabase.measurementDao().getAllMeasurements();
        List<UnknownQuantityEntity> unknowns = appDatabase.unknownQuantityDao().getAllUnknowns();

        if (unknowns.isEmpty()) {
            throw new IllegalStateException("нет неизвестных величин для решения");
        }

        String unknownDesignation = unknowns.get(0).getLogicalDesignation();
        Map<String, Double> knownValues = new HashMap<>();
        for (ConcreteMeasurementEntity measurement : measurements) {
            String fullDesignation = measurement.getSubscript().isEmpty() ?
                    measurement.getBaseDesignation() : measurement.getBaseDesignation() + "_" + measurement.getSubscript();
            knownValues.put(fullDesignation, measurement.getValue());
        }

        Map<String, Double> computedValues = new HashMap<>(knownValues);
        List<Step> steps = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        double result = computeVariable(unknownDesignation, computedValues, steps, visited);

        LogUtils.d("Solver", "решение для " + unknownDesignation + " с известными значениями: " + knownValues);
        return new SolutionResult(result, steps);
    }

    // рекурсивное вычисление переменной
    private double computeVariable(String variable, Map<String, Double> computedValues, List<Step> steps, Set<String> visited) {
        if (computedValues.containsKey(variable)) {
            return computedValues.get(variable);
        }

        if (visited.contains(variable)) {
            throw new IllegalArgumentException("обнаружен цикл в зависимостях для переменной: " + variable);
        }
        visited.add(variable);

        List<Formula> possibleFormulas = formulaDatabase.getAdjacencyList().getOrDefault(variable, new ArrayList<>());
        if (possibleFormulas.isEmpty()) {
            throw new IllegalArgumentException("нет формул для вычисления " + variable);
        }

        for (Formula formula : possibleFormulas) {
            List<String> variables = formula.getVariables();
            boolean canCompute = true;

            for (String var : variables) {
                if (!var.equals(variable) && !computedValues.containsKey(var)) {
                    canCompute = false;
                    break;
                }
            }

            if (canCompute) {
                Double[] values = new Double[variables.size()];
                for (int i = 0; i < variables.size(); i++) {
                    String var = variables.get(i);
                    values[i] = var.equals(variable) ? null : computedValues.get(var);
                }
                double result = formula.calculate(variable, values);
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    throw new IllegalArgumentException("недопустимый результат для " + variable + ": " + result);
                }
                computedValues.put(variable, result);
                steps.add(new Step(variable, result, formula));
                LogUtils.d("Solver", "вычислено: " + variable + " = " + result + " по формуле " + formula.getBaseExpression());
                visited.remove(variable);
                return result;
            }
        }

        for (Formula formula : possibleFormulas) {
            List<String> variables = formula.getVariables();
            for (String var : variables) {
                if (!var.equals(variable) && !computedValues.containsKey(var)) {
                    try {
                        computeVariable(var, computedValues, steps, visited);
                    } catch (IllegalArgumentException e) {
                        LogUtils.w("Solver", "не удалось вычислить промежуточную величину " + var + ": " + e.getMessage());
                    }
                }
            }

            boolean canCompute = true;
            for (String var : variables) {
                if (!var.equals(variable) && !computedValues.containsKey(var)) {
                    canCompute = false;
                    break;
                }
            }

            if (canCompute) {
                Double[] values = new Double[variables.size()];
                for (int i = 0; i < variables.size(); i++) {
                    String var = variables.get(i);
                    values[i] = var.equals(variable) ? null : computedValues.get(var);
                }
                double result = formula.calculate(variable, values);
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    throw new IllegalArgumentException("недопустимый результат для " + variable + ": " + result);
                }
                computedValues.put(variable, result);
                steps.add(new Step(variable, result, formula));
                LogUtils.d("Solver", "вычислено: " + variable + " = " + result + " по формуле " + formula.getBaseExpression());
                visited.remove(variable);
                return result;
            }
        }

        throw new IllegalArgumentException("невозможно вычислить " + variable + ": недостаточно данных");
    }
}