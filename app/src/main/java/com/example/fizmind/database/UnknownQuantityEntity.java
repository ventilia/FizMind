package com.example.fizmind.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "unknown_quantities")
public class UnknownQuantityEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String displayDesignation;
    public String logicalDesignation;
    public String subscript;
    public boolean usesStix;
    public String displayText;


    public UnknownQuantityEntity() {}


    public UnknownQuantityEntity(
            String displayDesignation, String logicalDesignation,
            String subscript, boolean usesStix, String displayText) {
        this.displayDesignation = displayDesignation;
        this.logicalDesignation = logicalDesignation;
        this.subscript = subscript;
        this.usesStix = usesStix;
        this.displayText = displayText;
    }


    public int getId() { return id; }
    public String getDisplayDesignation() { return displayDesignation; }
    public String getLogicalDesignation() { return logicalDesignation; }
    public String getSubscript() { return subscript; }
    public boolean isUsesStix() { return usesStix; }
    public String getDisplayText() { return displayText; }

    // сет
    public void setId(int id) { this.id = id; }
    public void setDisplayDesignation(String displayDesignation) { this.displayDesignation = displayDesignation; }
    public void setLogicalDesignation(String logicalDesignation) { this.logicalDesignation = logicalDesignation; }
    public void setSubscript(String subscript) { this.subscript = subscript; }
    public void setUsesStix(boolean usesStix) { this.usesStix = usesStix; }
    public void setDisplayText(String displayText) { this.displayText = displayText; }
}