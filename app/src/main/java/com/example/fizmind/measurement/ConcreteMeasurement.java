package com.example.fizmind.measurement;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import com.example.fizmind.SI.SIConverter;
import com.example.fizmind.utils.LogUtils;

//подклассс
public class ConcreteMeasurement extends Measurement implements Parcelable {

    private final String baseDesignation;
    private final String subscript;              // индекс
    private final boolean constant;
    private final SpannableStringBuilder originalDisplay;
    private final double originalValue;
    private final String originalUnit;
    private final String conversionSteps;        // шаги конвертации
    private final boolean isSIUnit;              // флаг единицы СИ
    private final boolean isConversionMode;      // флаг режима конвертации


    public ConcreteMeasurement(String baseDesignation, double value, String unit,
                               String designationOperations, String valueOperations,
                               String subscript, boolean constant,
                               SpannableStringBuilder originalDisplay,
                               double originalValue, String originalUnit,
                               String conversionSteps, boolean isSIUnit, boolean isConversionMode) {
        super(baseDesignation, value, unit, designationOperations, valueOperations);
        this.baseDesignation = baseDesignation;


        this.designation = baseDesignation;


        this.subscript = subscript != null ? subscript : "";
        this.constant = constant;
        this.originalDisplay = originalDisplay != null ? originalDisplay : createDefaultDisplay(baseDesignation, subscript, originalValue, originalUnit);
        this.originalValue = originalValue;
        this.originalUnit = originalUnit != null ? originalUnit : "";
        this.conversionSteps = conversionSteps != null ? conversionSteps : "";
        this.isSIUnit = isSIUnit;
        this.isConversionMode = isConversionMode;
        LogUtils.logMeasurementCreated("ConcreteMeasurement", toString());
    }

    /**
     *  baseDesignation базовое обозначение
     *  subscript индекс
     *  value значение
     *  unit единица измерения
     *  отформатированное отображение
     */
    private SpannableStringBuilder createDefaultDisplay(String baseDesignation, String subscript, double value, String unit) {
        SpannableStringBuilder display = new SpannableStringBuilder();
        display.append(baseDesignation);
        if (!subscript.isEmpty()) {
            display.append("_").append(subscript);
        }
        display.append(" = ").append(SIConverter.formatValue(value)).append(" ").append(unit);
        return display;
    }


    protected ConcreteMeasurement(Parcel in) {
        super(in.readString(), in.readDouble(), in.readString(), in.readString(), in.readString());
        this.baseDesignation = designation;
        this.subscript = in.readString();
        this.constant = in.readByte() != 0;
        this.originalDisplay = new SpannableStringBuilder(in.readString());
        this.originalValue = in.readDouble();
        this.originalUnit = in.readString();
        this.conversionSteps = in.readString();
        this.isSIUnit = in.readByte() != 0;
        this.isConversionMode = in.readByte() != 0;
    }


      // в parcel

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(baseDesignation);
        dest.writeDouble(value);
        dest.writeString(unit);
        dest.writeString(designationOperations);
        dest.writeString(valueOperations);
        dest.writeString(subscript);
        dest.writeByte((byte) (constant ? 1 : 0));
        dest.writeString(originalDisplay.toString());
        dest.writeDouble(originalValue);
        dest.writeString(originalUnit);
        dest.writeString(conversionSteps);
        dest.writeByte((byte) (isSIUnit ? 1 : 0));
        dest.writeByte((byte) (isConversionMode ? 1 : 0));
    }


     //(не используется)

    @Override
    public int describeContents() {
        return 0;
    }


     // создатель объекта

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
    public boolean validate() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            LogUtils.e("ConcreteMeasurement", "недопустимое значение: " + value);
            return false;
        }
        if (unit == null || unit.isEmpty()) {
            LogUtils.e("ConcreteMeasurement", "единица измерения не указана для " + baseDesignation);
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!designationOperations.isEmpty()) {
            sb.append(designationOperations).append("(").append(baseDesignation).append(")");
        } else {
            sb.append(baseDesignation);
        }
        if (!subscript.isEmpty()) {
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


     //получение полного обозначения с индексом

    public String getFullDesignation() {
        return subscript.isEmpty() ? baseDesignation : baseDesignation + "_" + subscript;
    }

    public String getBaseDesignation() {
        return baseDesignation;
    }

    public String getSubscript() {
        return subscript;
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