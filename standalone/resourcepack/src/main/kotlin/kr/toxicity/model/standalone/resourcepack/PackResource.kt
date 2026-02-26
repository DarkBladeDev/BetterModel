package kr.toxicity.model.standalone.resourcepack

fun interface ByteSupplier {
    fun get(): ByteArray
}

data class PackResource(
    val overlay: PackOverlay?,
    val path: PackPath,
    val estimatedSize: Long,
    val supplier: ByteSupplier,
) {
    fun bytes(): ByteArray = supplier.get()

    companion object {
        fun of(path: PackPath, estimatedSize: Long, supplier: ByteSupplier): PackResource =
            PackResource(null, path, estimatedSize, supplier)

        fun of(overlay: PackOverlay?, path: PackPath, estimatedSize: Long, supplier: ByteSupplier): PackResource =
            PackResource(overlay, path, estimatedSize, supplier)
    }
}

class PackByte(val path: PackPath, val bytes: ByteArray) : Comparable<PackByte> {
    override fun compareTo(other: PackByte): Int = path.compareTo(other.path)

    override fun equals(other: Any?): Boolean = other is PackByte && path == other.path

    override fun hashCode(): Int = path.hashCode()
}
