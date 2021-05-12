package com.datatoggle.server.cache

import com.datatoggle.server.db.DbProject
import com.datatoggle.server.db.ProjectDestinationRepo
import com.datatoggle.server.db.ProjectRepo
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.tools.DbUtils
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

data class DestinationConfig(
    val scriptUrl: String,
    val destinationSpecificConfig: Map<String,Any?>
)

@Service
class UserConfigCache(
    private val projectRepo: ProjectRepo,
    private val projectDestinationRepo: ProjectDestinationRepo,
) {

        private val cache: MutableMap<String,List<DestinationConfig>> = mutableMapOf()

        suspend fun initCache(){
            val allProjects = projectRepo.findAll().toList()
            allProjects.forEach { reloadProjectConfig(it) }
        }

        suspend fun reloadProjectConfig(dbProject: DbProject){
            val dbDests = projectDestinationRepo.findByProjectId(dbProject.id)
            val dests = dbDests.map {
                DestinationConfig(
                    scriptUrl = DestinationDef.byUri[it.destinationUri]!!.scriptUrl,
                    destinationSpecificConfig = DbUtils.jsonToMap(it.destinationSpecificConfig)
                )
            }
            cache[dbProject.apiKey.toString()] = dests
        }

        fun getProjectConfig(apiKey: String): List<DestinationConfig>? {
            return cache[apiKey]
        }

}
