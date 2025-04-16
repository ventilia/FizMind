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
     * вычисляет неизвестную величину
     * @param formula выбранная формула
     * @param knownValues карта известных величин
     * @param unknownDesignation обозначение неизвестной
     * @return значение неизвестной величины
     * @throws IllegalArgumentException если данных недостаточно
     */
    public double solve(Formula formula, Map<String, Double> knownValues, String unknownDesignation) {
        List<String> variables = formula.getVariables();

        // проверка наличия всех необходимых данных
        for (String var : variables) {
            if (!var.equals(unknownDesignation) && !knownValues.containsKey(var)) {
                throw new IllegalArgumentException("недостаточно данных для вычисления: отсутствует " + var);
            }
        }

        // подготовка значений для вычисления
        Double[] values = new Double[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            String var = variables.get(i);
            if (var.equals(unknownDesignation)) {
                values[i] = null; // неизвестная
            } else {
                values[i] = knownValues.get(var);
            }
        }

        // вычисление результата
        double result = formula.calculate(values);
        LogUtils.d("Solver", "вычислено: " + unknownDesignation + " = " + result);
        return result;
    }
}