package com.example.softwareengineerthree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.softwareengineerthree.data.PokemonRepositoryProvider
import com.example.softwareengineerthree.ui.PokemonScreen
import com.example.softwareengineerthree.ui.theme.SoftwareengineerthreeTheme
import com.example.softwareengineerthree.PokemonUiState

class MainActivity : ComponentActivity() {
    private val viewModel: PokemonViewModel by viewModels {
        PokemonViewModelFactory(PokemonRepositoryProvider.createRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SoftwareengineerthreeTheme {
                PokemonApp(viewModel)
            }
        }
    }
}

@Composable
fun PokemonApp(viewModel: PokemonViewModel) {
    val state by viewModel.state.collectAsState()
    PokemonScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onRetry = viewModel::refresh,
        onPokemonSelected = viewModel::onPokemonSelected
    )
}

@Preview(showBackground = true)
@Composable
fun PokemonAppPreview() {
    SoftwareengineerthreeTheme {
        val fakeState = PokemonUiState(
            isLoading = false,
            items = emptyList(),
            filteredItems = emptyList(),
            selected = null,
            errorMessage = null
        )
        PokemonScreen(
            state = fakeState,
            onQueryChanged = {},
            onRetry = {},
            onPokemonSelected = {}
        )
    }
}
