package com.example.fizmind.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// сущность для таблицы unknown_quantities
@Entity(tableName = "unknown_quantities")
public class UnknownQuantityEntity {
    @PrimaryKey(autoGenerate = true)
    public int id; // уникальный идентификатор

    public String displayDesignation; // отображаемое обозначение
    public String logicalDesignation; // логическое обозначение
    public String subscript;          // нижний индекс
    public boolean usesStix;          // использование шрифта STIX
    public String displayText;        // форматированный текст в виде HTML
}