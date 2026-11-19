package ar.jornadas.ui

import android.app.Application
import androidx.lifecycle.*
import ar.jornadas.JornadasApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class ProgramaModel(app: Application, private val estado: SavedStateHandle) : AndroidViewModel(app) {
    private val servicios = (app as JornadasApp).servicios
    val mesa = servicios.mesa
    private val repo = servicios.agenda
    val charlas = repo.programa.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val hayEdicion = repo.hayEdicion.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val dia = estado.getStateFlow("dia", 0)
    val tema = estado.getStateFlow("tema", "Todos")
    val aviso = MutableStateFlow("")
    val cargando = MutableStateFlow(false)

    private var peticion: Job? = null
    private var pendiente = false

    fun iniciar() {
        if (servicios.tomarCargaAutomatica()) recargar()
    }

    fun recargar() {
        if (peticion?.isActive == true) {
            pendiente = true
            return
        }
        peticion = viewModelScope.launch {
            cargando.value = true
            try {
                do {
                    pendiente = false
                    aviso.value = ""
                    try {
                        repo.sincronizar()
                    } catch (e: IOException) {
                        aviso.value = "No se pudo consultar el programa"
                    }
                } while (pendiente)
            } finally {
                cargando.value = false
            }
        }
    }

    fun elegirDia(valor: Int) { estado["dia"] = valor }

    fun elegirTema(valor: String) { estado["tema"] = valor }

    fun marcar(id: String) { viewModelScope.launch { repo.cambiarFavorita(id) } }
}
