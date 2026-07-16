package com.agenticedge.shopdemo.agent

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class AgentInference(
    val persona: String,
    val personaConfidence: Int,
    val engagement: Int,
    val purchaseIntent: Int,
    val predictedNextScreen: String,
    val nextScreenConfidence: Int
)

/**
 * Wraps the on-device TFLite Frontend Agent model. All inference happens
 * locally on the device — no network call, no server round-trip. This is the
 * "edge AI" brain behind persona generation, engagement/intent scoring, and
 * predictive navigation.
 */
class FrontendAgentModel(context: Context) {

    private val labels = AgentModelLabels.loadFromAssets(context)
    private val interpreter = Interpreter(loadModelFile(context, MODEL_FILE))

    /**
     * The labels JSON's output indices are raw flatbuffer tensor indices (as
     * reported by the Python TFLite converter), which do NOT match the small
     * 0..outputTensorCount()-1 slots the Java Interpreter API expects for
     * [Interpreter.runForMultipleInputsOutputs]. Resolve the real slot for
     * each head by matching tensor names instead.
     */
    private val headNameToOutputSlot: Map<String, Int> = run {
        val tensorNameToSlot = (0 until interpreter.outputTensorCount).associateBy {
            interpreter.getOutputTensor(it).name()
        }
        labels.outputIndex.mapNotNull { (headName, rawIndex) ->
            val tensorName = labels.rawIndexToTensorName[rawIndex]
            val slot = tensorNameToSlot[tensorName]
            if (slot != null) headName to slot else null
        }.toMap()
    }

    fun infer(features: FloatArray): AgentInference {
        require(features.size == FeatureExtractor.FEATURE_COUNT) {
            "Expected ${FeatureExtractor.FEATURE_COUNT} features, got ${features.size}"
        }

        val personaOut = Array(1) { FloatArray(labels.personaClasses.size) }
        val engagementOut = Array(1) { FloatArray(1) }
        val intentOut = Array(1) { FloatArray(1) }
        val nextScreenOut = Array(1) { FloatArray(labels.nextScreenClasses.size) }

        val outputs = mutableMapOf<Int, Any>()
        headNameToOutputSlot["persona"]?.let { outputs[it] = personaOut }
        headNameToOutputSlot["engagement"]?.let { outputs[it] = engagementOut }
        headNameToOutputSlot["purchaseIntent"]?.let { outputs[it] = intentOut }
        headNameToOutputSlot["nextScreen"]?.let { outputs[it] = nextScreenOut }

        val inputs = arrayOf<Any>(arrayOf(features))
        interpreter.runForMultipleInputsOutputs(inputs, outputs)

        val (personaIdx, personaConf) = argmax(personaOut[0])
        val (nextScreenIdx, nextScreenConf) = argmax(nextScreenOut[0])

        return AgentInference(
            persona = labels.personaClasses.getOrElse(personaIdx) { "Unknown" },
            personaConfidence = (personaConf * 100).toInt().coerceIn(0, 100),
            engagement = (engagementOut[0][0] * 100).toInt().coerceIn(0, 100),
            purchaseIntent = (intentOut[0][0] * 100).toInt().coerceIn(0, 100),
            predictedNextScreen = labels.nextScreenClasses.getOrElse(nextScreenIdx) { "Home" },
            nextScreenConfidence = (nextScreenConf * 100).toInt().coerceIn(0, 100)
        )
    }

    private fun argmax(values: FloatArray): Pair<Int, Float> {
        var bestIdx = 0
        var bestVal = values.getOrElse(0) { 0f }
        for (i in values.indices) {
            if (values[i] > bestVal) {
                bestVal = values[i]
                bestIdx = i
            }
        }
        return bestIdx to bestVal
    }

    private fun loadModelFile(context: Context, fileName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(fileName)
        FileInputStream(assetFileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    companion object {
        private const val MODEL_FILE = "agent_model.tflite"
    }
}
