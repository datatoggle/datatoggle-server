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
        DestinationParamDef("project_token", "Project token", DestinationParamType.String, "", true),
        DestinationParamDef("eu_residency", "EU residency", DestinationParamType.Boolean, false, true)
    )) {
    };


    companion object {
        val byUri = values().asList().map { it.uri to it }.toMap()
    }

}

class DestinationParamDef(
    val uri: String,
    val name: String,
    val type: DestinationParamType,
    val defaultValue: Any,
    val mandatory: Boolean
)

enum class DestinationParamType {
    String,
    Boolean
}
