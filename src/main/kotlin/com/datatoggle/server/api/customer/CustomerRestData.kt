package com.datatoggle.server.api.customer

data class RestWorkspaceSnippet(
    val uri: String,
    val name: String,
)

data class RestWorkspace(
    val uri: String,
    val apiKey: String,
    val name: String,
    val destinations: List<RestDestinationConfigWithInfo>)


data class RestDestinationDef(
    val uri: String,
    val name: String,
    val paramDefs: List<RestDestinationParamDef>
)

data class RestDestinationConfig(
    val destinationUri: String,
    val isEnabled: Boolean,
    val destinationSpecificConfig: Map<String,Any?>
)

data class RestDestinationConfigWithInfo(
    val config: RestDestinationConfig,
    val paramErrors: Map<String,String>,
)

enum class RestParamType{
    Boolean,
    String,
    Dict,
    Int,
    Float
}

data class RestDestinationParamDef(
    val uri: String,
    val name: String,
    val type: RestParamType,
    val defaultValue: Any
)
