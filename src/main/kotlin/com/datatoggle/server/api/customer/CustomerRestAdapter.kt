package com.datatoggle.server.api.customer

import com.datatoggle.server.db.DbProject
import com.datatoggle.server.db.DbProjectDestination
import com.datatoggle.server.db.DbProjectSource
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.destination.DestinationParamType
import com.datatoggle.server.tools.DbUtils

class CustomerRestAdapter {

    companion object {

        fun toRestProject(dbProject: DbProject, dbSource: DbProjectSource, dbDestinations: List<DbProjectDestination>): RestProject {
            return RestProject(
                uri = dbProject.uri,
                name = dbProject.name,
                apiKey = dbSource.apiKey.toString(),
                destinations = dbDestinations.map { toRestDestinationConfigWithInfo(it) }
            )
        }

        fun toRestProjectSnippet(dbProject: DbProject): RestProjectSnippet {
            return RestProjectSnippet(
                uri = dbProject.uri,
                name = dbProject.name,
            )
        }

        fun toRestDestinationConfigWithInfo(dbDestination: DbProjectDestination): RestDestinationConfigWithInfo {
            val config = DbUtils.jsonToMap(dbDestination.destinationSpecificConfig)

            return RestDestinationConfigWithInfo(
                config = RestDestinationConfig(
                    destinationUri = dbDestination.destinationUri,
                    isEnabled = dbDestination.enabled,
                    destinationSpecificConfig = config // dbDestination.config
                ),
                paramErrors = DestinationDef.byUri[dbDestination.destinationUri]!!.getParamErrors(config)
            )
        }

        fun toRestDestinationDef(destinationDef: DestinationDef): RestDestinationDef {
            return RestDestinationDef(
                uri = destinationDef.uri,
                name = destinationDef.displayName,
                paramDefs = destinationDef.parameters.map {
                    RestDestinationParamDef(
                        uri = it.uri,
                        name = it.name,
                        type = toRestParamType(it.type),
                        defaultValue = it.defaultValue
                    )
                }
            )
        }

        private fun toRestParamType(paramType: DestinationParamType): RestParamType {
            return when(paramType){
                DestinationParamType.String -> RestParamType.String
                DestinationParamType.Boolean -> RestParamType.Boolean
            }
        }


    }

}
