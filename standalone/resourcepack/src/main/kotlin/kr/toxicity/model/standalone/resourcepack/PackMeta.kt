package kr.toxicity.model.standalone.resourcepack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonSerializer
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName

/**
 * Serializable `pack.mcmeta` representation.
 */
data class PackMeta(
    val pack: Pack,
    val overlays: Overlay? = null,
) {
    fun toJson(): JsonElement = GSON.toJsonTree(this)

    fun toResource(): PackResource {
        val json = GSON.toJson(this)
        return PackResource.of(PATH, 2L * json.length) { json.toByteArray(Charsets.UTF_8) }
    }

    data class Pack(
        @SerializedName("pack_format") val packFormat: Int,
        val description: String,
        @SerializedName("supported_formats") val supportedFormats: VersionRange,
        @SerializedName("min_format") val minFormat: PackVersion,
        @SerializedName("max_format") val maxFormat: PackVersion,
    )

    data class PackVersion(val major: Int, val minor: Int = 0) {
        fun toJson(): JsonElement =
            if (minor <= 0) JsonPrimitive(major)
            else JsonArray(2).apply {
                add(major)
                add(minor)
            }
    }

    data class Overlay(val entries: List<OverlayEntry>)

    data class OverlayEntry(
        val formats: VersionRange,
        val directory: String,
        @SerializedName("min_format") val minFormat: PackVersion = PackVersion(formats.minInclusive),
        @SerializedName("max_format") val maxFormat: PackVersion = PackVersion(formats.maxInclusive),
    )

    data class VersionRange(
        @SerializedName("min_inclusive") val minInclusive: Int,
        @SerializedName("max_inclusive") val maxInclusive: Int,
    ) {
        constructor(value: Int) : this(value, value)

        fun toJson(): JsonElement =
            if (minInclusive == maxInclusive) JsonPrimitive(minInclusive)
            else JsonArray(2).apply {
                add(minInclusive)
                add(maxInclusive)
            }
    }

    class Builder(private val settings: ResourcePackSettings) {
        private var format: Int = settings.packFormat
        private var description: String = settings.description
        private var supportedFormats: VersionRange = settings.supportedFormats
        private var minFormat: PackVersion = settings.minFormat
        private var maxFormat: PackVersion = settings.maxFormat
        private val entries: MutableList<OverlayEntry> = mutableListOf()

        fun format(format: Int): Builder = apply { this.format = format }
        fun description(description: String): Builder = apply { this.description = description }
        fun supportedFormats(range: VersionRange): Builder = apply { supportedFormats = range }
        fun minFormat(version: PackVersion): Builder = apply { minFormat = version }
        fun maxFormat(version: PackVersion): Builder = apply { maxFormat = version }
        fun overlayEntry(overlayEntry: OverlayEntry): Builder = apply { entries += overlayEntry }

        fun build(): PackMeta = PackMeta(
            pack = Pack(
                packFormat = format,
                description = description,
                supportedFormats = supportedFormats,
                minFormat = minFormat,
                maxFormat = maxFormat,
            ),
            overlays = entries.takeIf { it.isNotEmpty() }?.let(::Overlay),
        )
    }

    companion object {
        val PATH = PackPath("pack.mcmeta")

        private val GSON: Gson = GsonBuilder()
            .registerTypeAdapter(PackVersion::class.java, JsonSerializer<PackVersion> { src, _, _ -> src.toJson() })
            .registerTypeAdapter(VersionRange::class.java, JsonSerializer<VersionRange> { src, _, _ -> src.toJson() })
            .create()
    }
}
