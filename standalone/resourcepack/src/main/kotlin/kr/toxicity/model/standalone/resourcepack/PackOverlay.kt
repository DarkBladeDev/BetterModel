package kr.toxicity.model.standalone.resourcepack

data class PackOverlay(
    val packName: String,
    val range: PackMeta.VersionRange?,
    val enabled: () -> Boolean,
) : Comparable<PackOverlay> {
    fun path(namespace: String): PackPath = if (packName.isEmpty()) PackPath.EMPTY else PackPath("${namespace}_$packName")

    fun test(): Boolean = enabled()

    override fun compareTo(other: PackOverlay): Int = packName.compareTo(other.packName)

    companion object {
        fun defaults(settings: ResourcePackSettings): List<PackOverlay> = listOf(
            PackOverlay("", null) { true },
            PackOverlay("legacy", PackMeta.VersionRange(22, 45)) { settings.includeLegacyOverlay },
            PackOverlay("modern", PackMeta.VersionRange(46, 99)) { settings.includeModernOverlay },
        )
    }
}
