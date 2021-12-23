package com.datatoggle.server.api.customer

import com.datatoggle.server.db.DbProject
import com.datatoggle.server.db.DbProjectConnection
import com.datatoggle.server.db.DbProjectDestination
import com.datatoggle.server.db.DbProjectMember
import com.datatoggle.server.db.DbProjectSource
import com.datatoggle.server.db.DbUserAccount
import com.datatoggle.server.db.ProjectConnectionRepo
import com.datatoggle.server.db.ProjectDestinationRepo
import com.datatoggle.server.db.ProjectMemberRepo
import com.datatoggle.server.db.ProjectRepo
import com.datatoggle.server.db.ProjectSourceRepo
import com.datatoggle.server.db.UserAccountRepo
import com.datatoggle.server.destination.DestinationDef
import com.datatoggle.server.tools.DbUtils
import com.datatoggle.server.tools.generateUri
import com.datatoggle.server.user.CloudflareConfigExporter
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import java.io.InvalidObjectException
import java.time.Instant
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
    val config: RestDestinationConfig
)

data class PostDestinationConfigReply(
    val saved: Boolean,
    val configWithInfo: RestDestinationConfigWithInfo
)

@CrossOrigin("\${datatoggle.webapp_url}")
@RestController
class CustomerRestApi(
    private val useAccountRepo: UserAccountRepo,
    private val projectRepo: ProjectRepo,
    private val projectDestinationRepo: ProjectDestinationRepo,
    private val projectSourceRepo: ProjectSourceRepo,
    private val projectConnectionRepo: ProjectConnectionRepo,
    private val projectMemberRepo: ProjectMemberRepo,
    private val configExporter: CloudflareConfigExporter,
    @Value("\${datatoggle.destination_scripts_url_prefix}") private val destinationScriptsUrlPrefix: String
) {

    @Transactional
    @GetMapping("/api/customer/project-snippets")
    suspend fun getProjectSnippets(@RequestHeader(name="Authorization") token: String): GetProjectSnippetsReply {

        // https://firebase.google.com/docs/auth/admin/verify-id-tokens
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
        var user = useAccountRepo.findByFirebaseAuthUid(decodedToken.uid)
        if (user == null) {
            user = createNewUserAccount(decodedToken)
        }

        val dbProjects = projectRepo.findByUserAccountId(user.id)

        val projects = dbProjects.map { CustomerRestAdapter.toRestProjectSnippet(it)}
        return GetProjectSnippetsReply(projects)
    }

    private suspend fun createNewUserAccount(
        decodedToken: FirebaseToken
    ): DbUserAccount {
        val userUri = generateUri(decodedToken.email, decodedToken.uid)

        return useAccountRepo.save(
            DbUserAccount(
                uri = userUri,
                firebaseAuthUid = decodedToken.uid
            )
        )
    }

    @GetMapping("/api/customer/project/{uri}")
    suspend fun getProject(@RequestHeader(name="Authorization") token: String, @PathVariable uri: String): GetProjectReply {
        val user = getLoggedUser(token)

        val dbProject = projectRepo.findByUserAccountIdAndProjectUri(user.id, uri)

        val dbDests = projectDestinationRepo.findByProjectId(dbProject.id)
        val dbSource = getProjectSource(dbProject)
        val project = CustomerRestAdapter.toRestProject(dbProject, dbSource, dbDests)
        return GetProjectReply(project)
    }


    @Transactional
    @PostMapping("/api/customer/projects")
    suspend fun postCreateProject(@RequestHeader(name="Authorization") token: String, @RequestBody args: PostCreateProjectArgs): PostCreateProjectReply {
        val user = getLoggedUser(token)
        val apiKey = UUID.randomUUID()
        val apiKeyStr = apiKey.toString()

        val project = projectRepo.save(DbProject(
            uri = generateUri(args.projectName, apiKeyStr),
            name = args.projectName
        ))

        projectMemberRepo.save(
            DbProjectMember(
                projectId = project.id,
                userAccountId = user.id)
        )

        projectSourceRepo.save(DbProjectSource(
            uri = generateUri("default-${project.uri}"),
            name = "Default source",
            projectId = project.id,
            apiKey = apiKey
        ))

        val success = configExporter.exportConf(apiKeyStr, buildConfig(project))
        if (!success){
            throw Exception("could not export config for project '${project.uri}'")
        }

        return PostCreateProjectReply(project.uri)
    }

    @GetMapping("/api/customer/destination-defs")
    suspend fun getDestinationDefs(): GetDestinationDefsReply{
        return GetDestinationDefsReply(
            DestinationDef.values().asList()
            .map { CustomerRestAdapter.toRestDestinationDef(it) }
        )
    }

    @Transactional
    @PostMapping("/api/customer/destination-configs")
    suspend fun postUpsertDestinationConfig(
        @RequestHeader(name="Authorization") token: String,
        @RequestBody args: PostDestinationConfigArgs): PostDestinationConfigReply {

        val user = getLoggedUser(token)
        val project = projectRepo.findByUserAccountIdAndProjectUri(user.id, args.projectUri)
        val dbSource = getProjectSource(project)

        val previousDbDest =
            projectDestinationRepo.findByDestinationUriAndProjectId(args.config.destinationUri, project.id)

        val dbDest = DbProjectDestination(
            id = previousDbDest?.id ?: 0,
            enabled = args.config.isEnabled,
            projectId = project.id,
            destinationUri = args.config.destinationUri,
            destinationSpecificConfig = DbUtils.mapToJson(args.config.destinationSpecificConfig), //args.config.config
            lastModificationDatetime = Instant.now()
        )

        // we don't save data if it's invalid and enabled
        val savedDest = projectDestinationRepo.save(dbDest)

        if (previousDbDest == null){ // new destination -> create connection
            projectConnectionRepo.save(DbProjectConnection(
                projectId = project.id,
                sourceId = dbSource.id,
                destinationId = savedDest.id
            ))
        }

        val result = CustomerRestAdapter.toRestDestinationConfigWithInfo(savedDest)

        val success = configExporter.exportConf(dbSource.apiKey.toString(), buildConfig(project))
        if (!success){
            throw Exception("could not export config for project '${project.uri}'")
        }

        return PostDestinationConfigReply(
            true,
            result
        )
    }

    private suspend fun getProjectSource(project: DbProject): DbProjectSource {
        val dbSources = projectSourceRepo.findByProjectId(project.id)
        if (dbSources.size != 1) {
            throw InvalidObjectException("there should be exactly one source for project '${project.uri}'")
        }
        val dbSource = dbSources.first()
        return dbSource
    }


    private suspend fun getLoggedUser(token: String): DbUserAccount {
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
        val user = useAccountRepo.findByFirebaseAuthUid(decodedToken.uid)
        return user!!
    }

    suspend fun buildConfig(dbProject: DbProject) : ClientGlobalConfig {
        val dbDests = projectDestinationRepo
            .findByProjectId(dbProject.id)
            .filter { it.enabled }

        val dests = dbDests.map {
            val destinationDef = DestinationDef.byUri[it.destinationUri]!!
            ClientDestinationConfig(
                scriptUrl = "${destinationScriptsUrlPrefix}/${destinationDef.uri}/dist/index.js",
                scriptName = "datatoggle_${destinationDef.uri}",
                name = destinationDef.displayName,
                destinationSpecificConfig = DbUtils.jsonToMap(it.destinationSpecificConfig)
            )
        }
        val lastModification = dbDests
            .maxByOrNull { it.lastModificationDatetime }
            ?.lastModificationDatetime ?: Instant.now()

        return ClientGlobalConfig(
            lastModification = lastModification.toString(),
            destinations = dests
        )
    }
}
