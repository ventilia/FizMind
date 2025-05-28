package com.example.fizmind.modules;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;

public class InputModule {
    private final ModuleType type;
    private final StringBuilder content;
    private boolean isActive;


    public InputModule(ModuleType type) {
        this.type = type;
        this.content = new StringBuilder();
        this.isActive = false;
    }


    public void apply(String input) {
        content.append(input);
    }


    public boolean deleteChar() {
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
            return content.length() == 0;
        }
        return true;
    }

    public void deleteEntire() {
        content.setLength(0);
        isActive = false;
    }


    public void activate() {
        isActive = true;
    }


    public void deactivate() {
        isActive = false;
    }


    public boolean isEmpty() {
        return content.length() == 0;
    }


    public boolean isActive() {
        return isActive;
    }


    public ModuleType getType() {
        return type;
    }


    public String getContent() {
        return content.toString();
    }

    public SpannableStringBuilder getDisplayText() {
        SpannableStringBuilder result = new SpannableStringBuilder(content.toString());
        result.setSpan(new SubscriptSpan(), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setSpan(new RelativeSizeSpan(0.75f), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }
}