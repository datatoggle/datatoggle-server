package com.datatoggle.server.api.customer

import com.datatoggle.server.destination.DestinationDef
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class DestinationCheckTest {

    @Test
    fun checkConfigWithValidConfReturnEmptyMap() {

        val specificConfig = mapOf(
            "token" to "tok",
            "config" to mapOf<String, Any>()
        )

        val result = DestinationCheck.checkConfigParams(DestinationDef.Mixpanel.uri, specificConfig)
        Assertions.assertThat(result).isEmpty()
    }

    @Test
    fun checkConfigWithEmptyStringMandatoryReturnError() {

        val specificConfig = mapOf(
            "token" to "",
            "config" to mapOf<String, Any>()
        )

        val result = DestinationCheck.checkConfigParams(DestinationDef.Mixpanel.uri, specificConfig)
        Assertions.assertThat(result.size).isEqualTo(1)
        Assertions.assertThat(result["token"]).isEqualTo("Mandatory")
    }

    @Test
    fun checkConfigWithInvalidStringReturnError() {

        val specificConfig = mapOf(
            "token" to true,
            "config" to mapOf<String, Any>()
        )

        val result = DestinationCheck.checkConfigParams(DestinationDef.Mixpanel.uri, specificConfig)
        Assertions.assertThat(result.size).isEqualTo(1)
        Assertions.assertThat(result["token"]).isEqualTo("Must be a string")
    }
}
