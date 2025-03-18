package com.example.fizmind.modules;

/**
 * Интерфейс для модулей, таких как степень или нижний индекс.
 */
public interface Module {
    /**
     * Применяет модуль к базовому значению (числу или обозначению).
     * @param base базовое значение
     * @return строка с применённым модулем
     */
    String apply(String base);

    /**
     * Проверяет валидность применения модуля к базовому значению.
     * @param base базовое значение
     * @return true, если модуль применим, иначе false
     */
    boolean validate(String base);

    /**
     * Возвращает символ модуля (например, "^" или "_").
     * @return символ модуля
     */
    String getSymbol();

    /**
     * Указывает, применяется ли модуль к числу (true) или к обозначению (false).
     * @return true для чисел, false для обозначений
     */
    boolean appliesToValue();
}