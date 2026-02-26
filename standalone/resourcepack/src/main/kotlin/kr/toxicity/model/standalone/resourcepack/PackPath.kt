package kr.toxicity.model.standalone.resourcepack

data class PackPath(val path: String) : Comparable<PackPath> {
    fun resolve(vararg subPaths: String): PackPath {
        if (subPaths.isEmpty()) return this
        val suffix = subPaths.joinToString(DELIMITER)
        return PackPath(if (path.isEmpty()) suffix else "$path$DELIMITER$suffix")
    }

    override fun compareTo(other: PackPath): Int = path.compareTo(other.path)

    override fun toString(): String = path

    companion object {
        const val DELIMITER: String = "/"
        val EMPTY: PackPath = PackPath("")
    }
}
