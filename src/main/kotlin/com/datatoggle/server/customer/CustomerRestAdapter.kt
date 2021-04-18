package com.datatoggle.server.customer

import com.datatoggle.server.db.DbProject
import com.datatoggle.server.db.DbProjectDestination
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.destination.DestinationParamType

class CustomerRestAdapter {

    companion object {

        fun getProject(dbProject: DbProject, dbDestinations: List<DbProjectDestination>): RestProject {
            return RestProject(
                uri = dbProject.uri,
                name = dbProject.name,
                apiKey = dbProject.apiKey.toString(),
                destinations = getDestinations(dbDestinations)
            )
        }

        private fun getDestinations(dbDestinations: List<DbProjectDestination>): List<RestDestination> {

            val destMap = dbDestinations.map { it.uri to it }.toMap()
            return DestinationDef.values().asList().map { d ->
                toRestDestination(d, destMap[d.uri])
            }
        }

        private fun toRestDestination(def: DestinationDef, dbDest: DbProjectDestination?) = RestDestination(
            isEnabled = false,
            uri = def.uri,
            name = def.displayName,
            config = def.parameters.map { p ->
                RestDestinationParam(
                    uri = p.uri,
                    name = p.displayName,
                    type = toRestParamType(p.type),
                    value = dbDest?.config?.getOrDefault(p.uri, p.defaultValue) ?: p.defaultValue
                )
            }
        )

        private fun toRestParamType(paramType: DestinationParamType): RestParamType {
            return when(paramType){
                DestinationParamType.String -> RestParamType.String
                DestinationParamType.Boolean -> RestParamType.Boolean
            }
        }
    }

}
