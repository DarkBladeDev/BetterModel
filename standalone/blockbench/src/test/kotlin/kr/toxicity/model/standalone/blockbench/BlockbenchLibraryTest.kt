package kr.toxicity.model.standalone.blockbench

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlockbenchLibraryTest {

    @Test
    fun `reads and processes blockbench model`() {
        val model = BlockbenchLibrary.process("Demo Knight", sampleModelJson)

        assertEquals("demo_knight", model.name)
        assertEquals(FormatVersion.BLOCKBENCH_5, model.formatVersion)
        assertEquals(1, model.textures.size)
        assertEquals(1, model.bones.size)
        assertEquals("Root", model.bones.first().name)
        assertEquals(1, model.animations.size)
        assertTrue(model.animations.first().channels.isNotEmpty())
    }

    @Test
    fun `uses legacy conversion for format v4`() {
        val raw = BlockbenchLibrary.read(sampleModelJson.replace("\"5.0\"", "\"4.0\""))
        assertEquals(FormatVersion.BLOCKBENCH_LEGACY, raw.meta.formatVersion)
    }

    private val sampleModelJson = """
        {
          "meta": { "format_version": "5.0" },
          "resolution": { "width": 16, "height": 16 },
          "elements": [
            {
              "type": "cube",
              "name": "Body",
              "uuid": "cube-1",
              "from": [0, 0, 0],
              "to": [2, 2, 2],
              "origin": [0, 0, 0],
              "rotation": [0, 0, 0],
              "inflate": 0,
              "visibility": true
            }
          ],
          "outliner": [
            {
              "name": "Root",
              "uuid": "group-1",
              "origin": [0, 0, 0],
              "rotation": [0, 0, 0],
              "children": ["cube-1"]
            }
          ],
          "textures": [
            {
              "name": "base.png",
              "source": "data:image/png;base64,aGVsbG8=",
              "width": 16,
              "height": 16,
              "uv_width": 16,
              "uv_height": 16,
              "frame_time": 1,
              "frame_interpolate": false
            }
          ],
          "animations": [
            {
              "name": "walk",
              "loop": "loop",
              "override": false,
              "uuid": "anim-1",
              "length": 1.2,
              "animators": {
                "group-1": {
                  "name": "Root",
                  "rotation_global": false,
                  "keyframes": [
                    {
                      "channel": "rotation",
                      "time": 0,
                      "interpolation": "linear",
                      "data_points": [ { "x": 10, "y": 0, "z": 0 } ]
                    }
                  ]
                }
              }
            }
          ],
          "groups": [
            {
              "name": "Root",
              "uuid": "group-1",
              "origin": [0, 0, 0],
              "rotation": [0, 0, 0],
              "visibility": true
            }
          ]
        }
    """.trimIndent()
}
