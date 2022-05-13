package com.datatoggle.server.api.customer

import com.datatoggle.server.db.DbWorkspace
import com.datatoggle.server.db.DbWorkspaceDestination
import com.datatoggle.server.db.DbWorkspaceSource
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.destination.DestinationParamDefBool
import com.datatoggle.server.destination.DestinationParamDefDict
import com.datatoggle.server.destination.DestinationParamDefString
import com.datatoggle.server.destination.IDestinationParamDef
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

        fun toRestDestinationConfigWithInfo(
            dbDestination: DbWorkspaceDestination
        ): RestDestinationConfigWithInfo {
            val specificConfig = DbUtils.jsonToMap(dbDestination.destinationSpecificConfig)
            val paramErrors = DestinationCheck.checkConfigParams(dbDestination.destinationUri, specificConfig)

            return RestDestinationConfigWithInfo(
                config = RestDestinationConfig(
                    destinationUri = dbDestination.destinationUri,
                    isEnabled = dbDestination.enabled,
                    destinationSpecificConfig = specificConfig
                ),
                paramErrors = paramErrors
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
                        type = toRestParamType(it),
                        defaultValue = it.defaultValue,
                        isMandatory = it.isMandatory
                    )
                }
            )
        }

        private fun toRestParamType(def: IDestinationParamDef): RestParamType {
            return when(def){
                is DestinationParamDefDict -> RestParamType.Dict
                is DestinationParamDefString -> RestParamType.String
                is DestinationParamDefBool -> RestParamType.Boolean
            }

        }


    }

}
