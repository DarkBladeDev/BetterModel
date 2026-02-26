package kr.toxicity.model.standalone.resourcepack

import java.util.concurrent.ConcurrentHashMap

fun interface PackObfuscator {
    fun obfuscate(rawName: String): String

    fun withModels(models: PackObfuscator): Pair = pair(models = models, textures = this)

    data class Pair(val models: PackObfuscator, val textures: PackObfuscator)

    companion object {
        val NONE: PackObfuscator = PackObfuscator { name -> name }

        fun order(enabled: Boolean): PackObfuscator = if (enabled) Order() else NONE

        fun pair(models: PackObfuscator, textures: PackObfuscator): Pair = Pair(models, textures)
    }
}

private class Order : PackObfuscator {
    private val nameMap = ConcurrentHashMap<String, String>()

    override fun obfuscate(rawName: String): String = nameMap.computeIfAbsent(rawName) {
        var index = nameMap.size
        val builder = StringBuilder()
        while (index >= AVAILABLE_NAME.size) {
            builder.append(AVAILABLE_NAME[index % AVAILABLE_NAME.size])
            index /= AVAILABLE_NAME.size
        }
        builder.append(AVAILABLE_NAME[index % AVAILABLE_NAME.size])
        builder.toString()
    }

    private companion object {
        private val AVAILABLE_NAME = charArrayOf(
            'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'm', 'n', 'l', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        )
    }
}
