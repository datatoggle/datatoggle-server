package com.datatoggle.server.api.analytics

import com.datatoggle.server.api.customer.PostCreateProjectArgs
import com.datatoggle.server.db.DbTrackedSession
import com.datatoggle.server.db.TrackedSessionRepo
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.*


data class PostTrackedSessionArgs(
    val apiKey: String,
    val sampling: Int
)

@RestController
class SessionAnalyticsRestApi(
    @Value("\${datatoggle.server_analytics_token}") private val serverAnalyticsToken: String,
    private val trackedSessionRepo: TrackedSessionRepo
) {

    @PostMapping("/api/analytics/tracked-sessions")
    suspend fun postTrackedSession(
        @RequestHeader(name="Authorization") token: String,
        @RequestBody args: PostTrackedSessionArgs
    ) {
        if (token == serverAnalyticsToken) {
            trackedSessionRepo.save(DbTrackedSession(apiKey = UUID.fromString(args.apiKey), sampling = args.sampling))
        }
    }
}
