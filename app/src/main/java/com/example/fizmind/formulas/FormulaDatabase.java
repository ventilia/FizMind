package com.example.fizmind.formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * база данных физических формул
 */
public class FormulaDatabase {
    private final List<Formula> formulas;

    /**
     * конструктор базы данных, инициализирует список формул
     */
    public FormulaDatabase() {
        formulas = new ArrayList<>();
        // используем обозначения из PhysicalQuantityRegistry
        formulas.add(new Formula("F_latin = m_latin * a_latin",
                Arrays.asList("F_latin", "m_latin", "a_latin"),
                values -> values[1] * values[2])); // F = m * a
        formulas.add(new Formula("v_latin = s_latin / designation_t",
                Arrays.asList("v_latin", "s_latin", "designation_t"),
                values -> values[1] / values[2])); // v = s / t
        formulas.add(new Formula("E_latin = m_latin * designation_g * h_latin",
                Arrays.asList("E_latin", "m_latin", "designation_g", "h_latin"),
                values -> values[1] * values[2] * values[3])); // E = m * g * h
    }

    /**
     * возвращает список всех формул
     */
    public List<Formula> getFormulas() {
        return formulas;
    }
}