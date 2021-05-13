package com.datatoggle.server.api.user.v0

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class GetConfigReply(
    val config: RestGlobalConfig?,
    val changed: Boolean
)


@CrossOrigin("*")
@RestController
class UserRestApi(
    private val userConfigCache: UserConfigCache
) {

    @GetMapping("/api/user/v0/config/{apiKey}")
    suspend fun getConfig(@PathVariable apiKey: String, @RequestParam lastModification: String?): GetConfigReply {
        val projectConfig = userConfigCache.getProjectConfig(apiKey)
        if (projectConfig == null){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Datatoggle config not found for api key '${apiKey}'")
        } else if (lastModification == null || lastModification < projectConfig.lastModification){
            return GetConfigReply(projectConfig, true)
        } else {
            return GetConfigReply(null, false)
        }
    }

}
