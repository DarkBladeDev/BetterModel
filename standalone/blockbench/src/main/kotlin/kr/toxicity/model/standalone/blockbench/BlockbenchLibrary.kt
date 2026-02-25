package kr.toxicity.model.standalone.blockbench

import java.io.Reader

/**
 * Convenience entrypoint for loading and processing Blockbench models.
 */
object BlockbenchLibrary {

    fun read(reader: Reader): RawBlockbenchModel = BlockbenchParser.parse(reader)

    fun read(json: String): RawBlockbenchModel = BlockbenchParser.parse(json)

    fun process(modelName: String, reader: Reader): ProcessedModel =
        BlockbenchProcessor.process(modelName, read(reader))

    fun process(modelName: String, json: String): ProcessedModel =
        BlockbenchProcessor.process(modelName, read(json))
}
