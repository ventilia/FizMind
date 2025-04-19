package com.example.fizmind.solver;

import com.example.fizmind.formulas.Formula;
import com.example.fizmind.utils.LogUtils;

import java.util.List;
import java.util.Map;

/**
 * решатель для вычисления неизвестной величины
 */
public class Solver {
    /**
     * вычисляет неизвестную величину на основе формулы
     * @param formula выбранная формула
     * @param knownValues карта известных величин
     * @param unknownDesignation обозначение неизвестной величины
     * @return значение неизвестной величины
     * @throws IllegalArgumentException если данных недостаточно или вычисление невозможно
     */
    public double solve(Formula formula, Map<String, Double> knownValues, String unknownDesignation) {
        List<String> variables = formula.getVariables();

        // проверка наличия всех необходимых данных, кроме неизвестной
        int nullCount = 0;
        for (String var : variables) {
            if (!knownValues.containsKey(var)) {
                if (var.equals(unknownDesignation)) {
                    nullCount++;
                } else {
                    throw new IllegalArgumentException("недостаточно данных для вычисления: отсутствует " + var);
                }
            }
        }
        if (nullCount != 1 || !variables.contains(unknownDesignation)) {
            throw new IllegalArgumentException("формула не подходит для вычисления " + unknownDesignation);
        }

        // подготовка массива значений для вычисления
        Double[] values = new Double[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            String var = variables.get(i);
            if (var.equals(unknownDesignation)) {
                values[i] = null; // неизвестная величина
            } else {
                values[i] = knownValues.get(var);
            }
        }

        // вычисление результата
        double result = formula.calculate(values);
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new IllegalArgumentException("результат вычисления недопустим: " + result);
        }

        LogUtils.d("Solver", "вычислено: " + unknownDesignation + " = " + result);
        return result;
    }
}