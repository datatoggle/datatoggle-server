package com.datatoggle.server.trackeduser

import com.datatoggle.server.cache.DestinationConfig
import com.datatoggle.server.cache.UserConfigCache
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class GetConfigReply(
    val destinations:List<DestinationConfig>
)


@CrossOrigin("*")
@RestController
class TrackedUserRestApi(
    private val userConfigCache: UserConfigCache
) {

    @GetMapping("/api/user/config/{apiKey}")
    suspend fun getConfig(@PathVariable apiKey: String): GetConfigReply {
        val projectConfig = userConfigCache.getProjectConfig(apiKey)
        if (projectConfig == null){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Datatoggle config not found for api key '${apiKey}'")
        }
        return GetConfigReply(projectConfig)
    }

}
