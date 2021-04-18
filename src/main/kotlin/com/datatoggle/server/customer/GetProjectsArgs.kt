package com.datatoggle.server.customer

import com.datatoggle.server.db.DbCustomer
import com.datatoggle.server.db.CustomerRepo
import com.datatoggle.server.db.DbProject
import com.datatoggle.server.db.ProjectDestinationRepo
import com.datatoggle.server.db.ProjectRepo
import com.datatoggle.server.tools.generateUri
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import java.util.*

abstract class RequestBaseArgs(
    val authToken: String
)

class GetProjectsArgs(authToken: String) : RequestBaseArgs(authToken)

class GetProjectsReply(
    val projects: List<RestProject>
)

class PostCreateProjectArgs(
    authToken: String,
    val projectName: String
) : RequestBaseArgs(authToken)

data class PostCreateProjectReply(
    val project: RestProject
)

@CrossOrigin("\${datatoggle.webapp_url}")
@RestController
class CustomerRestApi(
    private val customerRepo: CustomerRepo,
    private val projectRepo: ProjectRepo,
    private val projectDestinationRepo: ProjectDestinationRepo
) {

    @GetMapping("/api/customer/projects")
    suspend fun getProjects(@RequestBody args: GetProjectsArgs): GetProjectsReply {

        // https://firebase.google.com/docs/auth/admin/verify-id-tokens
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(args.authToken)
        var customer = customerRepo.findByFirebaseAuthUid(decodedToken.uid)

        if (customer == null) {
            customer = createNewCustomer(decodedToken)
        }

        val dbProjects = projectRepo.findByCustomerId(customer.id)
        val dbDestsByProj = dbProjects.map {
            it to projectDestinationRepo.findByProjectId(it.id)
        }

        val projects = dbDestsByProj.map { CustomerRestAdapter.getProject(it.first, it.second)}
        return GetProjectsReply(projects)
    }

    private suspend fun createNewCustomer(
        decodedToken: FirebaseToken
    ): DbCustomer {
        val uri = generateUri(decodedToken.email, decodedToken.uid)
        return customerRepo.save(
            DbCustomer(
                uri = uri,
                firebaseAuthUid = decodedToken.uid
            )
        )
    }


    @PostMapping("/api/customer/project")
    suspend fun postCreateProject(@RequestBody args: PostCreateProjectArgs): PostCreateProjectReply {
        val customer = getLoggedCustomer(args)
        val apiKey = UUID.randomUUID()
        val project = projectRepo.save(DbProject(
            uri = generateUri("${args.projectName}_${customer.uri}", apiKey.toString()),
            name = args.projectName,
            apiKey = apiKey,
            customerId = customer.id
        ))
        return PostCreateProjectReply(CustomerRestAdapter.getProject(project, listOf()))
    }

    private suspend fun getLoggedCustomer(args: RequestBaseArgs): DbCustomer {
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(args.authToken)
        val customer = customerRepo.findByFirebaseAuthUid(decodedToken.uid)
        return customer!!
    }
}
