package com.datatoggle.server.customer


import com.datatoggle.server.db.CustomerDestinationRepo
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
    val config: RestCustomerConfig
)


@CrossOrigin("\${datatoggle.webapp_url}")
@RestController
class CustomerRestApi(
    private val customerRepo: CustomerRepo,
    private val customerDestinationRepo: CustomerDestinationRepo
) {

    @PostMapping("/api/customer/get_config")
    suspend fun postGetConfig(@RequestBody args: PostGetConfigArgs): PostGetConfigReply {

        // https://firebase.google.com/docs/auth/admin/verify-id-tokens
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(args.authToken)
        val firebaseAuthUid = decodedToken.uid

        var customer = customerRepo.findByFirebaseAuthUid(firebaseAuthUid)

        if (customer == null) {
            val uri = generateUri(decodedToken.email, firebaseAuthUid)
            customer = customerRepo.save(
                DbCustomer(
                    uri = uri,
                    userApiKey = UUID.randomUUID(),
                    firebaseAuthUid = firebaseAuthUid
                )
            )
        }

        val dbDests = customerDestinationRepo.findByCustomerId(customer.id)
        val destinations = CustomerRestAdapter.getDestinations(dbDests)
        return PostGetConfigReply(RestCustomerConfig(customer.userApiKey.toString(), destinations))
    }

}
