package com.datatoggle.server.customer

import com.datatoggle.server.db.DbCustomerDestination
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.destination.DestinationParamType

class CustomerRestAdapter {

    companion object {

        fun getDestinations(dbDestinations: List<DbCustomerDestination>): List<RestDestination> {

            val destMap = dbDestinations.map { it.uri to it }.toMap()
            return DestinationDef.values().asList().map { d ->
                toRestDestination(d, destMap[d.uri])
            }
        }

        private fun toRestDestination(def: DestinationDef, dbDest: DbCustomerDestination?) = RestDestination(
            isEnabled = false,
            uri = def.uri,
            displayName = def.displayName,
            config = def.parameters.map { p ->
                RestDestinationParam(
                    uri = p.uri,
                    displayName = p.displayName,
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