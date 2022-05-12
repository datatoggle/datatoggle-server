package com.datatoggle.server.api.customer

import com.datatoggle.server.api.customer.DestinationCheck.Companion.enrichSpecificConfigWithDefault
import com.datatoggle.server.db.DbEvent
import com.datatoggle.server.db.DbWorkspace
import com.datatoggle.server.db.DbWorkspaceConnection
import com.datatoggle.server.db.DbWorkspaceDestination
import com.datatoggle.server.db.DbWorkspaceMember
import com.datatoggle.server.db.DbWorkspaceSource
import com.datatoggle.server.db.DbUserAccount
import com.datatoggle.server.db.EventRepo
import com.datatoggle.server.db.WorkspaceConnectionRepo
import com.datatoggle.server.db.WorkspaceDestinationRepo
import com.datatoggle.server.db.WorkspaceMemberRepo
import com.datatoggle.server.db.WorkspaceRepo
import com.datatoggle.server.db.WorkspaceSourceRepo
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


class GetWorkspaceSnippetsReply(
    @Suppress("unused")
    val workspaces: List<RestWorkspaceSnippet>
)

class PostCreateWorkspaceArgs(
    val workspaceName: String
)

data class PostCreateWorkspaceReply(
    val uri: String
)

data class GetWorkspaceReply(
    val workspace: RestWorkspace
)

data class GetUserInfoReply(
    val userInfo: RestUserInfo
)

data class GetDestinationDefsReply(
    val destinationDefs: List<RestDestinationDef>
)

data class PostDestinationConfigArgs(
    val workspaceUri: String,
    val config: RestDestinationConfig
)

data class PostDestinationConfigReply(
    val saved: Boolean,
    val configWithInfo: RestDestinationConfigWithInfo
)

class PostEventArgs(
    val eventName: String,
    val data: Map<String, Any>
)

class PostEventReply()

@CrossOrigin("\${datatoggle.webapp_url}")
@RestController
class CustomerRestApi(
    private val useAccountRepo: UserAccountRepo,
    private val workspaceRepo: WorkspaceRepo,
    private val workspaceDestinationRepo: WorkspaceDestinationRepo,
    private val workspaceSourceRepo: WorkspaceSourceRepo,
    private val workspaceConnectionRepo: WorkspaceConnectionRepo,
    private val workspaceMemberRepo: WorkspaceMemberRepo,
    private val eventRepo: EventRepo,
    private val configExporter: CloudflareConfigExporter,
    @Value("\${datatoggle.destination_scripts_url_prefix}") private val destinationScriptsUrlPrefix: String
) {

    @Transactional
    @GetMapping("/api/customer/workspace-snippets")
    suspend fun getWorkspaceSnippets(@RequestHeader(name="Authorization") token: String): GetWorkspaceSnippetsReply {

        // https://firebase.google.com/docs/auth/admin/verify-id-tokens
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
        var user = useAccountRepo.findByFirebaseAuthUid(decodedToken.uid)
        if (user == null) {
            user = createNewUserAccount(decodedToken)
        }

        val dbWorkspaces = workspaceRepo.findByUserAccountId(user.id)

        val workspaces = dbWorkspaces.map { CustomerRestAdapter.toRestWorkspaceSnippet(it)}
        return GetWorkspaceSnippetsReply(workspaces)
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

    @GetMapping("/api/customer/workspace/{uri}")
    suspend fun getWorkspace(@RequestHeader(name="Authorization") token: String, @PathVariable uri: String): GetWorkspaceReply {
        val user = getLoggedUser(token)

        val dbWorkspace = workspaceRepo.findByUserAccountIdAndWorkspaceUri(user.id, uri)

        val dbDests = workspaceDestinationRepo.findByWorkspaceId(dbWorkspace.id)
        val dbSource = getWorkspaceSource(dbWorkspace)
        val workspace = CustomerRestAdapter.toRestWorkspace(dbWorkspace, dbSource, dbDests)
        return GetWorkspaceReply(workspace)
    }

    @GetMapping("/api/customer/userinfo")
    suspend fun getUserInfo(@RequestHeader(name="Authorization") token: String): GetUserInfoReply {
        val user = getLoggedUser(token)
        return GetUserInfoReply(RestUserInfo(user.uri))
    }

    @Transactional
    @PostMapping("/api/customer/workspaces")
    suspend fun postCreateWorkspace(@RequestHeader(name="Authorization") token: String, @RequestBody args: PostCreateWorkspaceArgs): PostCreateWorkspaceReply {
        val user = getLoggedUser(token)
        val apiKey = UUID.randomUUID()
        val apiKeyStr = apiKey.toString()

        val workspace = workspaceRepo.save(DbWorkspace(
            uri = generateUri(args.workspaceName, apiKeyStr),
            name = args.workspaceName
        ))

        workspaceMemberRepo.save(
            DbWorkspaceMember(
                workspaceId = workspace.id,
                userAccountId = user.id)
        )

        workspaceSourceRepo.save(DbWorkspaceSource(
            uri = generateUri("default-${workspace.uri}"),
            name = "Default source",
            workspaceId = workspace.id,
            apiKey = apiKey
        ))

        val success = configExporter.exportConf(apiKeyStr, buildConfig(workspace))
        if (!success){
            throw Exception("could not export config for workspace '${workspace.uri}'")
        }

        return PostCreateWorkspaceReply(workspace.uri)
    }

    @GetMapping("/api/customer/destination-defs")
    suspend fun getDestinationDefs(@RequestHeader(name="Authorization") token: String): GetDestinationDefsReply{

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
        val errors = DestinationCheck.checkConfigParams(args.config.destinationUri, args.config.destinationSpecificConfig)

        if (errors.isNotEmpty() && args.config.isEnabled){
            return PostDestinationConfigReply(
                false, RestDestinationConfigWithInfo(args.config, errors)
            )
        }

        val workspace = workspaceRepo.findByUserAccountIdAndWorkspaceUri(user.id, args.workspaceUri)
        val dbSource = getWorkspaceSource(workspace)

        val previousDbDest =
            workspaceDestinationRepo.findByDestinationUriAndWorkspaceId(args.config.destinationUri, workspace.id)

        if (previousDbDest == null && args.config.isEnabled) {
            // a new destination is always created as disabled
            // this should not happen
            throw Exception("Cannot save a new destination config as enabled")
        }

        // here: either there is no error or config is not enabled: we can save it
        val enrichedConfig = enrichSpecificConfigWithDefault(
            args.config.destinationUri,
            args.config.destinationSpecificConfig)

        val dbDest = DbWorkspaceDestination(
            id = previousDbDest?.id ?: 0,
            enabled = args.config.isEnabled,
            workspaceId = workspace.id,
            destinationUri = args.config.destinationUri,
            destinationSpecificConfig = DbUtils.mapToJson(enrichedConfig),
            lastModificationDatetime = Instant.now()
        )

        val savedDest = workspaceDestinationRepo.save(dbDest)

        if (previousDbDest == null) { // new destination -> create connection
            workspaceConnectionRepo.save(
                DbWorkspaceConnection(
                    workspaceId = workspace.id,
                    sourceId = dbSource.id,
                    destinationId = savedDest.id
                )
            )
        }

        val restResult = CustomerRestAdapter.toRestDestinationConfigWithInfo(savedDest)

        val success = configExporter.exportConf(dbSource.apiKey.toString(), buildConfig(workspace))
        if (!success){
            // NB: this should be done asynchronously, with retry and logging
            throw Exception("could not export config for workspace '${workspace.uri}'")
        }

        return PostDestinationConfigReply(
            true,
            restResult
        )
    }


    private suspend fun getWorkspaceSource(workspace: DbWorkspace): DbWorkspaceSource {
        val dbSources = workspaceSourceRepo.findByWorkspaceId(workspace.id)
        if (dbSources.size != 1) {
            throw InvalidObjectException("there should be exactly one source for workspace '${workspace.uri}'")
        }
        val dbSource = dbSources.first()
        return dbSource
    }


    private suspend fun getLoggedUser(token: String): DbUserAccount {
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
        val user = useAccountRepo.findByFirebaseAuthUid(decodedToken.uid)
        return user!!
    }

    suspend fun buildConfig(dbWorkspace: DbWorkspace) : ClientGlobalConfig {
        val dbDests = workspaceDestinationRepo
            .findByWorkspaceId(dbWorkspace.id)
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


    @Transactional
    @PostMapping("/api/customer/event")
    suspend fun postEvent(@RequestHeader(name="Authorization") token: String, @RequestBody args: PostEventArgs): PostEventReply {
        val user = getLoggedUser(token)
        eventRepo.save(
            DbEvent(
                userAccountUri = user.uri,
                eventName = args.eventName,
                eventData = DbUtils.mapToJson(args.data)
            )
        )
        return PostEventReply()
    }
}
