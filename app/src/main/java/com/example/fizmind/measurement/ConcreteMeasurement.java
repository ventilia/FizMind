package com.example.fizmind.measurement;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableStringBuilder;

import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.utils.LogUtils;

// класс для измерений с поддержкой базы данных и полной функциональностью
public class ConcreteMeasurement extends Measurement implements Parcelable {

    private final String baseDesignation;          // базовое обозначение
    private final String subscript;                // нижний индекс
    private final boolean constant;                // флаг константы
    private final SpannableStringBuilder originalDisplay; // исходный форматированный текст
    private final double originalValue;            // исходное значение
    private final String originalUnit;             // исходная единица измерения
    private final String conversionSteps;          // шаги конвертации
    private final boolean isSIUnit;                // флаг единицы СИ
    private final boolean isConversionMode;        // флаг режима конвертации

    // конструктор из сущности Room
    public ConcreteMeasurement(ConcreteMeasurementEntity entity) {
        super(entity.baseDesignation, entity.value, entity.unit, entity.designationOperations, entity.valueOperations);
        this.baseDesignation = entity.baseDesignation;
        this.subscript = entity.subscript;
        this.constant = entity.constant;
        this.originalDisplay = entity.originalDisplay != null
                ? (SpannableStringBuilder) Html.fromHtml(entity.originalDisplay)
                : new SpannableStringBuilder();
        this.originalValue = entity.originalValue;
        this.originalUnit = entity.originalUnit;
        this.conversionSteps = entity.conversionSteps;
        this.isSIUnit = entity.isSIUnit;
        this.isConversionMode = entity.isConversionMode;
    }

    // основной конструктор
    public ConcreteMeasurement(String baseDesignation, double value, String unit,
                               String designationOperations, String valueOperations,
                               String subscript, boolean constant, SpannableStringBuilder originalDisplay,
                               double originalValue, String originalUnit, String conversionSteps,
                               boolean isSIUnit, boolean isConversionMode) {
        super(baseDesignation, value, unit, designationOperations, valueOperations);
        this.baseDesignation = baseDesignation;
        this.subscript = subscript != null ? subscript : "";
        this.constant = constant;
        this.originalDisplay = originalDisplay != null ? originalDisplay : new SpannableStringBuilder();
        this.originalValue = originalValue;
        this.originalUnit = originalUnit != null ? originalUnit : "";
        this.conversionSteps = conversionSteps != null ? conversionSteps : "";
        this.isSIUnit = isSIUnit;
        this.isConversionMode = isConversionMode;
    }

    // конструктор для Parcelable
    protected ConcreteMeasurement(Parcel in) {
        super(in.readString(), in.readDouble(), in.readString(), in.readString(), in.readString());
        baseDesignation = in.readString();
        subscript = in.readString();
        constant = in.readByte() != 0;
        String htmlDisplay = in.readString();
        originalDisplay = htmlDisplay != null
                ? (SpannableStringBuilder) Html.fromHtml(htmlDisplay)
                : new SpannableStringBuilder();
        originalValue = in.readDouble();
        originalUnit = in.readString();
        conversionSteps = in.readString();
        isSIUnit = in.readByte() != 0;
        isConversionMode = in.readByte() != 0;
    }

    // геттеры
    public String getBaseDesignation() { return baseDesignation; }
    public String getSubscript() { return subscript; }
    public boolean isConstant() { return constant; }
    public SpannableStringBuilder getOriginalDisplay() { return originalDisplay; }
    public double getOriginalValue() { return originalValue; }
    public String getOriginalUnit() { return originalUnit; }
    public String getConversionSteps() { return conversionSteps; }
    public boolean isSIUnit() { return isSIUnit; }
    public boolean isConversionMode() { return isConversionMode; }

    // получение полного обозначения (с индексом)
    public String getFullDesignation() {
        return subscript.isEmpty() ? baseDesignation : baseDesignation + "_" + subscript;
    }

    // преобразование в сущность Room для сохранения
    public ConcreteMeasurementEntity toEntity() {
        ConcreteMeasurementEntity entity = new ConcreteMeasurementEntity();
        entity.baseDesignation = this.baseDesignation;
        entity.value = this.value;
        entity.unit = this.unit;
        entity.designationOperations = this.designationOperations;
        entity.valueOperations = this.valueOperations;
        entity.subscript = this.subscript;
        entity.constant = this.constant;
        entity.originalDisplay = Html.toHtml(this.originalDisplay);
        entity.originalValue = this.originalValue;
        entity.originalUnit = this.originalUnit;
        entity.conversionSteps = this.conversionSteps;
        entity.isSIUnit = this.isSIUnit;
        entity.isConversionMode = this.isConversionMode;
        return entity;
    }

    // реализация абстрактного метода validate
    @Override
    public boolean validate() {
        boolean isValid = value >= 0 && !unit.isEmpty() && !baseDesignation.isEmpty();
        if (!isValid) {
            LogUtils.w("ConcreteMeasurement", "валидация не пройдена: value=" + value + ", unit=" + unit + ", baseDesignation=" + baseDesignation);
        }
        return isValid;
    }

    // реализация Parcelable
    public static final Creator<ConcreteMeasurement> CREATOR = new Creator<ConcreteMeasurement>() {
        @Override
        public ConcreteMeasurement createFromParcel(Parcel in) {
            return new ConcreteMeasurement(in);
        }

        @Override
        public ConcreteMeasurement[] newArray(int size) {
            return new ConcreteMeasurement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(baseDesignation);
        dest.writeDouble(value);
        dest.writeString(unit);
        dest.writeString(designationOperations);
        dest.writeString(valueOperations);
        dest.writeString(subscript);
        dest.writeByte((byte) (constant ? 1 : 0));
        dest.writeString(Html.toHtml(originalDisplay));
        dest.writeDouble(originalValue);
        dest.writeString(originalUnit);
        dest.writeString(conversionSteps);
        dest.writeByte((byte) (isSIUnit ? 1 : 0));
        dest.writeByte((byte) (isConversionMode ? 1 : 0));
    }
}