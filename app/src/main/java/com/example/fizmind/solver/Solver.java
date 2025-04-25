package com.example.fizmind.solver;

import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Solver {
    private final FormulaDatabase formulaDatabase;
    private final InputAnalyzer inputAnalyzer;


    public Solver(FormulaDatabase formulaDatabase) {
        this.formulaDatabase = formulaDatabase;
        this.inputAnalyzer = new InputAnalyzer(formulaDatabase);
    }


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

    /**
     * шаг вычисления
     */
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

    public SolutionResult solve(Map<String, Double> knownValues, String unknownDesignation) {
        Map<String, Double> computedValues = new HashMap<>(knownValues);
        List<Step> steps = new ArrayList<>();
        Set<String> visited = new HashSet<>(); // набор для отслеживания посещенных переменных
        double result = computeVariable(unknownDesignation, computedValues, steps, visited);
        return new SolutionResult(result, steps);
    }

    /**
     * рекурсивно вычисляет величину, находя промежуточные значения
     * @param variable целевая переменная
     * @param computedValues карта вычисленных величин
     * @param steps список шагов вычисления
     * @param visited набор посещенных переменных для предотвращения циклов
     * @return значение переменной
     * @throws IllegalArgumentException если вычисление невозможно
     */
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
                visited.remove(variable); // удаляем из visited после успешного вычисления
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
                visited.remove(variable); // удаляем из visited после успешного вычисления
                return result;
            }
        }

        throw new IllegalArgumentException("невозможно вычислить " + variable + ": недостаточно данных");
    }
}