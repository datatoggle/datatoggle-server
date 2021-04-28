package com.datatoggle.server.destination

object CommonErrors {
    fun mandatory(paramName: String): String = "$paramName is mandatory"
}


enum class DestinationDef(
    val uri: String,
    val displayName: String,
    val parameters: List<DestinationParamDef>
) {

    Mixpanel("mixpanel", "Mixpanel", listOf(
        DestinationParamDef("projectToken", "Project token", DestinationParamType.String, ""),
        DestinationParamDef("euResidency", "EU residency", DestinationParamType.Boolean, false)
    )) {
        override fun getParamErrors(config: Map<String, Any?>): Map<String, String> {
            val result = mutableMapOf<String, String>()
            val projectToken = config["projectToken"] as String?
            if (projectToken.isNullOrBlank()){
                result["projectToken"] = CommonErrors.mandatory("projectToken")
            }
            return result
        }
    },

    Amplitude("amplitude", "Amplitude", listOf(
        DestinationParamDef("apiKey", "API key", DestinationParamType.String, "")
    )) {
        override fun getParamErrors(config: Map<String, Any?>): Map<String, String> {
            val result = mutableMapOf<String, String>()
            val apiKey = config["apiKey"] as String?
            if (apiKey.isNullOrBlank()){
                result["apiKey"] = CommonErrors.mandatory("apiKey")
            }
            return result
        }
    };

    companion object {
        val byUri = values().asList().map { it.uri to it }.toMap()
    }

    abstract fun getParamErrors(config: Map<String, Any?>): Map<String, String>
}

class DestinationParamDef(
    val uri: String,
    val name: String,
    val type: DestinationParamType,
    val defaultValue: Any
)

enum class DestinationParamType {
    String,
    Boolean
}
