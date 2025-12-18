package com.example.softwareengineerthree.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.softwareengineerthree.PokemonUiState
import com.example.softwareengineerthree.data.PokemonDetail
import com.example.softwareengineerthree.data.PokemonSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonScreen(
    state: PokemonUiState,
    onQueryChanged: (String) -> Unit,
    onRetry: () -> Unit,
    onPokemonSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Compose Pokedex") },
                actions = {
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by name or id") },
                singleLine = true
            )
            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry
                )
                else -> PokemonList(
                    pokemons = state.filteredItems,
                    onPokemonSelected = onPokemonSelected,
                    modifier = Modifier.weight(1f, fill = true)
                )
            }
            PokemonDetailCard(
                detail = state.selected,
                isLoading = state.isDetailLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.width(8.dp))
        AssistChip(onClick = onRetry, label = { Text("Try again") })
    }
}

@Composable
private fun PokemonList(
    pokemons: List<PokemonSummary>,
    onPokemonSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(pokemons, key = { it.id }) { pokemon ->
            PokemonRow(pokemon = pokemon, onClick = { onPokemonSelected(pokemon.name) })
        }
    }
}

@Composable
private fun PokemonRow(pokemon: PokemonSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = "${pokemon.name} sprite",
            modifier = Modifier.height(64.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = "#${pokemon.id}")
        }
    }
}

@Composable
private fun PokemonDetailCard(
    detail: PokemonDetail?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { CircularProgressIndicator() }

        detail == null -> Text(
            text = "Tap a Pokemon to view details.",
            style = MaterialTheme.typography.bodyMedium
        )

        else -> Card(modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = detail.spriteUrl,
                        contentDescription = "${detail.name} sprite",
                        modifier = Modifier.height(96.dp),
                        contentScale = ContentScale.Fit
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = detail.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Height: ${detail.height} | Weight: ${detail.weight}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Types",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    detail.types.forEach { type ->
                        AssistChip(
                            onClick = {},
                            label = { Text(type) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}
