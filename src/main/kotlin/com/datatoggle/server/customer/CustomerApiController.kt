package com.datatoggle.server.customer


import com.datatoggle.server.db.DbCustomer
import com.datatoggle.server.db.CustomerRepo
import com.datatoggle.server.tools.generateUri
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import org.springframework.web.bind.annotation.CrossOrigin


data class PostGetConfigArgs(
    val authToken: String
)

data class PostGetConfigReply(
    val config: CustomerConfig
)

data class CustomerConfig(
    val apiKey: String,
    val destinationConfigs: List<CustomerDestinationConfig>)

data class CustomerDestinationConfig(
    val isEnabled: Boolean,
    val destinationSpecificConfig: List<CustomerDestinationSpecificConfigParam>
)

enum class ParamType{
    Boolean,
    String,
    Float,
    Int
}

data class CustomerDestinationSpecificConfigParam(
    val name: String,
    val type: ParamType,
    val value: Any
)

@CrossOrigin("\${datatoggle.webapp_url}")
@RestController
class CustomerApiController(private val customerRepo: CustomerRepo) {

    @PostMapping("/api/customer/get_config")
    suspend fun postGetConfig(@RequestBody args: PostGetConfigArgs) : PostGetConfigReply{

        // https://firebase.google.com/docs/auth/admin/verify-id-tokens
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(args.authToken)
        val firebaseAuthUid = decodedToken.uid

        var customer = customerRepo.findByFirebaseAuthUid(firebaseAuthUid)
        if (customer == null){
            val uri = generateUri(decodedToken.email, firebaseAuthUid)
            customer = customerRepo.save(DbCustomer(uri = uri, userApiKey = UUID.randomUUID(), firebaseAuthUid = firebaseAuthUid))
        }
        return PostGetConfigReply(CustomerConfig(customer.userApiKey.toString(), listOf()))
    }

}
