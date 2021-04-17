package com.datatoggle.server.destination

enum class DestinationDef(
    val uri: String,
    val displayName: String,
    val parameters: List<DestinationParamDef>
) {
    Mixpanel("mixpanel", "Mixpanel", listOf(
        DestinationParamDef("projectToken", "Project token", DestinationParamType.String, ""),
        DestinationParamDef("euResidency", "EU residency", DestinationParamType.Boolean, false)
    )),

    Amplitude("amplitude", "Amplitude", listOf(
        DestinationParamDef("apiKey", "API key", DestinationParamType.String, "")
    ))
}

class DestinationParamDef(
    val uri: String,
    val displayName: String,
    val type: DestinationParamType,
    val defaultValue: Any
)

enum class DestinationParamType {
    String,
    Boolean
}
