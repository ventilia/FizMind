package com.example.fizmind.formulas;

import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class Formula {
    private final String baseExpression; // базовое выражение, например "F_latin = m_latin * a_latin"
    private final List<String> variables; // список переменных в формуле
    private final Map<String, Function<Double[], Double>> calculators; // карта функций для вычисления каждой переменной
    private final Map<String, String> expressions; // карта выражений для вычисления каждой переменной


  // baseExpression  выражение формулы
     // variables список переменных
  //calculators карта функций для вычисления
    // expressions карта выражений для каждой переменной

    public Formula(String baseExpression, List<String> variables,
                   Map<String, Function<Double[], Double>> calculators,
                   Map<String, String> expressions) {
        this.baseExpression = baseExpression;
        this.variables = variables;
        this.calculators = calculators;
        this.expressions = expressions;
    }


    public String getBaseExpression() {
        return baseExpression;
    }


    public List<String> getVariables() {
        return variables;
    }


    public Double calculate(String targetVariable, Double[] values) {
        Function<Double[], Double> calculator = calculators.get(targetVariable);
        if (calculator == null) {
            throw new IllegalArgumentException("Нет функции для вычисления " + targetVariable);
        }
        return calculator.apply(values);
    }


    public String getExpressionFor(String targetVariable) {
        return expressions.getOrDefault(targetVariable, baseExpression);
    }


    public String getDisplayExpression(String targetVariable) {
        String expression = getExpressionFor(targetVariable);

        if (expression.contains("/")) {
            String[] parts = expression.split("=");
            if (parts.length == 2) {
                String left = parts[0].trim();
                String right = parts[1].trim();
                String[] fraction = right.split("/");
                if (fraction.length == 2) {
                    // форматируем как дробь с переносами строк
                    return left + " = \n" + fraction[0].trim() + "\n—\n" + fraction[1].trim();
                }
            }
        }
        return expression;
    }
}