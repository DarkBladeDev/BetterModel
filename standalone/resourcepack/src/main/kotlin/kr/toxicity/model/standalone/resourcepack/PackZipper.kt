package kr.toxicity.model.standalone.resourcepack

import java.io.File
import java.security.MessageDigest
import java.util.Collections
import java.util.TreeMap
import java.util.TreeSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.asStream

class PackZipper internal constructor(
    private val settings: ResourcePackSettings,
) {
    private val metaBuilder = PackMeta.Builder(settings)
    private val overlayMap = ConcurrentHashMap<PackOverlay, PackAssets>()

    val defaultOverlay: PackAssets get() = overlay(PackOverlay.defaults(settings)[0])
    val legacy: PackAssets get() = overlay(PackOverlay.defaults(settings)[1])
    val modern: PackAssets get() = overlay(PackOverlay.defaults(settings)[2])

    fun overlay(overlay: PackOverlay): PackAssets =
        overlayMap.computeIfAbsent(overlay) { PackAssets(overlay = it, settings = settings) }

    fun metaBuilder(): PackMeta.Builder = metaBuilder

    fun build(icon: ByteArray? = null): BuildData {
        val resources = ArrayList<PackResource>(size())
        overlayMap.entries.forEach { (overlay, assets) ->
            if (overlay.test() && assets.dirty()) {
                resources += assets.resourceMap.values
                overlay.range?.let { metaBuilder.overlayEntry(PackMeta.OverlayEntry(it, assets.path.path)) }
            }
            assets.resourceMap.clear()
        }
        val meta = metaBuilder.build()
        resources += meta.toResource()
        icon?.let {
            resources += PackResource.of(PACK_ICON, it.size.toLong()) { it }
        }
        return BuildData(meta = meta, resources = resources)
    }

    fun size(): Int = overlayMap.values.sumOf { it.size() } + 2

    data class BuildData(val meta: PackMeta, val resources: List<PackResource>)

    companion object {
        private val PACK_ICON = PackPath("pack.png")
    }
}

class PackResult(
    private val meta: PackMeta,
    private val directory: File?,
) {
    private val overlays = TreeMap<PackOverlay, MutableSet<PackByte>>()
    private val assets = TreeSet<PackByte>()
    private val assetsView: Set<PackByte> = Collections.unmodifiableSet(assets)

    private val creationTime = System.currentTimeMillis()
    private var frozen = false
    private var changed = false
    private var uuid: UUID? = null

    fun set(overlay: PackOverlay?, packByte: PackByte) {
        check(!frozen) { "result is frozen." }
        if (overlay == null) {
            synchronized(assets) { assets += packByte }
        } else {
            synchronized(overlays) { overlays.computeIfAbsent(overlay) { TreeSet() } += packByte }
        }
    }

    fun freeze(changed: Boolean = false) {
        check(!frozen) { "result is frozen." }
        frozen = true
        this.changed = changed
    }

    fun changed(): Boolean = changed
    fun meta(): PackMeta = meta
    fun directory(): File? = directory

    fun hash(): UUID {
        uuid?.let { return it }
        synchronized(this) {
            uuid?.let { return it }
            return try {
                val sha = MessageDigest.getInstance("SHA-256")
                stream().map { it.bytes }.forEach(sha::update)
                UUID.nameUUIDFromBytes(sha.digest()).also { uuid = it }
            } catch (_: Exception) {
                UUID.randomUUID().also { uuid = it }
            }
        }
    }

    fun size(): Int = assets.size + overlays.values.sumOf { it.size }
    fun time(): Long = System.currentTimeMillis() - creationTime

    fun overlays(overlay: PackOverlay): Set<PackByte> =
        overlays[overlay]?.let(Collections::unmodifiableSet) ?: emptySet()

    fun stream() = (overlays.values.asSequence().flatten() + assets.asSequence()).asStream()

    fun assets(): Set<PackByte> = assetsView
}
