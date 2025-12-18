package com.example.softwareengineerthree

import com.example.softwareengineerthree.data.PokemonDetail
import com.example.softwareengineerthree.data.PokemonRepository
import com.example.softwareengineerthree.data.PokemonSummary
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Three intentionally failing tests for interview candidates.
 * Each failing expectation corresponds to a production bug to fix.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PokemonInterviewFailuresTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `refresh should return pokedex sorted alphabetically by name`() =
        runTest(dispatcherRule.dispatcher) {
            val repository = InMemoryRepository(
                page = listOf(
                    PokemonSummary(id = 3, name = "Zubat", imageUrl = "zubat"),
                    PokemonSummary(id = 2, name = "Abra", imageUrl = "abra"),
                    PokemonSummary(id = 1, name = "Mew", imageUrl = "mew"),
                ),
                detail = sampleDetail()
            )

            val viewModel = PokemonViewModel(repository, dispatcherRule.dispatcher)
            advanceUntilIdle()

            val names = viewModel.state.value.items.map { it.name }
            assertThat(names).containsExactly("Abra", "Mew", "Zubat").inOrder()
        }

    @Test
    fun `filter should match when query is id prefix`() =
        runTest(dispatcherRule.dispatcher) {
            val repository = InMemoryRepository(
                page = listOf(
                    PokemonSummary(id = 25, name = "Pikachu", imageUrl = "pikachu"),
                    PokemonSummary(id = 4, name = "Charmander", imageUrl = "charmander"),
                ),
                detail = sampleDetail()
            )

            val viewModel = PokemonViewModel(repository, dispatcherRule.dispatcher)
            advanceUntilIdle()

            viewModel.onQueryChanged("2")
            advanceUntilIdle()

            val filtered = viewModel.state.value.filteredItems.map { it.name }
            assertThat(filtered).containsExactly("Pikachu")
        }

    @Test
    fun `refresh failure should clear existing items and filtered items`() =
        runTest(dispatcherRule.dispatcher) {
            val repository = FailingRepository(sampleDetail())

            val viewModel = PokemonViewModel(repository, dispatcherRule.dispatcher)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertThat(state.items).isEmpty()
            assertThat(state.filteredItems).isEmpty()
            assertThat(state.errorMessage).isNotEmpty()
        }

    @Test
    fun `onPokemonSelected failure should clear previous selection`() =
        runTest(dispatcherRule.dispatcher) {
            val repository = TrackingRepository(sampleDetail(), shouldFailNext = true)

            val viewModel = PokemonViewModel(repository, dispatcherRule.dispatcher)
            advanceUntilIdle()

            // First set a selection
            repository.shouldFailNext = false
            viewModel.onPokemonSelected("bulbasaur")
            advanceUntilIdle()

            // Next call fails and should clear selection
            repository.shouldFailNext = true
            viewModel.onPokemonSelected("charmander")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertThat(state.selected).isNull()
            assertThat(state.errorMessage).isNotEmpty()
        }

    private fun sampleDetail() = PokemonDetail(
        id = 1,
        name = "Bulbasaur",
        height = 7,
        weight = 69,
        types = listOf("grass", "poison"),
        spriteUrl = "sprite"
    )
}

private class TrackingRepository(
    private val detail: PokemonDetail,
    var shouldFailNext: Boolean = false
) : PokemonRepository {
    var lastRequestedName: String? = null
        private set

    override suspend fun getPokemonPage(limit: Int, offset: Int): List<PokemonSummary> = emptyList()

    override suspend fun getPokemonDetail(name: String): PokemonDetail {
        lastRequestedName = name
        if (shouldFailNext) {
            shouldFailNext = false
            throw IllegalStateException("boom")
        }
        return detail
    }
}

private class InMemoryRepository(
    private val page: List<PokemonSummary>,
    private val detail: PokemonDetail
) : PokemonRepository {
    override suspend fun getPokemonPage(limit: Int, offset: Int): List<PokemonSummary> = page
    override suspend fun getPokemonDetail(name: String): PokemonDetail = detail
}

private class FailingRepository(
    private val detail: PokemonDetail
) : PokemonRepository {
    override suspend fun getPokemonPage(limit: Int, offset: Int): List<PokemonSummary> {
        throw IllegalStateException("network down")
    }

    override suspend fun getPokemonDetail(name: String): PokemonDetail = detail
}
