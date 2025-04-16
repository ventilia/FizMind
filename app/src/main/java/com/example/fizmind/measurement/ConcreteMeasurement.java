package com.example.fizmind.measurement;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import com.example.fizmind.SIConverter;
import com.example.fizmind.utils.LogUtils;

/**
 * конкретное измерение с поддержкой parcelable для передачи между активити
 */
public class ConcreteMeasurement extends Measurement implements Parcelable {

    private final String subscript;              // индекс измерения
    private final boolean constant;              // флаг константы
    private final SpannableStringBuilder originalDisplay; // исходное отображение с форматированием
    private final double originalValue;          // исходное значение
    private final String originalUnit;           // исходная единица измерения
    private final String conversionSteps;        // шаги конвертации
    private final boolean isSIUnit;              // флаг единицы СИ
    private final boolean isConversionMode;      // флаг режима конвертации

    /**
     * конструктор для создания измерения
     */
    public ConcreteMeasurement(String designation, double value, String unit,
                               String designationOperations, String valueOperations,
                               String subscript, boolean constant,
                               SpannableStringBuilder originalDisplay,
                               double originalValue, String originalUnit,
                               String conversionSteps, boolean isSIUnit, boolean isConversionMode) {
        super(designation, value, unit, designationOperations, valueOperations);
        this.subscript = subscript;
        this.constant = constant;
        this.originalDisplay = originalDisplay;
        this.originalValue = originalValue;
        this.originalUnit = originalUnit;
        this.conversionSteps = conversionSteps != null ? conversionSteps : "";
        this.isSIUnit = isSIUnit;
        this.isConversionMode = isConversionMode;
        LogUtils.logMeasurementCreated("ConcreteMeasurement", toString());
    }

    /**
     * конструктор для восстановления из parcel
     */
    protected ConcreteMeasurement(Parcel in) {
        super(in.readString(), in.readDouble(), in.readString(), in.readString(), in.readString());
        this.subscript = in.readString();
        this.constant = in.readByte() != 0;
        this.originalDisplay = new SpannableStringBuilder(in.readString()); // восстанавливаем как строку
        this.originalValue = in.readDouble();
        this.originalUnit = in.readString();
        this.conversionSteps = in.readString();
        this.isSIUnit = in.readByte() != 0;
        this.isConversionMode = in.readByte() != 0;
    }

    /**
     * запись данных в parcel
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(designation);
        dest.writeDouble(value);
        dest.writeString(unit);
        dest.writeString(designationOperations);
        dest.writeString(valueOperations);
        dest.writeString(subscript);
        dest.writeByte((byte) (constant ? 1 : 0));
        dest.writeString(originalDisplay.toString()); // сериализуем как строку
        dest.writeDouble(originalValue);
        dest.writeString(originalUnit);
        dest.writeString(conversionSteps);
        dest.writeByte((byte) (isSIUnit ? 1 : 0));
        dest.writeByte((byte) (isConversionMode ? 1 : 0));
    }

    /**
     * описание содержимого (не используется в данном случае)
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * создатель объекта из parcel
     */
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

    /**
     * валидация измерения
     */
    @Override
    public boolean validate() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            LogUtils.e("ConcreteMeasurement", "недопустимое значение: " + value);
            return false;
        }
        if (unit == null || unit.isEmpty()) {
            LogUtils.e("ConcreteMeasurement", "единица измерения не указана для " + designation);
            return false;
        }
        return true;
    }

    /**
     * строковое представление измерения
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (!designationOperations.isEmpty()) {
            sb.append(designationOperations).append("(").append(designation).append(")");
        } else {
            sb.append(designation);
        }
        if (subscript != null && !subscript.isEmpty()) {
            sb.append("_").append(subscript);
        }

        sb.append(" = ");
        if (!valueOperations.isEmpty()) {
            sb.append(valueOperations);
        } else {
            sb.append(SIConverter.formatValue(isConversionMode ? originalValue : value));
        }
        if (!unit.isEmpty()) {
            sb.append(" ").append(isConversionMode ? originalUnit : unit);
        }

        if (isConversionMode && !isSIUnit && !conversionSteps.isEmpty()) {
            sb.append(" = ").append(conversionSteps);
        }

        return sb.toString();
    }

    /**
     * получение единицы измерения в верхнем регистре
     */
    @Override
    public String getUnit() {
        return unit != null ? unit.toUpperCase() : "";
    }

    // геттеры для остальных полей
    public String getSubscript() {
        return subscript != null ? subscript : "";
    }

    public boolean isConstant() {
        return constant;
    }

    public SpannableStringBuilder getOriginalDisplay() {
        return originalDisplay;
    }

    public double getOriginalValue() {
        return originalValue;
    }

    public String getOriginalUnit() {
        return originalUnit;
    }

    public String getConversionSteps() {
        return conversionSteps;
    }

    public boolean isSIUnit() {
        return isSIUnit;
    }

    public boolean isConversionMode() {
        return isConversionMode;
    }
}