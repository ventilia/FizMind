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
        // добавляем формулы с возможностью представления других величин
        formulas.add(new Formula("F_latin = m_latin * a_latin",
                Arrays.asList("F_latin", "m_latin", "a_latin"),
                values -> values[1] * values[2])); // F = m * a
        formulas.add(new Formula("m_latin = F_latin / a_latin",
                Arrays.asList("m_latin", "F_latin", "a_latin"),
                values -> values[1] / values[2])); // m = F / a
        formulas.add(new Formula("a_latin = F_latin / m_latin",
                Arrays.asList("a_latin", "F_latin", "m_latin"),
                values -> values[1] / values[2])); // a = F / m

        formulas.add(new Formula("v_latin = s_latin / designation_t",
                Arrays.asList("v_latin", "s_latin", "designation_t"),
                values -> values[1] / values[2])); // v = s / t
        formulas.add(new Formula("s_latin = v_latin * designation_t",
                Arrays.asList("s_latin", "v_latin", "designation_t"),
                values -> values[1] * values[2])); // s = v * t
        formulas.add(new Formula("designation_t = s_latin / v_latin",
                Arrays.asList("designation_t", "s_latin", "v_latin"),
                values -> values[1] / values[2])); // t = s / v

        formulas.add(new Formula("E_latin = m_latin * designation_g * h_latin",
                Arrays.asList("E_latin", "m_latin", "designation_g", "h_latin"),
                values -> values[1] * values[2] * values[3])); // E = m * g * h
        formulas.add(new Formula("m_latin = E_latin / (designation_g * h_latin)",
                Arrays.asList("m_latin", "E_latin", "designation_g", "h_latin"),
                values -> values[1] / (values[2] * values[3]))); // m = E / (g * h)
        formulas.add(new Formula("h_latin = E_latin / (m_latin * designation_g)",
                Arrays.asList("h_latin", "E_latin", "m_latin", "designation_g"),
                values -> values[1] / (values[2] * values[3]))); // h = E / (m * g)

        formulas.add(new Formula("P_latin = F_latin / S_latin",
                Arrays.asList("P_latin", "F_latin", "S_latin"),
                values -> values[1] / values[2])); // P = F / S
        formulas.add(new Formula("F_latin = P_latin * S_latin",
                Arrays.asList("F_latin", "P_latin", "S_latin"),
                values -> values[1] * values[2])); // F = P * S
        formulas.add(new Formula("S_latin = F_latin / P_latin",
                Arrays.asList("S_latin", "F_latin", "P_latin"),
                values -> values[1] / values[2])); // S = F / P

        formulas.add(new Formula("designation_W = E_latin / designation_t",
                Arrays.asList("designation_W", "E_latin", "designation_t"),
                values -> values[1] / values[2])); // W = E / t
        formulas.add(new Formula("E_latin = designation_W * designation_t",
                Arrays.asList("E_latin", "designation_W", "designation_t"),
                values -> values[1] * values[2])); // E = W * t
        formulas.add(new Formula("designation_t = E_latin / designation_W",
                Arrays.asList("designation_t", "E_latin", "designation_W"),
                values -> values[1] / values[2])); // t = E / W

        formulas.add(new Formula("designation_ρ = m_latin / designation_V",
                Arrays.asList("designation_ρ", "m_latin", "designation_V"),
                values -> values[1] / values[2])); // ρ = m / V
        formulas.add(new Formula("m_latin = designation_ρ * designation_V",
                Arrays.asList("m_latin", "designation_ρ", "designation_V"),
                values -> values[1] * values[2])); // m = ρ * V
        formulas.add(new Formula("designation_V = m_latin / designation_ρ",
                Arrays.asList("designation_V", "m_latin", "designation_ρ"),
                values -> values[1] / values[2])); // V = m / ρ

        formulas.add(new Formula("designation_I = U_latin / R_latin",
                Arrays.asList("designation_I", "U_latin", "R_latin"),
                values -> values[1] / values[2])); // I = U / R
        formulas.add(new Formula("U_latin = designation_I * R_latin",
                Arrays.asList("U_latin", "designation_I", "R_latin"),
                values -> values[1] * values[2])); // U = I * R
        formulas.add(new Formula("R_latin = U_latin / designation_I",
                Arrays.asList("R_latin", "U_latin", "designation_I"),
                values -> values[1] / values[2])); // R = U / I

        formulas.add(new Formula("designation_Φ = B_latin * S_latin",
                Arrays.asList("designation_Φ", "B_latin", "S_latin"),
                values -> values[1] * values[2])); // Φ = B * S
        formulas.add(new Formula("B_latin = designation_Φ / S_latin",
                Arrays.asList("B_latin", "designation_Φ", "S_latin"),
                values -> values[1] / values[2])); // B = Φ / S
        formulas.add(new Formula("S_latin = designation_Φ / B_latin",
                Arrays.asList("S_latin", "designation_Φ", "B_latin"),
                values -> values[1] / values[2])); // S = Φ / B
    }

    /**
     * возвращает список всех формул
     * @return список формул
     */
    public List<Formula> getFormulas() {
        return formulas;
    }
}