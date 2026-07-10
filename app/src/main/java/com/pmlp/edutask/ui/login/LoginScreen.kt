package com.pmlp.edutask.ui.login

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.ui.theme.EduTaskTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LoginScreen(onLoginSuccess: (String, String, RolUsuario) -> Unit = { _, _, _ -> }) {
    val context   = LocalContext.current
    val winSize   = calculateWindowSizeClass(activity = context as Activity)
    val isCompact = winSize.widthSizeClass == WindowWidthSizeClass.Compact

    var matricula       by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passVisible     by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf<String?>(null) }
    val focusMgr        = LocalFocusManager.current
    val scroll          = rememberScrollState()
    val scope           = rememberCoroutineScope()
    val db              = remember { FirebaseFirestore.getInstance() }

    fun doLogin() {
        if (matricula.isBlank() || password.isBlank()) return

        scope.launch {
            isLoading = true
            errorMsg = null

            try {
                // Buscar usuario por matricula en Firestore
                val snapshot = db.collection("usuarios")
                    .whereEqualTo("matricula", matricula.trim())
                    .get()
                    .await()

                if (snapshot.isEmpty) {
                    errorMsg = "No se encontro un usuario con esa matricula."
                    isLoading = false
                    return@launch
                }

                val doc = snapshot.documents[0]
                val contrasenaFirestore = doc.getString("contrasena") ?: ""

                if (password != contrasenaFirestore) {
                    errorMsg = "Contrasena incorrecta."
                    isLoading = false
                    return@launch
                }

                // Mapear el rol del documento al enum
                val rolString = doc.getString("rol") ?: "Alumno"
                val rol = when (rolString) {
                    "Profesor"     -> RolUsuario.Profesor
                    "Coordinador"  -> RolUsuario.Coordinador
                    else           -> RolUsuario.Alumno
                }

                isLoading = false
                onLoginSuccess(rol)

            } catch (e: Exception) {
                isLoading = false
                errorMsg = "Error de conexion. Verifica tu internet."
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor   = MaterialTheme.colorScheme.onBackground
    ) { pad ->
        Box(
            modifier         = Modifier.fillMaxSize().padding(pad).verticalScroll(scroll),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .then(if (isCompact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 480.dp))
                    .padding(horizontal = if (isCompact) 24.dp else 0.dp)
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    color    = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.School, null, Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("EduTask", style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text("Plataforma Academica", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(40.dp))

                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    colors    = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Iniciar Sesion", style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface)

                        OutlinedTextField(
                            value = matricula, onValueChange = { matricula = it; errorMsg = null },
                            modifier = Modifier.fillMaxWidth(), label = { Text("Matricula") },
                            placeholder = { Text("Ej. A12345") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Matricula") },
                            isError = errorMsg != null, singleLine = true, enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusMgr.moveFocus(FocusDirection.Down) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                                cursorColor          = MaterialTheme.colorScheme.primary
                            ), shape = MaterialTheme.shapes.medium
                        )

                        OutlinedTextField(
                            value = password, onValueChange = { password = it; errorMsg = null },
                            modifier = Modifier.fillMaxWidth(), label = { Text("Contrasena") },
                            leadingIcon  = { Icon(Icons.Default.Lock, contentDescription = "Contrasena") },
                            enabled = !isLoading,
                            trailingIcon = {
                                IconButton(onClick = { passVisible = !passVisible }) {
                                    Icon(if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passVisible) "Ocultar" else "Mostrar")
                                }
                            },
                            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = errorMsg != null, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus(); doLogin() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                                cursorColor          = MaterialTheme.colorScheme.primary
                            ), shape = MaterialTheme.shapes.medium
                        )

                        AnimatedVisibility(visible = errorMsg != null, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ErrorOutline, "Error", Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error)
                                Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick  = { focusMgr.clearFocus(); doLogin() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled  = !isLoading && matricula.isNotBlank() && password.isNotBlank(),
                            colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape    = MaterialTheme.shapes.large
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(Modifier.size(20.dp), MaterialTheme.colorScheme.onPrimary, 2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Validando...", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Text("Iniciar Sesion", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TextButton(onClick = {}) {
                                Text("Olvidaste tu contrasena?", style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("El rol se asigna automaticamente segun tus credenciales institucionales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview(name = "Login Movil", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginPreviewMovil() {
    EduTaskTheme(darkTheme = false, dynamicColor = false) { LoginScreen() }
}

@Preview(name = "Login Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun LoginPreviewTablet() {
    EduTaskTheme(darkTheme = false, dynamicColor = false) { LoginScreen() }
}