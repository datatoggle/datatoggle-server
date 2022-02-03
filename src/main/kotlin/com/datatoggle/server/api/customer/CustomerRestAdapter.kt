package com.datatoggle.server.api.customer

import com.datatoggle.server.db.DbWorkspace
import com.datatoggle.server.db.DbWorkspaceDestination
import com.datatoggle.server.db.DbWorkspaceSource
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.destination.DestinationParamType
import com.datatoggle.server.tools.DbUtils

class CustomerRestAdapter {

    companion object {

        fun toRestWorkspace(dbWorkspace: DbWorkspace, dbSource: DbWorkspaceSource, dbDestinations: List<DbWorkspaceDestination>): RestWorkspace {
            return RestWorkspace(
                uri = dbWorkspace.uri,
                name = dbWorkspace.name,
                apiKey = dbSource.apiKey.toString(),
                destinations = dbDestinations.map { toRestDestinationConfigWithInfo(it) }
            )
        }

        fun toRestWorkspaceSnippet(dbWorkspace: DbWorkspace): RestWorkspaceSnippet {
            return RestWorkspaceSnippet(
                uri = dbWorkspace.uri,
                name = dbWorkspace.name,
            )
        }

        fun toRestDestinationConfigWithInfo(dbDestination: DbWorkspaceDestination): RestDestinationConfigWithInfo {
            val config = DbUtils.jsonToMap(dbDestination.destinationSpecificConfig)

            return RestDestinationConfigWithInfo(
                config = RestDestinationConfig(
                    destinationUri = dbDestination.destinationUri,
                    isEnabled = dbDestination.enabled,
                    destinationSpecificConfig = config
                ),
                paramErrors = mapOf()
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
                DestinationParamType.Dict -> RestParamType.Dict
                DestinationParamType.Int -> RestParamType.Int
                DestinationParamType.Float -> RestParamType.Float
            }
        }


    }

}
