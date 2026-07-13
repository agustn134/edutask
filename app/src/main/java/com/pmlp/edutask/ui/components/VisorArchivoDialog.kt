package com.pmlp.edutask.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisorArchivoDialog(
    base64String: String,
    nombreArchivo: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Para imágenes
    var bitmapImage by remember { mutableStateOf<Bitmap?>(null) }
    // Para PDFs
    var pdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    val isPdf = nombreArchivo.endsWith(".pdf", ignoreCase = true) || base64String.startsWith("JVBERi0") // Comienzo común de PDF en base64

    LaunchedEffect(base64String) {
        isLoading = true
        error = null
        try {
            withContext(Dispatchers.IO) {
                // Limpiar string de Base64 si contiene prefijos (ej: data:image/png;base64,)
                val cleanString = if (base64String.contains(",")) {
                    base64String.substring(base64String.indexOf(",") + 1)
                } else {
                    base64String
                }
                
                val bytes = Base64.decode(cleanString, Base64.DEFAULT)
                
                if (isPdf) {
                    // Procesar PDF
                    val tempFile = File.createTempFile("visor_temp", ".pdf", context.cacheDir)
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    
                    val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val pdfRenderer = PdfRenderer(fileDescriptor)
                    val pages = mutableListOf<Bitmap>()
                    
                    val screenWidth = context.resources.displayMetrics.widthPixels
                    
                    for (i in 0 until pdfRenderer.pageCount) {
                        val page = pdfRenderer.openPage(i)
                        // Escalar el PDF para mantener calidad
                        val scale = screenWidth.toFloat() / page.width.toFloat()
                        val bmp = Bitmap.createBitmap(
                            (page.width * scale).toInt(),
                            (page.height * scale).toInt(),
                            Bitmap.Config.ARGB_8888
                        )
                        // Fondo blanco para el PDF
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        pages.add(bmp)
                        page.close()
                    }
                    pdfRenderer.close()
                    fileDescriptor.close()
                    tempFile.delete()
                    pdfPages = pages
                } else {
                    // Procesar Imagen
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        bitmapImage = bmp
                    } else {
                        error = "No se pudo decodificar la imagen"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error = "Error al abrir archivo: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                TopAppBar(
                    title = { Text(nombreArchivo, style = MaterialTheme.typography.titleMedium, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar visor")
                        }
                    }
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Cargando archivo...", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else if (error != null) {
                        Text(
                            text = error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        if (isPdf && pdfPages.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(pdfPages.size) { index ->
                                    Image(
                                        bitmap = pdfPages[index].asImageBitmap(),
                                        contentDescription = "Página ${index + 1} de PDF",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }
                        } else if (bitmapImage != null) {
                            Image(
                                bitmap = bitmapImage!!.asImageBitmap(),
                                contentDescription = "Imagen de evidencia",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("No se pudo cargar el contenido", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
