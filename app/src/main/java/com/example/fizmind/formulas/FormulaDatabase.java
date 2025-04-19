package com.example.fizmind.formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * база данных физических формул с графом зависимостей
 */
public class FormulaDatabase {
    private final List<Formula> formulas;
    private final Map<String, List<Formula>> adjacencyList;

    /**
     * конструктор базы данных, инициализирует формулы и строит граф зависимостей
     */
    public FormulaDatabase() {
        formulas = new ArrayList<>();
        adjacencyList = new HashMap<>();

        // добавляем формулы для силы
        formulas.add(new Formula("F_latin = m_latin * a_latin",
                Arrays.asList("F_latin", "m_latin", "a_latin"),
                values -> values[1] * values[2])); // F = m * a
        formulas.add(new Formula("m_latin = F_latin / a_latin",
                Arrays.asList("m_latin", "F_latin", "a_latin"),
                values -> values[1] / values[2])); // m = F / a
        formulas.add(new Formula("a_latin = F_latin / m_latin",
                Arrays.asList("a_latin", "F_latin", "m_latin"),
                values -> values[1] / values[2])); // a = F / m

        // добавляем формулы для скорости
        formulas.add(new Formula("v_latin = s_latin / designation_t",
                Arrays.asList("v_latin", "s_latin", "designation_t"),
                values -> values[1] / values[2])); // v = s / t
        formulas.add(new Formula("s_latin = v_latin * designation_t",
                Arrays.asList("s_latin", "v_latin", "designation_t"),
                values -> values[1] * values[2])); // s = v * t
        formulas.add(new Formula("designation_t = s_latin / v_latin",
                Arrays.asList("designation_t", "s_latin", "v_latin"),
                values -> values[1] / values[2])); // t = s / v

        // добавляем формулы для энергии
        formulas.add(new Formula("E_latin = m_latin * designation_g * h_latin",
                Arrays.asList("E_latin", "m_latin", "designation_g", "h_latin"),
                values -> values[1] * values[2] * values[3])); // E = m * g * h
        formulas.add(new Formula("m_latin = E_latin / (designation_g * h_latin)",
                Arrays.asList("m_latin", "E_latin", "designation_g", "h_latin"),
                values -> values[1] / (values[2] * values[3]))); // m = E / (g * h)
        formulas.add(new Formula("h_latin = E_latin / (m_latin * designation_g)",
                Arrays.asList("h_latin", "E_latin", "m_latin", "designation_g"),
                values -> values[1] / (values[2] * values[3]))); // h = E / (m * g)

        // строим граф зависимостей
        for (Formula formula : formulas) {
            for (String variable : formula.getVariables()) {
                adjacencyList.computeIfAbsent(variable, k -> new ArrayList<>()).add(formula);
            }
        }
    }

    /**
     * возвращает список всех формул
     * @return список формул
     */
    public List<Formula> getFormulas() {
        return formulas;
    }

    /**
     * возвращает граф зависимостей
     * @return карта зависимостей между величинами и формулами
     */
    public Map<String, List<Formula>> getAdjacencyList() {
        return adjacencyList;
    }
}