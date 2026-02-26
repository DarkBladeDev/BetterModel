package kr.toxicity.model.standalone.resourcepack

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ResourcePackLibraryTest {

    @Test
    fun `build contains mcmeta and custom asset`() {
        val zipper = ResourcePackLibrary.zipper(
            ResourcePackSettings(namespace = "custom", includeLegacyOverlay = false, includeModernOverlay = false),
        )

        zipper.defaultOverlay.namespace().models().add("sample.json") {
            "{".toByteArray()
        }

        val build = zipper.build(icon = byteArrayOf(1, 2, 3))
        val paths = build.resources.map { it.path.path }.toSet()

        assertTrue(paths.contains("pack.mcmeta"))
        assertTrue(paths.contains("pack.png"))
        assertTrue(paths.contains("assets/custom/models/sample.json"))
    }

    @Test
    fun `obfuscator keeps stable mapping`() {
        val obfuscator = PackObfuscator.order(enabled = true)
        val first = obfuscator.obfuscate("alpha")
        val second = obfuscator.obfuscate("alpha")
        val third = obfuscator.obfuscate("beta")

        assertEquals(first, second)
        assertNotNull(third)
        assertTrue(third.isNotBlank())
    }
}
