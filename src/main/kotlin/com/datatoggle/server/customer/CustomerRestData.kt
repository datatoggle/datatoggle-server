package com.datatoggle.server.customer

data class RestProject(
    val uri: String,
    val apiKey: String,
    val name: String,
    val destinations: List<RestDestination>)

data class RestDestination(
    val uri: String,
    val isEnabled: Boolean,
    val name: String,
    val config: List<RestDestinationParam>
)

enum class RestParamType{
    Boolean,
    String,
}

data class RestDestinationParam(
    val uri: String,
    val name: String,
    val type: RestParamType,
    val value: Any
)
