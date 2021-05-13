package com.datatoggle.server.api.user.v0

import com.datatoggle.server.db.DbProject
import com.datatoggle.server.db.ProjectDestinationRepo
import com.datatoggle.server.db.ProjectRepo
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.tools.DbUtils
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.Instant

data class RestDestinationConfig(
    val scriptUrl: String,
    val destinationSpecificConfig: Map<String,Any?>
)

data class RestGlobalConfig(
    val lastModification: String,
    val destinations: List<RestDestinationConfig>
)

@Service
class UserConfigCache(
    private val projectRepo: ProjectRepo,
    private val projectDestinationRepo: ProjectDestinationRepo,
) {

        private val cache: MutableMap<String,RestGlobalConfig> = mutableMapOf()

        suspend fun initCache(){
            val allProjects = projectRepo.findAll().toList()
            allProjects.forEach { reloadProjectConfig(it) }
        }

        suspend fun reloadProjectConfig(dbProject: DbProject){
            val dbDests = projectDestinationRepo
                .findByProjectId(dbProject.id)
                .filter { it.enabled }

            val dests = dbDests.map {
                RestDestinationConfig(
                    scriptUrl = DestinationDef.byUri[it.destinationUri]!!.scriptUrl,
                    destinationSpecificConfig = DbUtils.jsonToMap(it.destinationSpecificConfig)
                )
            }
            val lastModification = dbDests
                .maxByOrNull { it.lastModificationDatetime }
                ?.lastModificationDatetime ?: Instant.now()

            cache[dbProject.apiKey.toString()] = RestGlobalConfig(
                lastModification = lastModification.toString(),
                destinations = dests
            )
        }

        fun getProjectConfig(apiKey: String): RestGlobalConfig? {
            return cache[apiKey]
        }

}
