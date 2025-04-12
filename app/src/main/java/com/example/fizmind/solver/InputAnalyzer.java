package com.example.fizmind.solver;

import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        for (Formula formula : formulaDatabase.getFormulas()) {
            List<String> variables = formula.getVariables();
            if (variables.contains(unknown) &&
                    variables.stream().allMatch(var -> var.equals(unknown) || knownValues.containsKey(var))) {
                suitableFormulas.add(formula);
            }
        }

        // возвращаем первую подходящую формулу (пока реализуем только одно решение)
        return suitableFormulas.isEmpty() ? null : suitableFormulas.get(0);
    }
}