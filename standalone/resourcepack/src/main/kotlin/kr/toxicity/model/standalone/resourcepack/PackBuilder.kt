package kr.toxicity.model.standalone.resourcepack

import java.util.concurrent.ConcurrentHashMap

class PackBuilder internal constructor(
    private val assets: PackAssets,
    private val path: PackPath,
) {
    private val obfuscator: PackObfuscator = PackObfuscator.order(enabled = assets.settings.useObfuscation)

    fun resolve(vararg paths: String): PackBuilder = PackBuilder(assets = assets, path = path.resolve(*paths))

    fun add(path: String, estimatedSize: Long = -1, supplier: ByteSupplier) {
        add(paths = arrayOf(path), estimatedSize = estimatedSize, supplier = supplier)
    }

    fun add(paths: Array<String>, estimatedSize: Long = -1, supplier: ByteSupplier) {
        val resolved = path.resolve(*paths)
        assets.resourceMap.putIfAbsent(
            resolved,
            PackResource.of(assets.overlay, resolved, estimatedSize, supplier),
        )
    }

    fun obfuscator(): PackObfuscator = obfuscator
}

class PackNamespace internal constructor(
    assets: PackAssets,
    namespace: String,
) {
    private val subPath = assets.path.resolve("assets", namespace)
    private val items = PackBuilder(assets = assets, path = subPath.resolve("items"))
    private val models = PackBuilder(assets = assets, path = subPath.resolve("models"))
    private val textures = PackBuilder(assets = assets, path = subPath.resolve("textures", "item"))

    fun items(): PackBuilder = items
    fun models(): PackBuilder = models
    fun textures(): PackBuilder = textures
}

class PackAssets internal constructor(
    val overlay: PackOverlay,
    internal val settings: ResourcePackSettings,
) {
    val path: PackPath = overlay.path(settings.namespace)
    internal val resourceMap = ConcurrentHashMap<PackPath, PackResource>()

    private val pluginNamespace = PackNamespace(this, settings.namespace)
    private val minecraft = PackNamespace(this, "minecraft")

    fun namespace(): PackNamespace = pluginNamespace
    fun minecraft(): PackNamespace = minecraft

    fun add(path: String, estimatedSize: Long = -1, supplier: ByteSupplier) {
        add(paths = arrayOf(path), estimatedSize = estimatedSize, supplier = supplier)
    }

    fun add(paths: Array<String>, estimatedSize: Long = -1, supplier: ByteSupplier) {
        val resolved = this.path.resolve(*paths)
        resourceMap.putIfAbsent(
            resolved,
            PackResource.of(overlay, resolved, estimatedSize, supplier),
        )
    }

    internal fun size(): Int = resourceMap.size
    internal fun dirty(): Boolean = size() > 0
}
