package com.example.evaluacion2.DB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductoDao {

    @Query("SELECT * FROM producto ORDER BY disponible DESC")
    fun getAll(): List<Producto>

    @Query("SELECT COUNT(*) FROM producto")
    fun count(): Int

    @Insert
    fun insert(producto:Producto):Long

    @Update
    fun update(producto:Producto)

    @Delete
    fun delete(producto:Producto)
}