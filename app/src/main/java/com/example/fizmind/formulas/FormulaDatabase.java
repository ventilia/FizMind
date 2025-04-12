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
        // добавляем примеры формул
        formulas.add(new Formula("F = m * a", Arrays.asList("F", "m", "a"),
                values -> values[1] * values[2])); // F = m * a
        formulas.add(new Formula("v = s / t", Arrays.asList("v", "s", "t"),
                values -> values[1] / values[2])); // v = s / t
        formulas.add(new Formula("E = m * g * h", Arrays.asList("E", "m", "g", "h"),
                values -> values[1] * values[2] * values[3])); // E = m * g * h
    }

    /**
     * возвращает список всех формул
     */
    public List<Formula> getFormulas() {
        return formulas;
    }
}