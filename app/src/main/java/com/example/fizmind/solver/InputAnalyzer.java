package com.example.fizmind.solver;

import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * анализатор ввода для поиска подходящей формулы
 */
public class InputAnalyzer {
    private final FormulaDatabase formulaDatabase;

    /**
     * конструктор анализатора
     * @param formulaDatabase база данных формул
     */
    public InputAnalyzer(FormulaDatabase formulaDatabase) {
        this.formulaDatabase = formulaDatabase;
    }

    /**
     * находит подходящую формулу на основе известных величин и неизвестной
     * @param knownValues карта известных величин (ключ - обозначение, значение - величина)
     * @param unknown неизвестная величина
     * @return подходящая формула или null, если не найдено
     */
    public Formula findSuitableFormula(Map<String, Double> knownValues, String unknown) {
        List<Formula> suitableFormulas = new ArrayList<>();
        LogUtils.d("InputAnalyzer", "поиск формулы: известные величины = " + knownValues + ", неизвестная = " + unknown);

        for (Formula formula : formulaDatabase.getFormulas()) {
            List<String> variables = formula.getVariables();
            LogUtils.d("InputAnalyzer", "проверка формулы: " + formula.getExpression() + ", переменные = " + variables);
            if (variables.contains(unknown) &&
                    variables.stream().allMatch(var -> var.equals(unknown) || knownValues.containsKey(var))) {
                suitableFormulas.add(formula);
                LogUtils.d("InputAnalyzer", "формула подходит: " + formula.getExpression());
            } else {
                LogUtils.d("InputAnalyzer", "формула не подходит: " + formula.getExpression());
            }
        }

        if (suitableFormulas.isEmpty()) {
            LogUtils.w("InputAnalyzer", "подходящая формула не найдена");
            return null;
        }
        Formula selectedFormula = suitableFormulas.get(0);
        LogUtils.d("InputAnalyzer", "выбрана формула: " + selectedFormula.getExpression());
        return selectedFormula;
    }
}