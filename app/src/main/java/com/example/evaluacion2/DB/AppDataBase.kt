package com.example.evaluacion2.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Producto::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao

    companion object {

        @Volatile
        private var BASE_DATOS: AppDataBase? = null
        fun getInstance(contexto: Context): AppDataBase {
            return BASE_DATOS ?: synchronized(this) {
                Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDataBase::class.java,
                    "ProductosBD.bd"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { BASE_DATOS = it }
            }
        }
    }
}