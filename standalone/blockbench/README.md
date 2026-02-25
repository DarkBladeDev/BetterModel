# standalone:blockbench

Pequeña librería standalone (Kotlin/JVM, Java 21) para leer y procesar modelos Generic Blockbench (`.bbmodel`) inspirada en el pipeline de BetterModel.

## Qué hace

- Parsea JSON de Blockbench con **Gson** + deserializadores polimórficos.
- Detecta versión de formato (`meta.format_version`) con **semver4j**.
- Convierte datos crudos a una forma procesada (`ProcessedModel`) para uso en otros motores.
- Decodifica texturas embebidas en base64.
- Aplica conversiones de coordenadas por versión (BB5 vs legacy) para animación.

## Uso rápido

```kotlin
val processed = BlockbenchLibrary.process("demon_knight", jsonString)
println(processed.bones.size)
println(processed.animations.map { it.name })
```

## Notas

- Está orientada a ser embebida como librería de parseo/procesado; no depende de Bukkit/Fabric/NMS.
- Maneja elementos principales (`cube`, `locator`, `null_object`, `camera`).
- Las expresiones dinámicas se preservan como texto (`VectorProvider.Expression`) para que el consumidor decida cómo evaluarlas.
