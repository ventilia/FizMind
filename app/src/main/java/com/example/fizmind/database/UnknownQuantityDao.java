package com.example.fizmind.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// интерфейс для операций с неизвестными величинами
@Dao
public interface UnknownQuantityDao {
    @Insert
    void insert(UnknownQuantityEntity unknown);

    @Update
    void update(UnknownQuantityEntity unknown);

    @Delete
    void delete(UnknownQuantityEntity unknown);

    @Query("SELECT * FROM unknown_quantities")
    List<UnknownQuantityEntity> getAllUnknowns();

    @Query("DELETE FROM unknown_quantities WHERE id = (SELECT MAX(id) FROM unknown_quantities)")
    void deleteLastUnknown();

    @Query("DELETE FROM unknown_quantities")
    void deleteAll();
}