package com.example.fizmind.modules;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import com.example.fizmind.utils.LogUtils;


public class InputModule {
    private final ModuleType type;
    private final StringBuilder content;
    private boolean isActive;

    public InputModule(ModuleType type) {
        // проверка поддерживаемых типов модулей
        if (type != ModuleType.SUBSCRIPT && type != ModuleType.SUBSCRIPT_P && type != ModuleType.SUBSCRIPT_K) {
            throw new IllegalArgumentException("поддерживаются только типы SUBSCRIPT, SUBSCRIPT_P и SUBSCRIPT_K");
        }
        this.type = type;
        this.content = new StringBuilder();
        this.isActive = true;

        // для SUBSCRIPT_P и SUBSCRIPT_K  добавляем  символ
        if (type == ModuleType.SUBSCRIPT_P) {
            content.append("p");
        } else if (type == ModuleType.SUBSCRIPT_K) {
            content.append("k");
        }

        LogUtils.logModuleCreated("InputModule", type);
    }


    public boolean apply(String input) {
        if (type == ModuleType.SUBSCRIPT_P || type == ModuleType.SUBSCRIPT_K) {
            LogUtils.w("InputModule", "модули 'p' и 'k' не поддерживают дополнительный ввод");
            return false;
        }
        if (!input.matches("[a-zA-Z0-9]")) {
            LogUtils.w("InputModule", "недопустимый символ для " + type.getDescription() + ": " + input);
            return false;
        }
        content.append(input);
        LogUtils.logSymbolAdded("InputModule", type, input);
        return true;
    }

    public boolean deleteChar() {
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
            LogUtils.logSymbolDeleted("InputModule", type, content.toString());
            return content.length() == 0;
        }
        return true;
    }


    public void deleteEntire() {
        content.setLength(0);
        LogUtils.logSymbolDeleted("InputModule", type, "модуль удален целиком");
    }


    public void activate() {
        isActive = true;
        LogUtils.logModuleActivated("InputModule", type);
    }


    public void deactivate() {
        isActive = false;
        LogUtils.logModuleDeactivated("InputModule", type);
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isEmpty() {
        return content.length() == 0;
    }


    public SpannableStringBuilder getDisplayText() {
        SpannableStringBuilder result = new SpannableStringBuilder();
        if (content.length() > 0) {
            int start = result.length();
            result.append(content.toString());
            int end = result.length();
            result.setSpan(new SubscriptSpan(), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            result.setSpan(new RelativeSizeSpan(0.75f), start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (isActive) {
            result.append(type.getSymbol());
        }
        return result;
    }

    public ModuleType getType() {
        return type;
    }

    public String getContent() {
        return content.toString();
    }
}