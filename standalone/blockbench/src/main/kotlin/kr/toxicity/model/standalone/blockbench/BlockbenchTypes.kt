package kr.toxicity.model.standalone.blockbench

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName

/**
 * Raw Blockbench model payload.
 */
data class RawBlockbenchModel(
    val meta: RawMeta,
    val resolution: RawResolution,
    val elements: List<RawElement>,
    val outliner: List<RawOutliner>,
    val textures: List<RawTexture>,
    val animations: List<RawAnimation> = emptyList(),
    val groups: List<RawGroup> = emptyList()
)

data class RawMeta(
    val formatVersion: FormatVersion
)

data class RawResolution(
    val width: Int,
    val height: Int
)

sealed interface RawElement {
    val uuid: String
    val type: String

    data class Cube(
        val name: String,
        override val uuid: String,
        val from: Vec3,
        val to: Vec3,
        val inflate: Float,
        val rotation: Vec3,
        val origin: Vec3,
        val visibility: Boolean
    ) : RawElement {
        override val type: String = "cube"
    }

    data class Locator(
        val name: String,
        override val uuid: String,
        val position: Vec3
    ) : RawElement {
        override val type: String = "locator"
    }

    data class NullObject(
        val name: String,
        override val uuid: String,
        val position: Vec3
    ) : RawElement {
        override val type: String = "null_object"
    }

    data class Camera(
        override val uuid: String
    ) : RawElement {
        override val type: String = "camera"
    }

    data class Unsupported(
        override val type: String,
        override val uuid: String = ""
    ) : RawElement
}

sealed interface RawOutliner {
    val uuid: String

    data class Reference(override val uuid: String) : RawOutliner

    data class Tree(
        val group: RawGroup,
        val children: List<RawOutliner>
    ) : RawOutliner {
        override val uuid: String get() = group.uuid
    }
}

data class RawGroup(
    val name: String,
    val uuid: String,
    val origin: Vec3,
    val rotation: Vec3,
    val visibility: Boolean
)

data class RawTexture(
    val name: String,
    val source: String,
    val width: Int,
    val height: Int,
    @SerializedName("uv_width")
    val uvWidth: Int,
    @SerializedName("uv_height")
    val uvHeight: Int,
    @SerializedName("frame_time")
    val frameTime: Int,
    @SerializedName("frame_interpolate")
    val frameInterpolate: Boolean
)

data class RawAnimation(
    val name: String,
    val length: Float,
    val loop: String = "play_once",
    @SerializedName("override")
    val override: Boolean = false,
    val animators: Map<String, RawAnimator> = emptyMap()
)

data class RawAnimator(
    val name: String?,
    val keyframes: List<RawKeyframe> = emptyList(),
    @SerializedName("rotation_global")
    val rotationGlobal: Boolean = false
)

data class RawKeyframe(
    val channel: String,
    @SerializedName("data_points")
    val dataPoints: List<RawDatapoint>,
    val time: Float,
    val interpolation: String? = null,
    @SerializedName("bezier_left_time")
    val bezierLeftTime: Vec3? = null,
    @SerializedName("bezier_left_value")
    val bezierLeftValue: Vec3? = null,
    @SerializedName("bezier_right_time")
    val bezierRightTime: Vec3? = null,
    @SerializedName("bezier_right_value")
    val bezierRightValue: Vec3? = null
)

data class RawDatapoint(
    val x: JsonPrimitive? = null,
    val y: JsonPrimitive? = null,
    val z: JsonPrimitive? = null,
    val script: String? = null
)

data class ProcessedModel(
    val name: String,
    val formatVersion: FormatVersion,
    val textures: List<ProcessedTexture>,
    val bones: List<ProcessedBone>,
    val animations: List<ProcessedAnimation>
)

data class ProcessedTexture(
    val packName: String,
    val bytes: ByteArray,
    val width: Int,
    val height: Int
)

data class ProcessedBone(
    val uuid: String,
    val name: String,
    val origin: Vec3,
    val rotation: Vec3,
    val visible: Boolean,
    val cubes: List<RawElement.Cube>
)

data class ProcessedAnimation(
    val name: String,
    val length: Float,
    val loop: String,
    val channels: List<AnimationChannel>
)

data class AnimationChannel(
    val boneName: String,
    val channel: String,
    val keyframes: List<ProcessedKeyframe>
)

data class ProcessedKeyframe(
    val time: Float,
    val value: VectorProvider,
    val interpolation: String
)

sealed interface VectorProvider {
    data class Constant(val value: Vec3) : VectorProvider
    data class Expression(val x: String, val y: String, val z: String) : VectorProvider
}

data class Vec3(
    val x: Float,
    val y: Float,
    val z: Float
) {
    fun invertXZ() = copy(x = -x, z = -z)

    companion object {
        val ZERO = Vec3(0f, 0f, 0f)

        fun from(element: JsonElement?): Vec3 {
            val arr = element?.asJsonArray ?: return ZERO
            return Vec3(
                arr.getOrNull(0)?.asFloat ?: 0f,
                arr.getOrNull(1)?.asFloat ?: 0f,
                arr.getOrNull(2)?.asFloat ?: 0f
            )
        }
    }
}

enum class FormatVersion {
    BLOCKBENCH_5,
    BLOCKBENCH_LEGACY;

    fun convertAnimationRotation(value: Vec3): Vec3 = when (this) {
        BLOCKBENCH_5 -> value.copy(x = -value.x, z = -value.z)
        BLOCKBENCH_LEGACY -> value.copy(y = -value.y, z = -value.z)
    }

    fun convertAnimationPosition(value: Vec3): Vec3 = when (this) {
        BLOCKBENCH_5 -> value.copy(x = -value.x, z = -value.z).toBlockScale()
        BLOCKBENCH_LEGACY -> value.copy(z = -value.z).toBlockScale()
    }

    fun convertAnimationScale(value: Vec3): Vec3 = Vec3(value.x - 1f, value.y - 1f, value.z - 1f)

    companion object {
        fun fromMajor(major: Int): FormatVersion = if (major >= 5) BLOCKBENCH_5 else BLOCKBENCH_LEGACY
    }
}

private fun Vec3.toBlockScale(): Vec3 = Vec3(x / 16f, y / 16f, z / 16f)
