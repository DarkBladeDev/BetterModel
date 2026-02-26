# standalone:resourcepack

Librería standalone (Kotlin/JVM, Java 21) que recrea la lógica de generación de resourcepacks de BetterModel sin depender de Bukkit/Fabric/NMS.

## Qué incluye

- API para construir assets por namespace (`items`, `models`, `textures/item`).
- Soporte de overlays (`default`, `legacy`, `modern`) con inclusión condicional.
- Generación de `pack.mcmeta` con `supported_formats`, `min_format` y `max_format`.
- Obfuscación opcional de nombres siguiendo la estrategia por orden usada en BetterModel.
- Resultado in-memory para integrarlo en cualquier plugin/sistema de publicación.

## Uso rápido

```kotlin
val zipper = ResourcePackLibrary.zipper(
    ResourcePackSettings(namespace = "myplugin", useObfuscation = true)
)

zipper.defaultOverlay.namespace().models().add("my_item.json") {
    """{"parent":"minecraft:item/generated"}""".toByteArray()
}

val build = zipper.build(icon = iconBytes)
val resources = build.resources
```

## Notas

- El módulo se puede consumir como dependencia Gradle (`standalone:resourcepack`).
- No escribe ZIP en disco por sí solo; entrega recursos y metadatos para que el plugin consumidor decida el empaquetado.
