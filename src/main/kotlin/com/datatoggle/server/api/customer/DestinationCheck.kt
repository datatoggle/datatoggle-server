package com.datatoggle.server.api.customer

import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.destination.DestinationParamDefDict
import com.datatoggle.server.destination.DestinationParamDefString
import com.datatoggle.server.destination.IDestinationParamDef

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
        fun checkConfigParams(destinationDefUri: String, config: Map<String, Any>): Map<String,String> {
            val def: DestinationDef = DestinationDef.byUri[destinationDefUri]!!
            val result = mutableMapOf<String, String>()
                for (d in def.parameters){
                    if (d.isMandatory){
                        val value = config[d.uri]
                        if (value == null || isConsideredEmpty(value)){
                            result[d.uri] = "Mandatory"
                        } else if (! typeIsOk(value, d)){
                            result[d.uri] = "Must be ${typeLabel(d)}"
                        }
                    }
                }
            return result
        }

        private fun isConsideredEmpty(value: Any): Boolean {
            return if (value is String){
                value.isEmpty()
            } else if (value is Map<*,*>){
                value.isEmpty()
            } else {
                false // a bool or number is never empty.
            }
        }

        private fun typeIsOk(value: Any, def: IDestinationParamDef): Boolean{
            return when(def){
                is DestinationParamDefDict -> value is Map<*, *>
                is DestinationParamDefString -> value is String
            }
        }

        private fun typeLabel(def: IDestinationParamDef): String {
            return when(def){
                is DestinationParamDefDict -> "a map"
                is DestinationParamDefString -> "a string"
            }
        }

    }
}
