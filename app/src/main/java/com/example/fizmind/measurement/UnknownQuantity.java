package com.example.fizmind.measurement;

import android.text.Html;
import android.text.SpannableStringBuilder;

import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.utils.LogUtils;

// класс для неизвестных величин с поддержкой базы данных
public class UnknownQuantity {
    private final String displayDesignation;    // отображаемое обозначение
    private final String logicalDesignation;    // логическое обозначение
    private final String subscript;             // нижний индекс
    private final boolean usesStix;             // флаг использования шрифта STIX
    private final SpannableStringBuilder displayText; // форматированный текст

    // конструктор из сущности Room
    public UnknownQuantity(UnknownQuantityEntity entity) {
        this.displayDesignation = entity.displayDesignation;
        this.logicalDesignation = entity.logicalDesignation;
        this.subscript = entity.subscript;
        this.usesStix = entity.usesStix;
        this.displayText = entity.displayText != null
                ? (SpannableStringBuilder) Html.fromHtml(entity.displayText)
                : new SpannableStringBuilder();
    }

    // основной конструктор
    public UnknownQuantity(String displayDesignation, String logicalDesignation,
                           String subscript, boolean usesStix, SpannableStringBuilder displayText) {
        this.displayDesignation = displayDesignation != null ? displayDesignation : "";
        this.logicalDesignation = logicalDesignation != null ? logicalDesignation : "";
        this.subscript = subscript != null ? subscript : "";
        this.usesStix = usesStix;
        this.displayText = displayText != null ? displayText : new SpannableStringBuilder();
    }

    // геттеры
    public String getDisplayDesignation() { return displayDesignation; }
    public String getLogicalDesignation() { return logicalDesignation; }
    public String getSubscript() { return subscript; }
    public boolean usesStix() { return usesStix; }
    public SpannableStringBuilder getDisplayText() { return displayText; }

    // получение полного обозначения (с индексом)
    public String getFullDesignation() {
        return subscript.isEmpty() ? logicalDesignation : logicalDesignation + "_" + subscript;
    }

    // преобразование в сущность Room
    public UnknownQuantityEntity toEntity() {
        UnknownQuantityEntity entity = new UnknownQuantityEntity();
        entity.displayDesignation = this.displayDesignation;
        entity.logicalDesignation = this.logicalDesignation;
        entity.subscript = this.subscript;
        entity.usesStix = this.usesStix;
        entity.displayText = Html.toHtml(this.displayText);
        return entity;
    }
}