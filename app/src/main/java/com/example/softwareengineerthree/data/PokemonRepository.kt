package com.example.softwareengineerthree.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

data class PokemonSummary(
    val id: Int,
    val name: String,
    val imageUrl: String
)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<String>,
    val spriteUrl: String
)

interface PokemonRepository {
    suspend fun getPokemonPage(limit: Int = 50, offset: Int = 0): List<PokemonSummary>
    suspend fun getPokemonDetail(name: String): PokemonDetail
}

class DefaultPokemonRepository(
    private val service: PokeApiService
) : PokemonRepository {

    override suspend fun getPokemonPage(limit: Int, offset: Int): List<PokemonSummary> =
        withContext(Dispatchers.IO) {
            val response = service.getPokemonPage(limit = limit, offset = offset)
            response.results.mapNotNull { resource ->
                runCatching {
                    val id = parsePokemonIdFromUrl(resource.url)
                    PokemonSummary(
                        id = id,
                        name = resource.name.replaceFirstChar { it.uppercase() },
                        imageUrl = spriteUrlFor(id)
                    )
                }.getOrNull()
            }
        }

    override suspend fun getPokemonDetail(name: String): PokemonDetail =
        withContext(Dispatchers.IO) {
            val detail = service.getPokemonDetail(name.lowercase())
            PokemonDetail(
                id = detail.id,
                name = detail.name.replaceFirstChar { it.uppercase() },
                height = detail.height,
                weight = detail.weight,
                types = detail.types.sortedBy { it.slot }.map { it.type.name },
                spriteUrl = detail.sprites.frontDefault ?: spriteUrlFor(detail.id)
            )
        }
}

private fun parsePokemonIdFromUrl(url: String): Int {
    val clean = url.trimEnd('/')
    val idPart = clean.substringAfterLast("/")
    return idPart.toIntOrNull()
        ?: throw IllegalArgumentException("Cannot parse pokemon id from url: $url")
}

object PokemonRepositoryProvider {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    fun createRepository(): PokemonRepository {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return DefaultPokemonRepository(retrofit.create(PokeApiService::class.java))
    }
}

// Visible for tests
internal fun pokemonSpriteUrlFromResource(resource: NamedApiResource): PokemonSummary {
    val id = parsePokemonIdFromUrl(resource.url)
    return PokemonSummary(id = id, name = resource.name, imageUrl = spriteUrlFor(id))
}
