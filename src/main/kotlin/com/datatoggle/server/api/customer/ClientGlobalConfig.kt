package com.datatoggle.server.api.customer

data class ClientDestinationConfig(
    val scriptUrl: String,
    val destinationSpecificConfig: Map<String,Any?>
)

data class ClientGlobalConfig(
    val lastModification: String,
    val destinations: List<ClientDestinationConfig>
)

