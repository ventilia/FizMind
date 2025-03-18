package com.example.fizmind.measurement;

import android.util.Log;

public class UnknownQuantity {
    private final String designation; // Логический идентификатор обозначения
    private String subscript; // Индекс для обозначения

    public UnknownQuantity(String designation, String subscript) {
        this.designation = designation;
        this.subscript = subscript;
        Log.d("UnknownQuantity", "Создано неизвестное: " + designation + (subscript != null ? "_" + subscript : ""));
    }

    public UnknownQuantity(String designation) {
        this(designation, "");
    }

    public String getDesignation() {
        return designation;
    }

    public String getSubscript() {
        return subscript;
    }

    public boolean validate() {
        if (designation == null || designation.isEmpty()) {
            Log.e("UnknownQuantity", "Пустое обозначение для неизвестного");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(designation);
        if (subscript != null && !subscript.isEmpty()) {
            sb.append("_").append(subscript);
        }
        sb.append(" = ?");
        return sb.toString();
    }
}