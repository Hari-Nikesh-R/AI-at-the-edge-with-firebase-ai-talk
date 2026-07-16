package com.agenticedge.shopdemo.agent

import android.content.Context
import org.json.JSONObject

data class AgentModelLabels(
    val inputFeatures: List<String>,
    val personaClasses: List<String>,
    val nextScreenClasses: List<String>,
    /** head name ("persona", "engagement", ...) -> raw flatbuffer tensor index (NOT usable directly as a TFLite Java output slot). */
    val outputIndex: Map<String, Int>,
    /** raw flatbuffer tensor index -> tensor name, used to resolve the actual output slot at runtime. */
    val rawIndexToTensorName: Map<Int, String>
) {
    companion object {
        fun loadFromAssets(context: Context, fileName: String = "agent_model_labels.json"): AgentModelLabels {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            fun stringList(key: String): List<String> {
                val arr = root.getJSONArray(key)
                return (0 until arr.length()).map { arr.getString(it) }
            }

            val outputIndexJson = root.getJSONObject("outputIndexMap")
            val outputIndex = outputIndexJson.keys().asSequence()
                .associateWith { outputIndexJson.getInt(it) }

            val outputDetails = root.getJSONArray("outputDetails")
            val rawIndexToTensorName = (0 until outputDetails.length()).associate { i ->
                val entry = outputDetails.getJSONObject(i)
                entry.getInt("index") to entry.getString("name")
            }

            return AgentModelLabels(
                inputFeatures = stringList("featureNames"),
                personaClasses = stringList("personaClasses"),
                nextScreenClasses = stringList("nextScreenClasses"),
                outputIndex = outputIndex,
                rawIndexToTensorName = rawIndexToTensorName
            )
        }
    }
}
