package com.example.softwareengineerthree.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val SPRITE_BASE =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon"

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonPage(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    @GET("pokemon/{name}")
    suspend fun getPokemonDetail(
        @Path("name") name: String
    ): PokemonDetailResponse
}

@JsonClass(generateAdapter = true)
data class PokemonListResponse(
    @Json(name = "results") val results: List<NamedApiResource> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NamedApiResource(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class PokemonDetailResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "height") val height: Int,
    @Json(name = "weight") val weight: Int,
    @Json(name = "types") val types: List<TypeSlot>,
    @Json(name = "sprites") val sprites: Sprites
)

@JsonClass(generateAdapter = true)
data class TypeSlot(
    @Json(name = "slot") val slot: Int,
    @Json(name = "type") val type: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class Sprites(
    @Json(name = "front_default") val frontDefault: String? = null
)

fun spriteUrlFor(pokemonId: Int): String = "$SPRITE_BASE/$pokemonId.png"
