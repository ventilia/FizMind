package com.example.fizmind.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// абстрактный класс базы данных для Room
@Database(entities = {ConcreteMeasurementEntity.class, UnknownQuantityEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // дао для работы с измерениями
    public abstract MeasurementDao measurementDao();

    // дао для работы с неизвестными величинами
    public abstract UnknownQuantityDao unknownQuantityDao();
}