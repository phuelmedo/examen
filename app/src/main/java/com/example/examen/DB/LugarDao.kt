package com.example.evaluacion2.DB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LugarDao {

    @Query("SELECT * FROM Lugar ORDER BY orden ASC")
    fun getAll(): List<Lugar>

    @Query("SELECT COUNT(*) FROM Lugar")
    fun count(): Int

    @Query("SELECT * FROM Lugar WHERE id = :id")
    fun findById(id:Int): Lugar

    @Insert
    fun insert(lugar: Lugar): Long
    @Update
    fun update(lugar: Lugar)

    @Delete
    fun delete(nombre:Lugar)
}