package com.example.fizmind.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

// Абстрактный класс базы данных для Room
@Database(entities = {ConcreteMeasurementEntity.class, UnknownQuantityEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // DAO для работы с измерениями
    public abstract MeasurementDao measurementDao();

    // DAO для работы с неизвестными величинами
    public abstract UnknownQuantityDao unknownQuantityDao();

    // Singleton для базы данных
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "database-name")
                            .addMigrations(MIGRATION_1_2) // Добавляем миграцию от 1 к 2
                            .fallbackToDestructiveMigration() // Оставляем как резервный вариант
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE concrete_measurements ADD COLUMN usesStix INTEGER NOT NULL DEFAULT 0");
        }
    };
}