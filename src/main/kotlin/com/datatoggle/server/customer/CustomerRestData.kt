package com.datatoggle.server.customer

data class RestCustomerConfig(
    val apiKey: String,
    val destinations: List<RestDestination>)

data class RestDestination(
    val isEnabled: Boolean,
    val uri: String,
    val displayName: String,
    val config: List<RestDestinationParam>
)

enum class RestParamType{
    Boolean,
    String,
}

data class RestDestinationParam(
    val uri: String,
    val displayName: String,
    val type: RestParamType,
    val value: Any
)
