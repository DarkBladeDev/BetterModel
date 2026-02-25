package kr.toxicity.model.standalone.blockbench

import com.google.gson.JsonPrimitive
import java.util.Base64

/**
 * Processes parsed Blockbench payload into an engine-agnostic model.
 */
object BlockbenchProcessor {

    fun process(modelName: String, model: RawBlockbenchModel): ProcessedModel {
        val cubesByUuid = model.elements
            .filterIsInstance<RawElement.Cube>()
            .associateBy { it.uuid }

        val groupsByUuid = model.groups.associateBy { it.uuid }

        val textures = model.textures.map {
            val textureName = it.name.substringBeforeLast('.')
            ProcessedTexture(
                packName = packName(if (textureName.startsWith("global_")) textureName else "${packName(modelName)}_$textureName"),
                bytes = decodeTexture(it.source),
                width = it.width,
                height = it.height
            )
        }

        val bones = model.outliner.mapNotNull { node ->
            when (node) {
                is RawOutliner.Reference -> null
                is RawOutliner.Tree -> {
                    val selectedGroup = groupsByUuid[node.uuid] ?: node.group
                    ProcessedBone(
                        uuid = selectedGroup.uuid,
                        name = selectedGroup.name,
                        origin = selectedGroup.origin.invertXZ(),
                        rotation = selectedGroup.rotation.invertXZ(),
                        visible = selectedGroup.visibility,
                        cubes = collectCubes(node, cubesByUuid)
                    )
                }
            }
        }

        val animations = model.animations.map { animation ->
            ProcessedAnimation(
                name = animation.name,
                length = animation.length,
                loop = animation.loop,
                channels = animation.animators.values
                    .filter { !it.name.isNullOrBlank() && it.keyframes.isNotEmpty() }
                    .flatMap { animator ->
                        animator.keyframes.mapNotNull { keyframe ->
                            val provider = buildProvider(keyframe.dataPoints.firstOrNull()) ?: return@mapNotNull null
                            val transformed = when (keyframe.channel) {
                                "position" -> transformProvider(provider, model.meta.formatVersion::convertAnimationPosition)
                                "rotation" -> transformProvider(provider, model.meta.formatVersion::convertAnimationRotation)
                                "scale" -> transformProvider(provider, model.meta.formatVersion::convertAnimationScale)
                                else -> provider
                            }
                            AnimationChannel(
                                boneName = animator.name!!,
                                channel = keyframe.channel,
                                keyframes = listOf(
                                    ProcessedKeyframe(
                                        time = keyframe.time,
                                        value = transformed,
                                        interpolation = keyframe.interpolation ?: "linear"
                                    )
                                )
                            )
                        }
                    }
            )
        }

        return ProcessedModel(
            name = packName(modelName),
            formatVersion = model.meta.formatVersion,
            textures = textures,
            bones = bones,
            animations = animations
        )
    }

    private fun collectCubes(tree: RawOutliner.Tree, cubesByUuid: Map<String, RawElement.Cube>): List<RawElement.Cube> {
        return tree.children.flatMap { child ->
            when (child) {
                is RawOutliner.Reference -> listOfNotNull(cubesByUuid[child.uuid])
                is RawOutliner.Tree -> collectCubes(child, cubesByUuid)
            }
        }
    }

    private fun decodeTexture(source: String): ByteArray {
        val payload = source.substringAfter(',')
        return Base64.getDecoder().decode(payload)
    }

    private fun packName(value: String): String = value.lowercase().replace(Regex("[^a-z0-9_.]"), "_")

    private fun buildProvider(datapoint: RawDatapoint?): VectorProvider? {
        datapoint ?: return null
        return if (datapoint.script != null) {
            VectorProvider.Expression(datapoint.script, datapoint.script, datapoint.script)
        } else {
            val x = datapoint.x.toExpressionOrNumber()
            val y = datapoint.y.toExpressionOrNumber()
            val z = datapoint.z.toExpressionOrNumber()
            if (x is NumberValue && y is NumberValue && z is NumberValue) {
                VectorProvider.Constant(Vec3(x.value, y.value, z.value))
            } else {
                VectorProvider.Expression(x.raw, y.raw, z.raw)
            }
        }
    }

    private fun transformProvider(provider: VectorProvider, transform: (Vec3) -> Vec3): VectorProvider {
        return when (provider) {
            is VectorProvider.Constant -> VectorProvider.Constant(transform(provider.value))
            is VectorProvider.Expression -> provider
        }
    }

    private sealed interface ParsedValue {
        val raw: String
    }

    private data class NumberValue(val value: Float, override val raw: String) : ParsedValue
    private data class ExpressionValue(override val raw: String) : ParsedValue

    private fun JsonPrimitive?.toExpressionOrNumber(): ParsedValue {
        if (this == null) return NumberValue(0f, "0")
        if (isNumber) return NumberValue(asFloat, asString)
        val text = asString.trim().ifEmpty { "0" }
        return text.toFloatOrNull()?.let { NumberValue(it, text) } ?: ExpressionValue(text)
    }
}
