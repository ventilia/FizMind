package com.example.fizmind.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// база данных с двумя таблицами
@Database(entities = {ConcreteMeasurementEntity.class, UnknownQuantityEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MeasurementDao measurementDao(); // доступ к DAO
}