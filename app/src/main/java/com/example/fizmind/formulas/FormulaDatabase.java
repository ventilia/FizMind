package com.example.fizmind.formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FormulaDatabase {
    private final List<Formula> formulas;
    private final Map<String, List<Formula>> adjacencyList;

    public FormulaDatabase() {
        formulas = new ArrayList<>();
        adjacencyList = new HashMap<>();

        // 1. скорость: v = s / t
        Map<String, Function<Double[], Double>> vstCalculators = new HashMap<>();
        vstCalculators.put("v_latin", values -> values[1] / values[2]); // v = s / t
        vstCalculators.put("s_latin", values -> values[0] * values[2]); // s = v * t
        vstCalculators.put("designation_t", values -> values[1] / values[0]); // t = s / v
        Map<String, String> vstExpressions = new HashMap<>();
        vstExpressions.put("v_latin", "v_latin = s_latin / designation_t");
        vstExpressions.put("s_latin", "s_latin = v_latin * designation_t");
        vstExpressions.put("designation_t", "designation_t = s_latin / v_latin");
        Formula vst = new Formula("v_latin = s_latin / designation_t",
                Arrays.asList("v_latin", "s_latin", "designation_t"), vstCalculators, vstExpressions);
        formulas.add(vst);

        // 2. ускорение: a = v / t
        Map<String, Function<Double[], Double>> avtCalculators = new HashMap<>();
        avtCalculators.put("a_latin", values -> values[1] / values[2]); // a = v / t
        avtCalculators.put("v_latin", values -> values[0] * values[2]); // v = a * t
        avtCalculators.put("designation_t", values -> values[1] / values[0]); // t = v / a
        Map<String, String> avtExpressions = new HashMap<>();
        avtExpressions.put("a_latin", "a_latin = v_latin / designation_t");
        avtExpressions.put("v_latin", "v_latin = a_latin * designation_t");
        avtExpressions.put("designation_t", "designation_t = v_latin / a_latin");
        Formula avt = new Formula("a_latin = v_latin / designation_t",
                Arrays.asList("a_latin", "v_latin", "designation_t"), avtCalculators, avtExpressions);
        formulas.add(avt);

        // 3. сила: F = m * a
        Map<String, Function<Double[], Double>> fmaCalculators = new HashMap<>();
        fmaCalculators.put("F_latin", values -> values[1] * values[2]); // F = m * a
        fmaCalculators.put("m_latin", values -> values[0] / values[2]); // m = F / a
        fmaCalculators.put("a_latin", values -> values[0] / values[1]); // a = F / m
        Map<String, String> fmaExpressions = new HashMap<>();
        fmaExpressions.put("F_latin", "F_latin = m_latin * a_latin");
        fmaExpressions.put("m_latin", "m_latin = F_latin / a_latin");
        fmaExpressions.put("a_latin", "a_latin = F_latin / m_latin");
        Formula fma = new Formula("F_latin = m_latin * a_latin",
                Arrays.asList("F_latin", "m_latin", "a_latin"), fmaCalculators, fmaExpressions);
        formulas.add(fma);

        // 4. вес: P = m * g
        Map<String, Function<Double[], Double>> pmgCalculators = new HashMap<>();
        pmgCalculators.put("designation_P", values -> values[1] * values[2]); // P = m * g
        pmgCalculators.put("m_latin", values -> values[0] / values[2]); // m = P / g
        pmgCalculators.put("designation_g", values -> values[0] / values[1]); // g = P / m
        Map<String, String> pmgExpressions = new HashMap<>();
        pmgExpressions.put("designation_P", "designation_P = m_latin * designation_g");
        pmgExpressions.put("m_latin", "m_latin = designation_P / designation_g");
        pmgExpressions.put("designation_g", "designation_g = designation_P / m_latin");
        Formula pmg = new Formula("designation_P = m_latin * designation_g",
                Arrays.asList("designation_P", "m_latin", "designation_g"), pmgCalculators, pmgExpressions);
        formulas.add(pmg);

        // 5. плотность: ρ = m / V
        Map<String, Function<Double[], Double>> rhoMVCalculators = new HashMap<>();
        rhoMVCalculators.put("designation_ρ", values -> values[1] / values[2]); // ρ = m / V
        rhoMVCalculators.put("m_latin", values -> values[0] * values[2]); // m = ρ * V
        rhoMVCalculators.put("designation_V", values -> values[1] / values[0]); // V = m / ρ
        Map<String, String> rhoMVExpressions = new HashMap<>();
        rhoMVExpressions.put("designation_ρ", "designation_ρ = m_latin / designation_V");
        rhoMVExpressions.put("m_latin", "m_latin = designation_ρ * designation_V");
        rhoMVExpressions.put("designation_V", "designation_V = m_latin / designation_ρ");
        Formula rhoMV = new Formula("designation_ρ = m_latin / designation_V",
                Arrays.asList("designation_ρ", "m_latin", "designation_V"), rhoMVCalculators, rhoMVExpressions);
        formulas.add(rhoMV);

        // 6. давление: p = F / S
        Map<String, Function<Double[], Double>> pfsCalculators = new HashMap<>();
        pfsCalculators.put("designation_p", values -> values[1] / values[2]); // p = F / S
        pfsCalculators.put("F_latin", values -> values[0] * values[2]); // F = p * S
        pfsCalculators.put("S_latin", values -> values[1] / values[0]); // S = F / p
        Map<String, String> pfsExpressions = new HashMap<>();
        pfsExpressions.put("designation_p", "designation_p = F_latin / S_latin");
        pfsExpressions.put("F_latin", "F_latin = designation_p * S_latin");
        pfsExpressions.put("S_latin", "S_latin = F_latin / designation_p");
        Formula pfs = new Formula("designation_p = F_latin / S_latin",
                Arrays.asList("designation_p", "F_latin", "S_latin"), pfsCalculators, pfsExpressions);
        formulas.add(pfs);

        // 7. работа: A = F * s
        Map<String, Function<Double[], Double>> afsCalculators = new HashMap<>();
        afsCalculators.put("designation_A", values -> values[1] * values[2]); // A = F * s
        afsCalculators.put("F_latin", values -> values[0] / values[2]); // F = A / s
        afsCalculators.put("s_latin", values -> values[0] / values[1]); // s = A / F
        Map<String, String> afsExpressions = new HashMap<>();
        afsExpressions.put("designation_A", "designation_A = F_latin * s_latin");
        afsExpressions.put("F_latin", "F_latin = designation_A / s_latin");
        afsExpressions.put("s_latin", "s_latin = designation_A / F_latin");
        Formula afs = new Formula("designation_A = F_latin * s_latin",
                Arrays.asList("designation_A", "F_latin", "s_latin"), afsCalculators, afsExpressions);
        formulas.add(afs);

        // 8. мощность: N = A / t
        Map<String, Function<Double[], Double>> natCalculators = new HashMap<>();
        natCalculators.put("designation_N", values -> values[1] / values[2]); // N = A / t
        natCalculators.put("designation_A", values -> values[0] * values[2]); // A = N * t
        natCalculators.put("designation_t", values -> values[1] / values[0]); // t = A / N
        Map<String, String> natExpressions = new HashMap<>();
        natExpressions.put("designation_N", "designation_N = designation_A / designation_t");
        natExpressions.put("designation_A", "designation_A = designation_N * designation_t");
        natExpressions.put("designation_t", "designation_t = designation_A / designation_N");
        Formula nat = new Formula("designation_N = designation_A / designation_t",
                Arrays.asList("designation_N", "designation_A", "designation_t"), natCalculators, natExpressions);
        formulas.add(nat);

        // 9. кинетическая энергия: Eₖ = (m * v²) / 2
        Map<String, Function<Double[], Double>> ekMVCalculators = new HashMap<>();
        ekMVCalculators.put("E_latin_k", values -> (values[1] * Math.pow(values[2], 2)) / 2); // Eₖ = m * v² / 2
        ekMVCalculators.put("m_latin", values -> (2 * values[0]) / Math.pow(values[2], 2)); // m = 2 * Eₖ / v²
        ekMVCalculators.put("v_latin", values -> Math.sqrt((2 * values[0]) / values[1])); // v = √(2 * Eₖ / m)
        Map<String, String> ekMVExpressions = new HashMap<>();
        ekMVExpressions.put("E_latin_k", "E_latin_k = (m_latin * v_latin²) / 2");
        ekMVExpressions.put("m_latin", "m_latin = (2 * E_latin_k) / v_latin²");
        ekMVExpressions.put("v_latin", "v_latin = √((2 * E_latin_k) / m_latin)");
        Formula ekMV = new Formula("E_latin_k = (m_latin * v_latin²) / 2",
                Arrays.asList("E_latin_k", "m_latin", "v_latin"), ekMVCalculators, ekMVExpressions);
        formulas.add(ekMV);

        // 10. потенциальная энергия: Eₚ = m * g * h
        Map<String, Function<Double[], Double>> epMGHCalculators = new HashMap<>();
        epMGHCalculators.put("E_latin_p", values -> values[1] * values[2] * values[3]); // Eₚ = m * g * h
        epMGHCalculators.put("m_latin", values -> values[0] / (values[2] * values[3])); // m = Eₚ / (g * h)
        epMGHCalculators.put("designation_g", values -> values[0] / (values[1] * values[3])); // g = Eₚ / (m * h)
        epMGHCalculators.put("h_latin", values -> values[0] / (values[1] * values[2])); // h = Eₚ / (m * g)
        Map<String, String> epMGHExpressions = new HashMap<>();
        epMGHExpressions.put("E_latin_p", "E_latin_p = m_latin * designation_g * h_latin");
        epMGHExpressions.put("m_latin", "m_latin = E_latin_p / (designation_g * h_latin)");
        epMGHExpressions.put("designation_g", "designation_g = E_latin_p / (m_latin * h_latin)");
        epMGHExpressions.put("h_latin", "h_latin = E_latin_p / (m_latin * designation_g)");
        Formula epMGH = new Formula("E_latin_p = m_latin * designation_g * h_latin",
                Arrays.asList("E_latin_p", "m_latin", "designation_g", "h_latin"), epMGHCalculators, epMGHExpressions);
        formulas.add(epMGH);

        // 11. количество теплоты: Q = c * m * ΔT
        Map<String, Function<Double[], Double>> qcmTCalculators = new HashMap<>();
        qcmTCalculators.put("designation_Q", values -> values[1] * values[2] * values[3]); // Q = c * m * ΔT
        qcmTCalculators.put("c_latin", values -> values[0] / (values[2] * values[3])); // c = Q / (m * ΔT)
        qcmTCalculators.put("m_latin", values -> values[0] / (values[1] * values[3])); // m = Q / (c * ΔT)
        qcmTCalculators.put("designation_T", values -> values[0] / (values[1] * values[2])); // ΔT = Q / (c * m)
        Map<String, String> qcmTExpressions = new HashMap<>();
        qcmTExpressions.put("designation_Q", "designation_Q = c_latin * m_latin * designation_T");
        qcmTExpressions.put("c_latin", "c_latin = designation_Q / (m_latin * designation_T)");
        qcmTExpressions.put("m_latin", "m_latin = designation_Q / (c_latin * designation_T)");
        qcmTExpressions.put("designation_T", "designation_T = designation_Q / (c_latin * m_latin)");
        Formula qcmT = new Formula("designation_Q = c_latin * m_latin * designation_T",
                Arrays.asList("designation_Q", "c_latin", "m_latin", "designation_T"), qcmTCalculators, qcmTExpressions);
        formulas.add(qcmT);

        // 12. закон Ома: I = U / R
        Map<String, Function<Double[], Double>> iurCalculators = new HashMap<>();
        iurCalculators.put("designation_I", values -> values[1] / values[2]); // I = U / R
        iurCalculators.put("U_latin", values -> values[0] * values[2]); // U = I * R
        iurCalculators.put("R_latin", values -> values[1] / values[0]); // R = U / I
        Map<String, String> iurExpressions = new HashMap<>();
        iurExpressions.put("designation_I", "designation_I = U_latin / R_latin");
        iurExpressions.put("U_latin", "U_latin = designation_I * R_latin");
        iurExpressions.put("R_latin", "R_latin = U_latin / designation_I");
        Formula iur = new Formula("designation_I = U_latin / R_latin",
                Arrays.asList("designation_I", "U_latin", "R_latin"), iurCalculators, iurExpressions);
        formulas.add(iur);

        // 13. мощность электрического тока: P = I * U
        Map<String, Function<Double[], Double>> piuCalculators = new HashMap<>();
        piuCalculators.put("P_power", values -> values[1] * values[2]); // P = I * U
        piuCalculators.put("designation_I", values -> values[0] / values[2]); // I = P / U
        piuCalculators.put("U_latin", values -> values[0] / values[1]); // U = P / I
        Map<String, String> piuExpressions = new HashMap<>();
        piuExpressions.put("P_power", "P_power = designation_I * U_latin");
        piuExpressions.put("designation_I", "designation_I = P_power / U_latin");
        piuExpressions.put("U_latin", "U_latin = P_power / designation_I");
        Formula piu = new Formula("P_power = designation_I * U_latin",
                Arrays.asList("P_power", "designation_I", "U_latin"), piuCalculators, piuExpressions);
        formulas.add(piu);

        // 14. скорость света: c = f * λ
        Map<String, Function<Double[], Double>> cflCalculators = new HashMap<>();
        cflCalculators.put("c_latin", values -> values[1] * values[2]); // c = f * λ
        cflCalculators.put("designation_f", values -> values[0] / values[2]); // f = c / λ
        cflCalculators.put("designation_λ", values -> values[0] / values[1]); // λ = c / f
        Map<String, String> cflExpressions = new HashMap<>();
        cflExpressions.put("c_latin", "c_latin = designation_f * designation_λ");
        cflExpressions.put("designation_f", "designation_f = c_latin / designation_λ");
        cflExpressions.put("designation_λ", "designation_λ = c_latin / designation_f");
        Formula cfl = new Formula("c_latin = designation_f * designation_λ",
                Arrays.asList("c_latin", "designation_f", "designation_λ"), cflCalculators, cflExpressions);
        formulas.add(cfl);

        // 15. общая энергия: E = m * c²
        Map<String, Function<Double[], Double>> emcCalculators = new HashMap<>();
        emcCalculators.put("E_latin", values -> values[1] * Math.pow(values[2], 2)); // E = m * c²
        emcCalculators.put("m_latin", values -> values[0] / Math.pow(values[2], 2)); // m = E / c²
        emcCalculators.put("c_latin", values -> Math.sqrt(values[0] / values[1])); // c = √(E / m)
        Map<String, String> emcExpressions = new HashMap<>();
        emcExpressions.put("E_latin", "E_latin = m_latin * c_latin²");
        emcExpressions.put("m_latin", "m_latin = E_latin / c_latin²");
        emcExpressions.put("c_latin", "c_latin = √(E_latin / m_latin)");
        Formula emc = new Formula("E_latin = m_latin * c_latin²",
                Arrays.asList("E_latin", "m_latin", "c_latin"), emcCalculators, emcExpressions);
        formulas.add(emc);

        //  граф зависимостей
        for (Formula formula : formulas) {
            for (String variable : formula.getVariables()) {
                adjacencyList.computeIfAbsent(variable, k -> new ArrayList<>()).add(formula);
            }
        }
    }

    public List<Formula> getFormulas() {
        return formulas;
    }

    public Map<String, List<Formula>> getAdjacencyList() {
        return adjacencyList;
    }
}