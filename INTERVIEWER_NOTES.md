# Interviewer Notes (Private)

Purpose: there are three intentional failures in `app/src/test/java/com/example/softwareengineerthree/PokemonInterviewFailuresTest.kt`. The candidate should spend ~1 hour fixing the app code (not the tests) to make them pass.

## Failing cases and where to nudge
1) `refresh should return pokedex sorted alphabetically by name`
   - Current behavior sorts by id in `PokemonViewModel.refresh()`.
   - Nudge: “The list order in UI should be alphabetical — check the refresh logic.”

2) `filter should match when query is id prefix`
   - Current filter matches only exact id string or name. See `filterPokemon`.
   - Nudge: “Search should find ‘25’ when typing ‘2’. Consider how ids are compared.”

3) `onPokemonSelected failure should clear previous selection`
   - On a failed detail fetch we leave the old `selected` intact. See `onPokemonSelected` failure path.
   - Nudge: “If detail load fails, we shouldn’t show stale details.”

## Suggested interview flow (if candidate gets stuck)
- Have them run `GRADLE_USER_HOME=./.gradle ./gradlew test` to see the red tests.
- Ask them to open `PokemonInterviewFailuresTest` first, then `PokemonViewModel.kt`.
- Encourage incremental fixes and re-running tests; UI changes aren’t required for passing.
- If time remains, ask for a brief explanation of their fixes and any tradeoffs.

## Housekeeping
- Do not let candidates edit tests.
- Keep the app runnable; network calls hit https://pokeapi.co/ (INTERNET permission already set).
