package com.example.fizmind.solver;

import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class InputAnalyzer {
    private final FormulaDatabase formulaDatabase;


    public InputAnalyzer(FormulaDatabase formulaDatabase) {
        this.formulaDatabase = formulaDatabase;
    }

    /**
     * находит цепочку формул для вычисления неизвестной величины
     * @param knownValues известные величины и их значения
     * @param unknown неизвестная величина
     * @return список формул, представляющих путь решения, или null, если путь не найден
     */
    public List<Formula> findFormulaPath(Map<String, Double> knownValues, String unknown) {
        Map<String, List<Formula>> adjacencyList = formulaDatabase.getAdjacencyList();
        Set<String> visited = new HashSet<>(knownValues.keySet());
        Deque<String> queue = new ArrayDeque<>(knownValues.keySet());
        Map<String, Formula> parentFormula = new HashMap<>();
        Map<String, String> parentVariable = new HashMap<>();

        while (!queue.isEmpty()) {
            String currentVar = queue.poll();

            if (currentVar.equals(unknown)) {
                List<Formula> path = reconstructPath(parentFormula, parentVariable, unknown);
                LogUtils.d("InputAnalyzer", "найден путь к " + unknown + ": " + path);
                return path;
            }

            List<Formula> relatedFormulas = adjacencyList.getOrDefault(currentVar, new ArrayList<>());
            for (Formula formula : relatedFormulas) {
                for (String nextVar : formula.getVariables()) {
                    if (!visited.contains(nextVar)) {
                        visited.add(nextVar);
                        queue.add(nextVar);
                        parentFormula.put(nextVar, formula);
                        parentVariable.put(nextVar, currentVar);
                    }
                }
            }
        }

        LogUtils.w("InputAnalyzer", "не удалось найти путь к " + unknown);
        return null;
    }

    /**
     * воссоздает путь решения из родительских формул
     * @param parentFormula карта формул для каждой величины
     * @param parentVariable карта предыдущих величин
     * @param unknown целевая величина
     * @return список формул, представляющих решение
     */
    private List<Formula> reconstructPath(Map<String, Formula> parentFormula, Map<String, String> parentVariable, String unknown) {
        List<Formula> path = new ArrayList<>();
        String currentVar = unknown;

        while (parentFormula.containsKey(currentVar)) {
            Formula formula = parentFormula.get(currentVar);
            path.add(0, formula);
            currentVar = parentVariable.get(currentVar);
        }

        return path;
    }
}