package com.example.fizmind.modules;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;

/**
 * Класс, представляющий модуль (степень, нижний индекс и т.д.).
 * Поддерживает ввод, удаление и отображение с учетом типа модуля.
 */
public class InputModule {
    private final ModuleType type;  // Тип модуля (степень или нижний индекс)
    private final StringBuilder content;  // Содержимое модуля (числа)
    private boolean isActive;  // Активен ли модуль (фокус на нем)

    public InputModule(ModuleType type) {
        this.type = type;
        this.content = new StringBuilder();
        this.isActive = true;
        Log.d("InputModule", "Создан модуль: " + type.getDescription());
    }

    /**
     * Применяет ввод к модулю.
     * @param input Введенный символ.
     * @return true, если ввод успешен, false — если символ недопустим.
     */
    public boolean apply(String input) {
        if (!input.matches("[0-9]")) {
            Log.w("InputModule", "Недопустимый символ для " + type.getDescription() + ": " + input);
            return false;
        }
        content.append(input);
        Log.d("InputModule", "Добавлено в " + type.getDescription() + ": " + input);
        return true;
    }

    /**
     * Удаляет последний символ из модуля.
     * @return true, если модуль стал пустым после удаления, false — если еще есть содержимое.
     */
    public boolean delete() {
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
            Log.d("InputModule", "Удалён символ из " + type.getDescription() + ", осталось: " + content);
            return content.length() == 0;
        }
        return true;  // Если уже пусто
    }

    /**
     * Проверяет, активен ли модуль.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Активирует модуль (установка фокуса).
     */
    public void activate() {
        isActive = true;
        Log.d("InputModule", "Модуль активирован: " + type.getDescription());
    }

    /**
     * Деактивирует модуль (снятие фокуса).
     */
    public void deactivate() {
        isActive = false;
        Log.d("InputModule", "Модуль " + type.getDescription() + " деактивирован");
    }

    /**
     * Возвращает текстовое представление модуля для отображения.
     */
    public SpannableStringBuilder getDisplayText() {
        SpannableStringBuilder result = new SpannableStringBuilder();
        if (content.length() > 0) {
            int start = result.length();
            result.append(content.toString());
            int end = result.length();

            // Применяем стили в зависимости от типа модуля
            if (type == ModuleType.EXPONENT) {
                result.setSpan(new SuperscriptSpan(), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                result.setSpan(new RelativeSizeSpan(0.75f), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type == ModuleType.SUBSCRIPT) {
                result.setSpan(new SubscriptSpan(), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                result.setSpan(new RelativeSizeSpan(0.75f), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else if (isActive) {
            result.append(type.getSymbol());  // Показываем символ модуля, если он активен и пуст
        }
        return result;
    }

    /**
     * Проверяет, пуст ли модуль.
     * @return true, если модуль не содержит цифр, false — если содержит.
     */
    public boolean isEmpty() {
        return content.length() == 0;
    }

    public ModuleType getType() {
        return type;
    }
}