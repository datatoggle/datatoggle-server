package com.datatoggle.server.destination

object CommonErrors {
    fun mandatory(paramName: String): String = "$paramName is mandatory"
}


enum class DestinationDef(
    val uri: String,
    val displayName: String,
    val parameters: List<DestinationParamDef>
) {

    Segment("segment", "Segment", listOf(
        DestinationParamDef("write_key", "Write key", DestinationParamType.String, "")
    )){
        override fun getParamErrors(config: Map<String, Any?>): Map<String, String> {
            val result = mutableMapOf<String, String>()
            val projectToken = config["write_key"] as String?
            if (projectToken.isNullOrBlank()){
                result["write_key"] = CommonErrors.mandatory("write_key")
            }
            return result
        }

    };

    /*
    Mixpanel("mixpanel", "Mixpanel", listOf(
        DestinationParamDef("project_token", "Project token", DestinationParamType.String, ""),
        DestinationParamDef("eu_residency", "EU residency", DestinationParamType.Boolean, false)
    )) {
        override fun getParamErrors(config: Map<String, Any?>): Map<String, String> {
            val result = mutableMapOf<String, String>()
            val projectToken = config["project_token"] as String?
            if (projectToken.isNullOrBlank()){
                result["project_token"] = CommonErrors.mandatory("project_token")
            }
            return result
        }
    };
    */


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
