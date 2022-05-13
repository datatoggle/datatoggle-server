package com.datatoggle.server.destination

enum class DestinationDef(
    val uri: String,
    val displayName: String,
    val parameters: List<IDestinationParamDef>
) {

    Segment("segment", "Segment", listOf(
        DestinationParamDefString("write_key", "Write key", true, defaultValue = "")
    )),
    Mixpanel("mixpanel", "Mixpanel", listOf(
        DestinationParamDefString("token", "Project token", true, ""),
        DestinationParamDefDict("config", "Config", false, mapOf()),
    ));

    companion object {
        val byUri = values().asList().associateBy { it.uri }
    }
}

//TODO NICO: 1) upgrade version 2) make it sealed interface 3) virer notion de paramType devenu inutile
sealed interface IDestinationParamDef {
    val uri: String
    val name: String
    val isMandatory: Boolean // mandatory == not empty (string not empty, dict not empty)
    val defaultValue: Any
}

class DestinationParamDefString(
    override val uri: String,
    override val name: String,
    override val isMandatory: Boolean,
    override val defaultValue: String,
) : IDestinationParamDef

class DestinationParamDefDict(
    override val uri: String,
    override val name: String,
    override val isMandatory: Boolean,
    override val defaultValue: Map<String, Any>,
) : IDestinationParamDef

class DestinationParamDefBool(
    override val uri: String,
    override val name: String,
    override val isMandatory: Boolean,
    override val defaultValue: Boolean,
) : IDestinationParamDef
