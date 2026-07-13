package com.pmlp.edutask.ui.coordinador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class CoordinadorUiState {
    object Idle : CoordinadorUiState()
    object Loading : CoordinadorUiState()
    data class Success(val usuarios: List<Usuario>) : CoordinadorUiState()
    data class Error(val message: String) : CoordinadorUiState()
}

class CoordinadorViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<CoordinadorUiState>(CoordinadorUiState.Idle)
    val uiState: StateFlow<CoordinadorUiState> = _uiState

    init {
        fetchUsuarios()
    }

    fun fetchUsuarios() {
        viewModelScope.launch {
            _uiState.value = CoordinadorUiState.Loading
            try {
                val snapshot = db.collection("usuarios").get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    val rolString = doc.getString("rol") ?: "Alumno"
                    val rolEnum = try {
                        RolUsuario.valueOf(rolString)
                    } catch (e: Exception) {
                        RolUsuario.Alumno
                    }
                    Usuario(
                        idUsuario = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        matricula = doc.getString("matricula") ?: "",
                        correo = doc.getString("correo") ?: "",
                        contrasena = doc.getString("contrasena") ?: "",
                        rol = rolEnum
                    )
                }
                _uiState.value = CoordinadorUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = CoordinadorUiState.Error(e.message ?: "Error al obtener usuarios")
            }
        }
    }

    fun saveUsuario(usuario: Usuario, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                var finalIdUsuario = usuario.idUsuario
                var finalMatricula = usuario.matricula

                if (finalIdUsuario.isEmpty()) {
                    val initials = usuario.nombre.trim().split("\\s+".toRegex())
                        .filter { it.isNotEmpty() }
                        .map { it.first().uppercaseChar() }
                        .joinToString("")
                    val prefix = when (usuario.rol) {
                        RolUsuario.Alumno -> "A-"
                        RolUsuario.Profesor -> "P-"
                        RolUsuario.Coordinador -> "C-"
                    }
                    finalIdUsuario = "$prefix$initials"
                    finalMatricula = "$prefix$initials"
                }

                val map = mapOf(
                    "nombre" to usuario.nombre,
                    "matricula" to finalMatricula,
                    "correo" to usuario.correo,
                    "contrasena" to usuario.contrasena,
                    "rol" to usuario.rol.name
                )

                db.collection("usuarios").document(finalIdUsuario).set(map).await()
                fetchUsuarios()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al guardar usuario")
            }
        }
    }

    fun deleteUsuario(idUsuario: String) {
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(idUsuario).delete().await()
                fetchUsuarios()
            } catch (e: Exception) {
                _uiState.value = CoordinadorUiState.Error(e.message ?: "Error al eliminar usuario")
            }
        }
    }
}
