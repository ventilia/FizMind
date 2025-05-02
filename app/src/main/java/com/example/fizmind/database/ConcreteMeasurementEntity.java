package com.example.fizmind.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// сущность для таблицы concrete_measurements
@Entity(tableName = "concrete_measurements")
public class ConcreteMeasurementEntity {
    @PrimaryKey(autoGenerate = true)
    public int id; // уникальный идентификатор

    public String baseDesignation;       // базовое обозначение
    public double value;                 // текущее значение
    public String unit;                  // текущая единица измерения
    public String designationOperations; // операции над обозначением
    public String valueOperations;       // операции над значением
    public String subscript;             // нижний индекс
    public boolean constant;             // флаг константы
    public String originalDisplay;       // форматированный текст в виде HTML
    public double originalValue;         // исходное значение
    public String originalUnit;          // исходная единица
    public String conversionSteps;       // шаги конвертации
    public boolean isSIUnit;             // флаг единицы СИ
    public boolean isConversionMode;     // флаг режима конвертации
}