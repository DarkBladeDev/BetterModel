package kr.toxicity.model.standalone.resourcepack

/**
 * Standalone entrypoint for building Minecraft resource-pack assets.
 */
object ResourcePackLibrary {
    fun zipper(settings: ResourcePackSettings = ResourcePackSettings()): PackZipper = PackZipper(settings)
}
