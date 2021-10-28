package com.datatoggle.server.destination

enum class DestinationDef(
    val uri: String,
    val displayName: String,
    val parameters: List<DestinationParamDef>
) {

    Segment("segment", "Segment", listOf(
        DestinationParamDef("write_key", "Write key", DestinationParamType.String, "", true)
    )){
    },

    Mixpanel("mixpanel", "Mixpanel", listOf(
        DestinationParamDef("token", "Project token", DestinationParamType.String, "", true),
        DestinationParamDef("config", "Config", DestinationParamType.Dict, mapOf<String, Any>(), false),
    ));

    companion object {
        val byUri = values().asList().associateBy { it.uri }
    }

}

class DestinationParamDef(
    val uri: String,
    val name: String,
    val type: DestinationParamType,
    val defaultValue: Any,
    val mandatory: Boolean // mandatory == not empty (string not empty, dict not empty)
)

enum class DestinationParamType {
    Int,
    Float,
    String,
    Boolean,
    Dict
}
