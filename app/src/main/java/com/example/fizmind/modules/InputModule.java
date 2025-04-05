package com.example.fizmind.modules;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.util.Log;

/**
 * Класс, представляющий модуль (нижний индекс).
 * Поддерживает ввод, удаление и отображение с учетом типа модуля.
 */
public class InputModule {
    private final ModuleType type;  // тип модуля (только SUBSCRIPT)
    private final StringBuilder content;  // содержимое модуля (числа)
    private boolean isActive;  // активен ли модуль (фокус на нем)

    public InputModule(ModuleType type) {
        if (type != ModuleType.SUBSCRIPT) {
            throw new IllegalArgumentException("поддерживается только тип SUBSCRIPT");
        }
        this.type = type;
        this.content = new StringBuilder();
        this.isActive = true;
        Log.d("InputModule", "создан модуль: " + type.getDescription());
    }

    /**
     * применяет ввод к модулю
     * @param input введенный символ
     * @return true, если ввод успешен, false — если символ недопустим
     */
    public boolean apply(String input) {
        if (!input.matches("[0-9]")) {
            Log.w("InputModule", "недопустимый символ для " + type.getDescription() + ": " + input);
            return false;
        }
        content.append(input);
        Log.d("InputModule", "добавлено в " + type.getDescription() + ": " + input);
        return true;
    }

    /**
     * удаляет последний символ из модуля
     * @return true, если модуль стал пустым после удаления, false — если еще есть содержимое
     */
    public boolean delete() {
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
            Log.d("InputModule", "удалён символ из " + type.getDescription() + ", осталось: " + content);
            return content.length() == 0;
        }
        return true;  // если уже пусто
    }

    /**
     * активирует модуль (установка фокуса)
     */
    public void activate() {
        isActive = true;
        Log.d("InputModule", "модуль активирован: " + type.getDescription());
    }

    /**
     * деактивирует модуль (снятие фокуса)
     */
    public void deactivate() {
        isActive = false;
        Log.d("InputModule", "модуль деактивирован: " + type.getDescription());
    }

    /**
     * проверяет, активен ли модуль
     * @return true, если модуль активен, false — если нет
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * проверяет, пуст ли модуль
     * @return true, если модуль не содержит цифр, false — если содержит
     */
    public boolean isEmpty() {
        return content.length() == 0;
    }

    /**
     * возвращает текстовое представление модуля для отображения
     * @return SpannableStringBuilder с примененными стилями
     */
    public SpannableStringBuilder getDisplayText() {
        SpannableStringBuilder result = new SpannableStringBuilder();
        if (content.length() > 0) {
            int start = result.length();
            result.append(content.toString());
            int end = result.length();
            result.setSpan(new SubscriptSpan(), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            result.setSpan(new RelativeSizeSpan(0.75f), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (isActive) {
            result.append(type.getSymbol());  // показываем символ модуля, если он активен и пуст
        }
        return result;
    }

    public ModuleType getType() {
        return type;
    }
}