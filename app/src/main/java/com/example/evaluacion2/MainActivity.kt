package com.example.evaluacion2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.evaluacion2.DB.AppDataBase
import com.example.evaluacion2.DB.Producto
import com.example.evaluacion2.ui.theme.Evaluacion2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppProductosUI()
        }
    }
}
enum class Accion {
    LISTAR, CREAR, EDITAR
}

@Composable
fun AppProductosUI(){
    val contexto = LocalContext.current
    val (productos, setProductos) = remember{ mutableStateOf( emptyList<Producto>() ) }
    val (seleccion, setSeleccion) = remember{ mutableStateOf<Producto?>(null) }
    val (accion, setAccion) = remember{ mutableStateOf(Accion.LISTAR) }


    LaunchedEffect(productos) {
        withContext(Dispatchers.IO) {
            val db = AppDataBase.getInstance( contexto )
            setProductos( db.productoDao().getAll() )
        }
    }

    val onSave = {
        val db = AppDataBase.getInstance( contexto )
        setAccion(Accion.LISTAR)
        setProductos( db.productoDao().getAll() )
    }


    when(accion) {
        Accion.CREAR -> ProductoFormUI(null, onSave)
        Accion.EDITAR -> ProductoFormUI(seleccion, onSave)
        else -> ListarProductosUI(
            productos,
            setProductos,
            onAdd = { setAccion( Accion.CREAR ) },
            onEdit = { producto ->
                setSeleccion(producto)
                setAccion( Accion.EDITAR)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListarProductosUI(
    productos:List<Producto>,
    setProductos: (List<Producto>) -> Unit,
    onAdd:() -> Unit = {},
    onEdit:(c:Producto) -> Unit = {}
){

    Scaffold(
        floatingActionButton = {
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
    ) { contentPadding ->
        if( productos.isNotEmpty() ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(productos) { producto ->
                    ProductoItemUI(producto, onEdit = { onEdit(producto) }) {
                        setProductos(productos.filterNot { it.id == producto.id })
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
fun ProductoItemUI(producto:Producto, onEdit:() -> Unit = {}, onDelete: () -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val contexto = LocalContext.current

    Spacer(modifier = Modifier.height(20.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Image(
            painter = painterResource(id = if (producto.disponible) R.drawable.carro else R.drawable.check),
            contentDescription = "Imagen Carro",
            modifier = Modifier.size(width = 20.dp, height = 20.dp)
                .clickable {
                    coroutineScope.launch(Dispatchers.IO) {
                        val dao = AppDataBase.getInstance(contexto).productoDao()
                        val updatedProducto = producto.copy(disponible = false)
                        dao.update(updatedProducto)
                        onDelete()
                    }
                }
        )
        Spacer(modifier = Modifier.weight(1f))
        Column() {
            Text(producto.producto, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Eliminar",
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    coroutineScope.launch(Dispatchers.IO) {
                        val dao = AppDataBase.getInstance(contexto).productoDao()
                        dao.delete(producto)
                        onDelete()
                    }
                }
        )
        Spacer(modifier = Modifier.width(20.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormUI(c:Producto?, onSave:()->Unit = {}){
    val contexto = LocalContext.current
    val (producto, setProducto) = remember { mutableStateOf( c?.producto ?: "" ) }
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
        ) {
            Image(
                painter = painterResource(id = R.drawable.carro),
                contentDescription = "Imagen Carro",
                modifier = Modifier.size(width = 100.dp, height = 100.dp)
            )
            TextField(
                value = producto,
                onValueChange = { setProducto(it) },
                label = { Text(text = stringResource(R.string.new_product)) })
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val dao = AppDataBase.getInstance(contexto).productoDao()
                    val producto = Producto(c?.id ?: 0, producto, disponible = true)
                    if (producto.id > 0) {
                        dao.update(producto)
                    } else {
                        dao.insert(producto)
                    }
                    snackbarHostState.showSnackbar("Se ha guardado el producto ${producto.producto}")
                    onSave()
                }
            }) {
                Text(text = stringResource(R.string.button_create))
            }
        }
    }
}