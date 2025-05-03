package com.example.fizmind.modules;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;

// модуль для обработки индексов
public class InputModule {
    private final ModuleType type;
    private final StringBuilder content;
    private boolean isActive;

    // конструктор
    public InputModule(ModuleType type) {
        this.type = type;
        this.content = new StringBuilder();
        this.isActive = false;
    }

    // применение ввода
    public boolean apply(String input) {
        if (type == ModuleType.SUBSCRIPT && input.matches("[a-zA-Z0-9]")) {
            content.append(input);
            return true;
        } else if ((type == ModuleType.SUBSCRIPT_P && "p".equals(input)) || (type == ModuleType.SUBSCRIPT_K && "k".equals(input))) {
            content.append(input);
            return true;
        }
        return false;
    }

    // удаление символа
    public boolean deleteChar() {
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
            return content.length() == 0;
        }
        return true;
    }

    // удаление всего модуля
    public void deleteEntire() {
        content.setLength(0);
        isActive = false;
    }

    // активация модуля
    public void activate() {
        isActive = true;
    }

    // деактивация модуля
    public void deactivate() {
        isActive = false;
    }

    // проверка, пуст ли модуль
    public boolean isEmpty() {
        return content.length() == 0;
    }

    // проверка активности
    public boolean isActive() {
        return isActive;
    }

    // получение типа модуля
    public ModuleType getType() {
        return type;
    }

    // получение содержимого
    public String getContent() {
        return content.toString();
    }

    // получение отображаемого текста
    public SpannableStringBuilder getDisplayText() {
        SpannableStringBuilder result = new SpannableStringBuilder(content.toString());
        result.setSpan(new SubscriptSpan(), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setSpan(new RelativeSizeSpan(0.75f), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }
}