package com.datatoggle.server.user.v0

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


data class PostConnectResult(
    val config: UserGlobalConfig
)

data class UserGlobalConfig(val destinationConfigs: List<UserDestinationConfig>)

data class UserDestinationConfig(
    val isEnabled: Boolean,
    val destinationSpecificConfig: Map<String, Any>
)

data class PostConnectArgs(
    val apiKey: String, // key of the consumer
    val userUuid: String?, // present if the user already exists
    val userIdForCustomer: String? // present if the client already called "identify"
)


@RestController
class UserApiController {

    @PostMapping("/api/user/v0/connect")
    suspend fun postConnect(userUri: String): PostConnectResult {
        return PostConnectResult(UserGlobalConfig(listOf()))
    }

    @PostMapping("/api/user/v0/first_connect")
    suspend fun postFirstConnect(args: PostConnectArgs): PostConnectResult {



        return PostConnectResult(UserGlobalConfig(listOf()))
    }

}
