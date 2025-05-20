package com.example.fizmind.modules;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;

public class InputModule {
    private final ModuleType type;
    private final StringBuilder content;
    private boolean isActive;
    private final boolean isFixed; // флаг для фиксированных модулей "p" и "k"

    // конструктор с инициализацией флага isFixed
    public InputModule(ModuleType type) {
        this.type = type;
        this.content = new StringBuilder();
        this.isActive = false;
        this.isFixed = (type == ModuleType.SUBSCRIPT_P || type == ModuleType.SUBSCRIPT_K);
    }

    // применение ввода с учетом фиксированных модулей
    public boolean apply(String input) {
        if (isFixed) {
            // для фиксированных модулей разрешаем только первый символ
            if (content.length() == 0 && (input.equals("p") || input.equals("k"))) {
                content.append(input);
                return true;
            }
            return false; // блокируем дальнейший ввод
        } else if (type == ModuleType.SUBSCRIPT && input.matches("[a-zA-Z0-9]")) {
            // для обычного подстрочного индекса разрешаем буквы и цифры
            content.append(input);
            return true;
        }
        return false;
    }

    // удаление символа
    public boolean deleteChar() {
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
            return content.length() == 0; // возвращаем true, если модуль стал пустым
        }
        return true; // модуль уже пуст
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

    // получение отображаемого текста с форматированием
    public SpannableStringBuilder getDisplayText() {
        SpannableStringBuilder result = new SpannableStringBuilder(content.toString());
        result.setSpan(new SubscriptSpan(), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setSpan(new RelativeSizeSpan(0.75f), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }
}