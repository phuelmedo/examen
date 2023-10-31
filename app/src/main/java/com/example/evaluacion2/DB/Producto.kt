package com.example.evaluacion2.DB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Producto (
    @PrimaryKey(autoGenerate = true) val id: Int,
    var producto:String ,
    var disponible:Boolean
)