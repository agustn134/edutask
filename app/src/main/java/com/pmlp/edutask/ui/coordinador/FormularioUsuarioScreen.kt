package com.pmlp.edutask.ui.coordinador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioUsuarioScreen(
    viewModel: CoordinadorViewModel,
    idUsuario: String?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = idUsuario != null

    var nombre by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.Alumno) }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val generatedMatricula = remember(nombre, rolSeleccionado) {
        val roleLetter = when (rolSeleccionado) {
            RolUsuario.Alumno -> "A"
            RolUsuario.Profesor -> "P"
            RolUsuario.Coordinador -> "C"
        }
        val initials = nombre.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
        if (initials.isEmpty()) "" else "$roleLetter-$initials"
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idUsuario, uiState) {
        if (isEdit && uiState is CoordinadorUiState.Success) {
            val user = (uiState as CoordinadorUiState.Success).usuarios.find { it.idUsuario == idUsuario }
            if (user != null) {
                nombre = user.nombre
                matricula = user.matricula
                correo = user.correo
                contrasena = user.contrasena
                rolSeleccionado = user.rol
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Usuario" else "Nuevo Usuario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMsg != null) {
                Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = if (isEdit) matricula else generatedMatricula,
                onValueChange = { if (isEdit) matricula = it },
                label = { Text("Matrícula") },
                readOnly = !isEdit,
                supportingText = if (!isEdit) { { Text("Generada automáticamente: Rol - Iniciales") } } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = rolSeleccionado.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    RolUsuario.entries.forEach { rol ->
                        DropdownMenuItem(
                            text = { Text(rol.name) },
                            onClick = {
                                rolSeleccionado = rol
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalMatricula = if (isEdit) matricula else generatedMatricula
                    if (nombre.isBlank() || finalMatricula.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                        errorMsg = "Todos los campos son obligatorios"
                        return@Button
                    }
                    isLoading = true
                    errorMsg = null
                    val usuario = Usuario(
                        idUsuario = idUsuario ?: "",
                        nombre = nombre,
                        matricula = finalMatricula,
                        correo = correo,
                        contrasena = contrasena,
                        rol = rolSeleccionado
                    )
                    viewModel.saveUsuario(
                        usuario = usuario,
                        onSuccess = {
                            isLoading = false
                            onBack()
                        },
                        onError = {
                            isLoading = false
                            errorMsg = it
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isEdit) "Guardar Cambios" else "Crear Usuario")
                }
            }
        }
    }
}
