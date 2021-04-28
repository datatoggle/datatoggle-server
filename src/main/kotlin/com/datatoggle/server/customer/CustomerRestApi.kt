package com.datatoggle.server.customer

import com.datatoggle.server.db.DbCustomer
import com.datatoggle.server.db.CustomerRepo
import com.datatoggle.server.db.DbProject
import com.datatoggle.server.db.DbProjectDestination
import com.datatoggle.server.db.ProjectDestinationRepo
import com.datatoggle.server.db.ProjectRepo
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.tools.generateUri
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*


class GetProjectSnippetsReply(
    @Suppress("unused")
    val projects: List<RestProjectSnippet>
)

class PostCreateProjectArgs(
    val projectName: String
)

data class PostCreateProjectReply(
    val uri: String
)

data class GetProjectReply(
    val project: RestProject
)

data class GetDestinationDefsReply(
    val destinationDefs: List<RestDestinationDef>
)

data class PostDestinationConfigArgs(
    val projectUri: String,
    val destinationUri: String,
    val config: RestDestinationConfig
)

data class PostDestinationConfigReply(
    val saved: Boolean,
    val configWithInfo: RestDestinationConfigWithInfo
)

@CrossOrigin("\${datatoggle.webapp_url}")
@RestController
class CustomerRestApi(
    private val customerRepo: CustomerRepo,
    private val projectRepo: ProjectRepo,
    private val projectDestinationRepo: ProjectDestinationRepo
) {

    @GetMapping("/api/customer/project-snippets")
    suspend fun getProjectSnippets(@RequestHeader(name="Authorization") token: String): GetProjectSnippetsReply {

        // https://firebase.google.com/docs/auth/admin/verify-id-tokens
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
        var customer = customerRepo.findByFirebaseAuthUid(decodedToken.uid)

        if (customer == null) {
            customer = createNewCustomer(decodedToken)
        }

        val dbProjects = projectRepo.findByCustomerId(customer.id)

        val projects = dbProjects.map { CustomerRestAdapter.toRestProjectSnippet(it)}
        return GetProjectSnippetsReply(projects)
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

    @GetMapping("/api/customer/project/{uri}")
    suspend fun getProject(@RequestHeader(name="Authorization") token: String, @PathVariable uri: String): GetProjectReply {
        val customer = getLoggedCustomer(token)

        val dbProject = projectRepo.findByUriAndCustomerId(uri, customer.id)
        val dbDests = projectDestinationRepo.findByProjectId(dbProject.id)
        val project = CustomerRestAdapter.toRestProject(dbProject, dbDests)
        return GetProjectReply(project)
    }


    @PostMapping("/api/customer/projects")
    suspend fun postCreateProject(@RequestHeader(name="Authorization") token: String, @RequestBody args: PostCreateProjectArgs): PostCreateProjectReply {
        val customer = getLoggedCustomer(token)
        val apiKey = UUID.randomUUID()
        val project = projectRepo.save(DbProject(
            uri = generateUri("${args.projectName}-${customer.uri}", apiKey.toString()),
            name = args.projectName,
            apiKey = apiKey,
            customerId = customer.id
        ))
        return PostCreateProjectReply(project.uri)
    }

    @GetMapping("/api/customer/destination-defs")
    suspend fun getDestinationDefs(): GetDestinationDefsReply{
        return GetDestinationDefsReply(
            DestinationDef.values().asList()
            .map { CustomerRestAdapter.toRestDestinationDef(it) }
        )
    }

    @PostMapping("/api/customer/destination-configs")
    suspend fun postUpsertDestinationConfig(
        @RequestHeader(name="Authorization") token: String,
        @RequestBody args: PostDestinationConfigArgs): PostDestinationConfigReply {

        val customer = getLoggedCustomer(token)
        val project = projectRepo.findByUriAndCustomerId(args.projectUri, customer.id)

        val dbDest = DbProjectDestination(
            enabled = args.config.isEnabled,
            projectId = project.id,
            destinationUri = args.destinationUri,
            config = args.config.config
        )

        val restWithInfo = CustomerRestAdapter.toRestDestinationConfigWithInfo(dbDest)
        // we don't save data if it's invalid and enabled
        return if (restWithInfo.paramErrors.isEmpty() || !dbDest.enabled){
            val saved = projectDestinationRepo.save(dbDest)
            val result = CustomerRestAdapter.toRestDestinationConfigWithInfo(saved)
            PostDestinationConfigReply(
                true,
                result
            )
        } else {
            PostDestinationConfigReply(
                false,
                restWithInfo
            )
        }
    }

    private suspend fun getLoggedCustomer(token: String): DbCustomer {
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
        val customer = customerRepo.findByFirebaseAuthUid(decodedToken.uid)
        return customer!!
    }
}
