package com.example.softwareengineerthree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.softwareengineerthree.data.PokemonDetail
import com.example.softwareengineerthree.data.PokemonRepository
import com.example.softwareengineerthree.data.PokemonSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PokemonUiState(
    val isLoading: Boolean = true,
    val isDetailLoading: Boolean = false,
    val query: String = "",
    val items: List<PokemonSummary> = emptyList(),
    val filteredItems: List<PokemonSummary> = emptyList(),
    val selected: PokemonDetail? = null,
    val errorMessage: String? = null
)

class PokemonViewModel(
    private val repository: PokemonRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonUiState())
    val state: StateFlow<PokemonUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runCatching {
                withContext(ioDispatcher) { repository.getPokemonPage() }
            }
            result.onSuccess { pokedex ->
                val sorted = pokedex.sortedBy { it.id }
                _state.update { current ->
                    val filtered = filterPokemon(sorted, current.query)
                    current.copy(
                        isLoading = false,
                        items = sorted,
                        filteredItems = filtered,
                        errorMessage = null,
                        selected = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load Pokemon right now."
                    )
                }
            }
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        applyFilter()
    }

    fun onPokemonSelected(name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isDetailLoading = true, errorMessage = null) }
            val result = runCatching {
                val normalized = name.trim().lowercase()
                withContext(ioDispatcher) { repository.getPokemonDetail(normalized) }
            }
            result.onSuccess { detail ->
                _state.update {
                    it.copy(
                        isDetailLoading = false,
                        selected = detail,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isDetailLoading = false,
                        errorMessage = error.message ?: "Unable to load that Pokemon."
                    )
                }
            }
        }
    }

    private fun applyFilter() {
        _state.update { current ->
            val filtered = filterPokemon(current.items, current.query)
            current.copy(filteredItems = filtered)
        }
    }

    private fun filterPokemon(list: List<PokemonSummary>, query: String): List<PokemonSummary> {
        if (query.isBlank()) return list
        val lower = query.trim().lowercase()
        return list.filter { summary ->
            summary.name.lowercase().contains(lower) || summary.id.toString() == lower
        }
    }
}

class PokemonViewModelFactory(
    private val repository: PokemonRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokemonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PokemonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
