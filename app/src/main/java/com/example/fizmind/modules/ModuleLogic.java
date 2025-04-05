package com.example.fizmind.modules;

import android.util.Log;
import com.example.fizmind.SIConverter;

/**
 * класс для обработки модулей, в частности степени
 */
public class ModuleLogic {

    /**
     * применяет модуль (степень) к значению после конвертации в си
     *
     * @param value        значение в си
     * @param exponent     степень
     * @param unit         единица измерения в си
     * @return массив: [итоговое значение, итоговая единица, шаги применения модуля]
     */
    public static Object[] applyExponent(double value, String exponent, String unit) {
        // если степень не указана, возвращаем исходные данные без изменений
        if (exponent == null || exponent.isEmpty()) {
            return new Object[]{value, unit, ""};
        }

        try {
            int exp = Integer.parseInt(exponent);
            double resultValue = Math.pow(value, exp);
            String resultUnit = unit + "^" + exp;
            String steps = String.format("(%s %s)^%d = %s %s",
                    SIConverter.formatValue(value), unit, exp,
                    SIConverter.formatValue(resultValue), resultUnit);
            Log.d("ModuleLogic", "применена степень: " + steps);
            return new Object[]{resultValue, resultUnit, steps};
        } catch (NumberFormatException e) {
            Log.e("ModuleLogic", "неверный формат степени: " + exponent, e);
            return new Object[]{value, unit, "ошибка: неверная степень"};
        }
    }
}