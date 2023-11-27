package com.example.evaluacion2.DB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Lugar (
    @PrimaryKey(autoGenerate = true) val id: Int,
    var nombre:String ,
    var orden:Int ,
    var url_imagen:String ,
    var lat_long:String ,
    var imagen:String? ,
    var costo_alojamiento:Int ,
    var costo_transporte:Int? ,
    var comentario:String?
)