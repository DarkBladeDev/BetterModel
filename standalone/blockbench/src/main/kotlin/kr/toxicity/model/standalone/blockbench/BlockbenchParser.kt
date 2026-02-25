package kr.toxicity.model.standalone.blockbench

import com.google.gson.*
import com.vdurmont.semver4j.Semver
import java.io.Reader

/**
 * Standalone parser for Generic Blockbench model files.
 */
object BlockbenchParser {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(RawMeta::class.java, JsonDeserializer { json, _, _ ->
            val major = Semver(
                json.asJsonObject.getAsJsonPrimitive("format_version").asString,
                Semver.SemverType.LOOSE
            ).major
            RawMeta(FormatVersion.fromMajor(major))
        })
        .registerTypeAdapter(RawElement::class.java, JsonDeserializer { json, _, context ->
            val obj = json.asJsonObject
            when (obj.getAsJsonPrimitive("type")?.asString ?: "cube") {
                "cube" -> RawElement.Cube(
                    name = obj.getAsJsonPrimitive("name")?.asString.orEmpty(),
                    uuid = obj.getAsJsonPrimitive("uuid").asString,
                    from = Vec3.from(obj.get("from")),
                    to = Vec3.from(obj.get("to")),
                    inflate = obj.getAsJsonPrimitive("inflate")?.asFloat ?: 0f,
                    rotation = Vec3.from(obj.get("rotation")),
                    origin = Vec3.from(obj.get("origin")),
                    visibility = obj.getAsJsonPrimitive("visibility")?.asBoolean ?: true
                )

                "locator" -> RawElement.Locator(
                    name = obj.getAsJsonPrimitive("name")?.asString.orEmpty(),
                    uuid = obj.getAsJsonPrimitive("uuid").asString,
                    position = Vec3.from(obj.get("position"))
                )

                "null_object" -> RawElement.NullObject(
                    name = obj.getAsJsonPrimitive("name")?.asString.orEmpty(),
                    uuid = obj.getAsJsonPrimitive("uuid").asString,
                    position = Vec3.from(obj.get("position"))
                )

                "camera" -> RawElement.Camera(
                    uuid = obj.getAsJsonPrimitive("uuid").asString
                )

                else -> RawElement.Unsupported(type = obj.getAsJsonPrimitive("type")?.asString ?: "unknown")
            }
        })
        .registerTypeAdapter(RawOutliner::class.java, JsonDeserializer { json, _, context ->
            when {
                json.isJsonPrimitive -> RawOutliner.Reference(json.asString)
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    val group = context.deserialize<RawGroup>(json, RawGroup::class.java)
                    val children = obj.getAsJsonArray("children")?.map {
                        context.deserialize<RawOutliner>(it, RawOutliner::class.java)
                    }.orEmpty()
                    RawOutliner.Tree(group, children)
                }

                else -> error("Unsupported outliner payload")
            }
        })
        .registerTypeAdapter(RawKeyframe::class.java, JsonDeserializer { json, _, context ->
            val obj = json.asJsonObject
            val dataPoints = obj.getAsJsonArray("data_points")
                ?.map { element ->
                    val source = element.asJsonObject
                    RawDatapoint(
                        x = source.getAsJsonPrimitive("x"),
                        y = source.getAsJsonPrimitive("y"),
                        z = source.getAsJsonPrimitive("z"),
                        script = source.getAsJsonPrimitive("script")?.asString
                    )
                }
                .orEmpty()
            RawKeyframe(
                channel = obj.getAsJsonPrimitive("channel")?.asString ?: "not_found",
                dataPoints = dataPoints,
                time = obj.getAsJsonPrimitive("time")?.asFloat ?: 0f,
                interpolation = obj.getAsJsonPrimitive("interpolation")?.asString,
                bezierLeftTime = context.deserialize(obj.get("bezier_left_time"), Vec3::class.java),
                bezierLeftValue = context.deserialize(obj.get("bezier_left_value"), Vec3::class.java),
                bezierRightTime = context.deserialize(obj.get("bezier_right_time"), Vec3::class.java),
                bezierRightValue = context.deserialize(obj.get("bezier_right_value"), Vec3::class.java),
            )
        })
        .registerTypeAdapter(Vec3::class.java, JsonDeserializer { json, _, _ -> Vec3.from(json) })
        .create()

    fun parse(reader: Reader): RawBlockbenchModel = gson.fromJson(reader, RawBlockbenchModel::class.java)

    fun parse(json: String): RawBlockbenchModel = gson.fromJson(json, RawBlockbenchModel::class.java)
}
