package com.datatoggle.server.api.customer

import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.destination.DestinationParamType

class DestinationCheck {

    companion object {

        fun enrichSpecificConfigWithDefault(destinationDefUri: String, config: Map<String, Any>): Map<String, Any>{
            val def: DestinationDef = DestinationDef.byUri[destinationDefUri]!!
            return config.plus(
                def.parameters
                    .filter { !config.containsKey(it.uri) }
                    .map { it.uri to it.defaultValue }
            )
        }

        // TODO Add tests when we add other destination defs (with int, float, boolean, mandatory or not)
        fun checkConfigParams(config: RestDestinationConfig): Map<String,String> {
            val def: DestinationDef = DestinationDef.byUri[config.destinationUri]!!
            val result = mutableMapOf<String, String>()
                for (d in def.parameters){
                    if (d.isMandatory){
                        val value = config.destinationSpecificConfig[d.uri]
                        if (value == null || isConsideredEmpty(value)){
                            result[d.uri] = "Mandatory"
                        } else if (! typeIsOk(value, d.type)){
                            result[d.uri] = "Must be ${typeLabel(d.type)}"
                        }
                    }
                }
            return result
        }

        private fun isConsideredEmpty(value: Any): Boolean {
            if (value is String){
                return value.isEmpty()
            } else if (value is Map<*,*>){
                return value.isEmpty()
            } else {
                return false // a bool or number is never empty.
            }
        }

        private fun typeIsOk(value: Any, paramType: DestinationParamType): Boolean{
            return when(paramType){
                DestinationParamType.Int -> value is Int
                DestinationParamType.Float -> value is Float || value is Int || value is Double
                DestinationParamType.String -> value is String
                DestinationParamType.Boolean -> value is Boolean
                DestinationParamType.Dict -> value is Map<*, *>
            }
        }

        private fun typeLabel(paramType: DestinationParamType): String {
            return when(paramType){
                DestinationParamType.Int -> "an integer"
                DestinationParamType.Float -> "a number"
                DestinationParamType.String -> "a string"
                DestinationParamType.Boolean -> "true or false"
                DestinationParamType.Dict -> "a map"
            }
        }

    }
}
