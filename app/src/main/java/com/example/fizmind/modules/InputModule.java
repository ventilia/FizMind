package com.example.fizmind.modules;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import com.example.fizmind.utils.LogUtils;

/**
 * класс, представляющий модуль (нижний индекс).
 * поддерживает ввод, удаление и отображение с учетом типа модуля.
 */
public class InputModule {
    private final ModuleType type;
    private final StringBuilder content;
    private boolean isActive;

    public InputModule(ModuleType type) {
        if (type != ModuleType.SUBSCRIPT) {
            throw new IllegalArgumentException("поддерживается только тип SUBSCRIPT");
        }
        this.type = type;
        this.content = new StringBuilder();
        this.isActive = true;
        LogUtils.logModuleCreated("InputModule", type);
    }

    public boolean apply(String input) {
        if (!input.matches("[0-9]")) {
            LogUtils.w("InputModule", "недопустимый символ для " + type.getDescription() + ": " + input);
            return false;
        }
        content.append(input);
        LogUtils.logSymbolAdded("InputModule", type, input);
        return true;
    }

    public boolean delete() {
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
            LogUtils.logSymbolDeleted("InputModule", type, content.toString());
            return content.length() == 0;
        }
        return true;
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
}