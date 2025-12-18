# Compose Pokédex (Interview Exercise)

This project is a simple Jetpack Compose Pokédex that hits the public PokeAPI. It’s wired for an interview exercise with three intentionally failing unit tests. Candidates get 60 minutes to make those tests pass by fixing the app code (do not change the tests).

## What’s broken (see tests)
- `PokemonInterviewFailuresTest` outlines the exact expectations (all currently failing by design):
  - List from `refresh` must be sorted alphabetically.
  - Filtering should work when the query is an id prefix (e.g., "2" matches 25).
  - Selecting a Pokémon should clear previous selection when the fetch fails.

The corresponding bugs live in `app/src/main/java/com/example/softwareengineerthree/PokemonViewModel.kt`.

## How to run tests
```
GRADLE_USER_HOME=./.gradle ./gradlew test
```

## Goal for candidates
1) Read the failing tests in `app/src/test/java/com/example/softwareengineerthree/PokemonInterviewFailuresTest.kt`.
2) Apply code fixes in the main source (not the tests) to satisfy the assertions.
3) Re-run `./gradlew test` until all tests pass.
