package com.example.fizmind.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// интерфейс для операций с измерениями
@Dao
public interface MeasurementDao {
    @Insert
    void insert(ConcreteMeasurementEntity measurement);

    @Update
    void update(ConcreteMeasurementEntity measurement);

    @Delete
    void delete(ConcreteMeasurementEntity measurement);

    @Query("SELECT * FROM concrete_measurements")
    List<ConcreteMeasurementEntity> getAllMeasurements();

    @Query("DELETE FROM concrete_measurements WHERE id = (SELECT MAX(id) FROM concrete_measurements)")
    void deleteLastMeasurement();
}