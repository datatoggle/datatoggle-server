package com.datatoggle.server.user

import com.datatoggle.server.api.customer.ClientGlobalConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody


/*
curl -X PUT "https://api.cloudflare.com/client/v4/accounts/01a7362d577a6c3019a474fd6f485823/storage/kv/namespaces/0f2ac74b498b48028cb68387c421e279/values/My-Key?expiration=1578435000&expiration_ttl=300" \
     -H "X-Auth-Email: user@example.com" \
     -H "X-Auth-Key: c2547eb745079dac9320b638f5e225cf483cc5cfdda41" \
     -H "Content-Type: text/plain" \
     --data '"Some Value"'
 */

data class CloudflareResponse(
    val success: Boolean,
    val errors: List<String>,
    val messages: List<String>
)

@Service
class CloudflareConfigExporter(
    @Value("\${datatoggle.cloudflare.account_id}") private val accountId: String,
    @Value("\${datatoggle.cloudflare.configs_namespace_id}") private val configsNamespaceId: String,
    @Value("\${datatoggle.cloudflare.api_token}") private val apiToken: String
) {

    suspend fun exportConf(apiKey: String, config: ClientGlobalConfig) : Boolean {

        val response = WebClient.create().put()
            .uri("https://api.cloudflare.com/client/v4/accounts/$accountId/storage/kv/namespaces/$configsNamespaceId/values/$apiKey")
            .bodyValue(config)
            .header("Authorization", "Bearer $apiToken")
            .header("Content-Type", "application/json")
            .retrieve()
            .awaitBody<CloudflareResponse>()

        return response.success
    }


}
