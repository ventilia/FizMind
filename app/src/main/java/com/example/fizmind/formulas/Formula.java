package com.example.fizmind.formulas;

import java.util.List;
import java.util.function.Function;

/**
 * класс, представляющий физическую формулу
 */
public class Formula {
    private final String expression; // текстовое представление формулы, например "F = m * a"
    private final List<String> variables; // список переменных в формуле
    private final Function<Double[], Double> calculate; // функция для вычисления

    /**
     * конструктор формулы
     * @param expression строка с выражением формулы
     * @param variables список переменных
     * @param calculate функция для вычисления результата
     */
    public Formula(String expression, List<String> variables, Function<Double[], Double> calculate) {
        this.expression = expression;
        this.variables = variables;
        this.calculate = calculate;
    }

    /**
     * возвращает текстовое представление формулы
     */
    public String getExpression() {
        return expression;
    }

    /**
     * возвращает список переменных
     */
    public List<String> getVariables() {
        return variables;
    }

    /**
     * вычисляет результат по формуле
     * @param values массив значений переменных
     * @return результат вычисления
     */
    public Double calculate(Double[] values) {
        return calculate.apply(values);
    }
}