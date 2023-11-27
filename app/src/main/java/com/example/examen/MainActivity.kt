package com.example.evaluacion2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evaluacion2.DB.AppDataBase
import com.example.evaluacion2.DB.Lugar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppLugaresUI()
        }
    }
}
enum class Accion {
    LISTAR, CREAR, EDITAR
}

@Composable
fun AppLugaresUI(){
    val contexto = LocalContext.current
    val (lugares, setLugares) = remember { mutableStateOf(emptyList<Lugar>()) }
    val (seleccion, setSeleccion) = remember { mutableStateOf<Lugar?>(null) }
    val (accion, setAccion) = remember { mutableStateOf(Accion.LISTAR) }

    LaunchedEffect(lugares) {
        withContext(Dispatchers.IO) {
            val db = AppDataBase.getInstance( contexto )
            setLugares( db.lugarDao().getAll() )
        }
    }

    val onSave = {
        val db = AppDataBase.getInstance( contexto )
        setAccion(Accion.LISTAR)
            setLugares( db.lugarDao().getAll() )
    }


    when (accion) {
        Accion.CREAR -> LugarFormUI(null, onSave)
        Accion.EDITAR -> LugarFormUI(seleccion, onSave)

        else -> ListarLugaresUI(
            lugares,
            setLugares,
            onAdd = {
                setAccion(Accion.CREAR)
            },
            onEdit = { lugarSeleccionado ->
                val lugar = lugares.find { it.nombre == lugarSeleccionado.nombre }
                if (lugar != null) {
                    setSeleccion(lugar)
                    setAccion(Accion.EDITAR)
                }
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListarLugaresUI(
    lugares:List<Lugar>,
    setLugares: (List<Lugar>) -> Unit,
    onAdd:() -> Unit = {},
    onEdit:(c:Lugar) -> Unit = {},
){

    var lugarSeleccionado by remember { mutableStateOf<Lugar?>(null) }


    Scaffold(

        floatingActionButton = {
            if (lugarSeleccionado == null) {
                ExtendedFloatingActionButton(
                    onClick = { onAdd() },
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "crear"
                        )
                    },
                    text = { Text(text = stringResource(R.string.button_create)) }
                )
            }
        }
    ) { contentPadding ->
        if (lugares.isNotEmpty()) {
            if (lugarSeleccionado != null) {
                LugarDetalleUI(
                    lugar = lugarSeleccionado!!,
                    onEdit = { onEdit(lugarSeleccionado!!) },
                    setLugares = { nuevosLugares -> setLugares(nuevosLugares) }
                ) {
                    setLugares(lugares.filterNot { it.id == lugarSeleccionado!!.id })
                    lugarSeleccionado = null
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(lugares) { lugar ->
                        LugarItemUI(
                            lugar = lugar,
                            onEdit = { onEdit(lugar) },
                            setLugares = { nuevosLugares -> setLugares(nuevosLugares) }
                        ) {
                            lugarSeleccionado = lugar
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.list_empty))
            }
        }
    }
}
@Composable
fun LugarItemUI(lugar:Lugar, setLugares: (List<Lugar>) -> Unit, onEdit:(Lugar) -> Unit = {}, onDelete: () -> Unit = {}, onShowLocation: (Boolean) -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    val contexto = LocalContext.current
    var tasaDeCambio: Double? by remember { mutableStateOf(null) }

    cargarTasaDeCambio(
        onSuccess = { tasa ->
            tasaDeCambio = tasa
        },
        onError = {

        }
    )

    Spacer(modifier = Modifier.height(20.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Image(
            painter = rememberImagePainter(data = lugar.url_imagen),
            contentDescription = "Imagen Lugar",
            modifier = Modifier
                .size(150.dp, 120.dp)
                .clickable {

                },
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column() {

            Text(lugar.nombre, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)) {
                    append(text = stringResource(R.string.cost))
                }
                val costoAlojamientoConvertido = lugar.costo_alojamiento?.let { costo ->
                    tasaDeCambio?.let { tasa ->
                        (costo / tasa).toInt()
                    } ?: 0
                } ?: 0
                append("$" + (lugar.costo_alojamiento).toString() + " - " + costoAlojamientoConvertido + "USD")
            })
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)) {
                    append(text = stringResource(R.string.transfer))
                }
                val costoTransporteConvertido = lugar.costo_transporte?.let { costo ->
                    tasaDeCambio?.let { tasa ->
                        (costo / tasa).toInt()
                    } ?: 0
                } ?: 0
                append("$" + lugar.costo_transporte.toString() + " - " + costoTransporteConvertido + "USD")
            })
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicacion",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onShowLocation(true)
                        }

                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onEdit(lugar)
                        }
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            coroutineScope.launch(Dispatchers.IO) {
                                val dao = AppDataBase.getInstance(contexto).lugarDao()
                                dao.delete(lugar)
                                val db = AppDataBase.getInstance(contexto)
                                setLugares(db.lugarDao().getAll())
                                onDelete()
                            }
                        }
                )
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
    }
}
@Composable
fun LugarDetalleUI(lugar:Lugar, onSave:()->Unit = {}, setLugares: (List<Lugar>) -> Unit, onEdit:(Lugar) -> Unit = {}, onDelete: () -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val contexto = LocalContext.current
    val (fotoTomada, setFotoTomada) = remember { mutableStateOf(false) }
    val (imagenTomada, setImagenTomada) = remember { mutableStateOf<Bitmap?>(null) }
    var tasaDeCambio: Double? by remember { mutableStateOf(null) }

    cargarTasaDeCambio(
        onSuccess = { tasa ->
            tasaDeCambio = tasa
        },
        onError = {

        }
    )

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { result: Bitmap? ->
        if (result != null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Foto tomada exitosamente")
            }
            setImagenTomada(result)
            setFotoTomada(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePicture.launch(null)
        } else {

        }
    }

    val iconoTomarFoto = if (fotoTomada) {
        Icons.Default.Done
    } else {
        Icons.Default.Add
    }

    val (showImageDialog, setShowImageDialog) = remember { mutableStateOf(false) }

    Column() {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = lugar.nombre,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )
        Spacer(modifier = Modifier.height(15.dp))
        Image(
            painter = rememberImagePainter(data = lugar.url_imagen),
            contentDescription = "Imagen Lugar",
            modifier = Modifier
                .size(300.dp, 240.dp)
                .clickable {
                    coroutineScope.launch(Dispatchers.IO) {

                    }
                }
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)) {
                                append(text = stringResource(R.string.cost))
                            }
                        }
                    )
                    val costoAlojamientoConvertido = lugar.costo_alojamiento?.let { costo ->
                        tasaDeCambio?.let { tasa ->
                            (costo / tasa).toInt()
                        } ?: 0
                    } ?: 0
                    Text("$" + lugar.costo_alojamiento.toString() + " - " + costoAlojamientoConvertido + "USD")
                }
                Spacer(modifier = Modifier.width(25.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)) {
                                append(text = stringResource(R.string.transfer))
                            }
                        }
                    )
                    val costoTrasladoConvertido = lugar.costo_transporte?.let { costo ->
                        tasaDeCambio?.let { tasa ->
                            (costo / tasa).toInt()
                        } ?: 0
                    } ?: 0
                    Text("$" + lugar.costo_transporte.toString() + " - " + costoTrasladoConvertido + "USD")
                }
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = stringResource(id = R.string.Comment),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )
        Text(
            text = lugar.comentario.toString(),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )
        Spacer(modifier = Modifier.height(15.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)

        ) {
            Icon(
                imageVector = iconoTomarFoto,
                contentDescription = if (fotoTomada) "Ver Foto" else "Tomar Foto",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        if (fotoTomada) {
                            if (imagenTomada != null) {
                                setShowImageDialog(true)
                            }
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
            )
            if (showImageDialog) {
                AlertDialog(
                    onDismissRequest = {
                        setShowImageDialog(false)
                    },
                    title = { Text(text = stringResource(R.string.tittle)) },
                    text = {
                        Image(
                            painter = rememberImagePainter(data = imagenTomada),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(500.dp),
                            contentScale = ContentScale.Crop
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                setShowImageDialog(false)
                            }
                        ) {
                            Text(text = stringResource(R.string.close))
                        }
                    }
                )
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        onEdit(lugar)
                    }
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        coroutineScope.launch(Dispatchers.IO) {
                            val dao = AppDataBase.getInstance(contexto).lugarDao()
                            dao.delete(lugar)
                            val db = AppDataBase.getInstance(contexto)
                            setLugares(db.lugarDao().getAll())
                            onDelete()
                            onSave()
                        }
                    }
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 200.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(5.dp)
                    ),
            ) {
                MapScreen(lugar = lugar)
            }

        }
    }
}
@Composable
fun MapScreen(lugar: Lugar) {
    MapaOsmUI(latitudLongitud = lugar.lat_long, modifier = Modifier.fillMaxSize())
}
@Composable
fun MapaOsmUI(latitudLongitud: String, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    AndroidView(
        factory = {
            MapView(it).also {
                it.setTileSource(TileSourceFactory.MAPNIK)
                org.osmdroid.config.Configuration.getInstance().userAgentValue = contexto.packageName
            }
        },
        modifier = modifier
            .size(100.dp, 100.dp)
            .clip(RoundedCornerShape(16.dp)),
        update = {
            it.overlays.removeIf { true }
            it.invalidate()
            it.controller.setZoom(15.0)

            val latitudLongitudArray = latitudLongitud.split(", ")

            if (latitudLongitudArray.size == 2) {
                val latitud = latitudLongitudArray[0].toDoubleOrNull()
                val longitud = latitudLongitudArray[1].toDoubleOrNull()

                if (latitud != null && longitud != null) {
                    val geoPoint = GeoPoint(latitud, longitud)
                    it.controller.animateTo(geoPoint)
                    val marcador = Marker(it)
                    marcador.position = geoPoint
                    marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    it.overlays.add(marcador)
                }
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LugarFormUI(c:Lugar?, onSave:()->Unit = {}){
    val contexto = LocalContext.current

    val (nombre, setNombre) = remember { mutableStateOf(c?.nombre ?: "") }
    val (urlImagen, setUrlImagen) = remember { mutableStateOf(c?.url_imagen ?: "") }
    val (latLong, setLatLong) = remember { mutableStateOf(c?.lat_long ?: "") }
    val (orden, setOrden) = remember { mutableStateOf(c?.orden ?: 0) }
    val (costoAlojamiento, setCostoAlojamiento) = remember { mutableStateOf(c?.costo_alojamiento ?: 0) }
    val (costoTransporte, setCostoTransporte) = remember { mutableStateOf(c?.costo_transporte ?: 0) }
    val (comentario, setComentario) = remember { mutableStateOf(c?.comentario ?: "") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost( snackbarHostState) }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                value = nombre,
                onValueChange = { setNombre(it) },
                label = { Text(text = stringResource(R.string.new_place)) })
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = urlImagen,
                onValueChange = { setUrlImagen(it) },
                label = { Text(text = stringResource(R.string.new_image)) })
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = latLong,
                onValueChange = { setLatLong(it) },
                label = { Text(text = stringResource(R.string.new_ubication)) })
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = if (orden == 0) "" else orden.toString(),
                onValueChange = { setOrden(it.toIntOrNull() ?: 0) },
                label = { Text(text = stringResource(R.string.new_order)) })
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = if (costoAlojamiento == 0) "" else costoAlojamiento.toString(),
                onValueChange = { setCostoAlojamiento(it.toIntOrNull() ?: 0) },
                label = { Text(text = stringResource(R.string.new_accommodation)) })
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = if (costoTransporte == 0) "" else costoTransporte.toString(),
                onValueChange = { setCostoTransporte(it.toIntOrNull() ?: 0) },
                label = { Text(text = stringResource(R.string.new_transport)) })
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = comentario,
                onValueChange = { setComentario(it) },
                label = { Text(text = stringResource(R.string.new_comment)) })
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val dao = AppDataBase.getInstance(contexto).lugarDao()
                    val lugar = Lugar(
                        id = c?.id ?: 0,
                        nombre = nombre,
                        orden = orden,
                        url_imagen = urlImagen,
                        lat_long = latLong,
                        imagen = c?.imagen,
                        costo_alojamiento = costoAlojamiento,
                        costo_transporte = costoTransporte,
                        comentario = comentario
                    )
                    if (lugar.id > 0) {
                        dao.update(lugar)
                    } else {
                        dao.insert(lugar)
                    }
                    snackbarHostState.showSnackbar("Se ha guardado el lugar ${lugar.nombre}")
                    onSave()
                }
            }) {
                Text(text = stringResource(R.string.button_save))
            }
        }
    }
}

interface ApiService {
    @GET("api")
    fun getDolarExchangeRate(): Call<DolarResponse>
}
data class DolarResponse(
    val dolar: Indicador
)

data class Indicador(
    val codigo: String,
    val nombre: String,
    val unidad_medida: String,
    val fecha: String,
    val valor: Double
)
val retrofit = Retrofit.Builder()
    .baseUrl("https://mindicador.cl/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
fun obtenerTasaDeCambio(onResponse: (Double) -> Unit, onError: (String) -> Unit) {
    val call = apiService.getDolarExchangeRate()

    call.enqueue(object : Callback<DolarResponse> {
        override fun onResponse(call: Call<DolarResponse>, response: Response<DolarResponse>) {
            if (response.isSuccessful) {
                val dolarData = response.body()?.dolar
                if (dolarData != null) {
                    val tasaDeCambio = dolarData.valor
                    onResponse(tasaDeCambio)
                } else {
                    onError("Respuesta de API inválida")
                }
            } else {
                onError("Error en la solicitud: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<DolarResponse>, t: Throwable) {
            onError("Error de red: ${t.message}")
        }
    })
}

private fun cargarTasaDeCambio(onSuccess: (Double) -> Unit, onError: () -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://mindicador.cl/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    obtenerTasaDeCambio(
        onResponse = { tasaDeCambio ->
            onSuccess(tasaDeCambio)
        },
        onError = { errorMessage ->
            onError()
        }
    )
}