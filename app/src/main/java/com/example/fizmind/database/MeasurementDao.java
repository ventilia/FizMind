package com.example.fizmind.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// интерфейс для операций с базой данных
@Dao
public interface MeasurementDao {
    // добавить измерение
    @Insert
    void insertConcreteMeasurement(ConcreteMeasurementEntity measurement);

    // добавить неизвестную величину
    @Insert
    void insertUnknownQuantity(UnknownQuantityEntity unknown);

    // обновить измерение
    @Update
    void updateConcreteMeasurement(ConcreteMeasurementEntity measurement);

    // получить все измерения
    @Query("SELECT * FROM concrete_measurements ORDER BY id ASC")
    List<ConcreteMeasurementEntity> getAllConcreteMeasurements();

    // получить все неизвестные величины
    @Query("SELECT * FROM unknown_quantities ORDER BY id ASC")
    List<UnknownQuantityEntity> getAllUnknownQuantities();

    // удалить последнее измерение
    @Query("DELETE FROM concrete_measurements WHERE id = (SELECT MAX(id) FROM concrete_measurements)")
    void deleteLastConcreteMeasurement();

    // удалить последнюю неизвестную величину
    @Query("DELETE FROM unknown_quantities WHERE id = (SELECT MAX(id) FROM unknown_quantities)")
    void deleteLastUnknownQuantity();

    // очистить все измерения
    @Query("DELETE FROM concrete_measurements")
    void clearConcreteMeasurements();

    // очистить все неизвестные величины
    @Query("DELETE FROM unknown_quantities")
    void clearUnknownQuantities();

    // проверить уникальность индекса
    @Query("SELECT COUNT(*) FROM concrete_measurements WHERE baseDesignation = :designation AND subscript = :subscript")
    int countByDesignationAndSubscript(String designation, String subscript);
}