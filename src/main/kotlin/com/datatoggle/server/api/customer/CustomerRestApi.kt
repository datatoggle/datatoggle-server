package com.datatoggle.server.api.customer

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.datatoggle.server.db.DbWorkspace
import com.datatoggle.server.db.DbWorkspaceConnection
import com.datatoggle.server.db.DbWorkspaceDestination
import com.datatoggle.server.db.DbWorkspaceMember
import com.datatoggle.server.db.DbWorkspaceSource
import com.datatoggle.server.db.DbUserAccount
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

@CrossOrigin("\${datatoggle.webapp_url}")
@RestController
class CustomerRestApi(
    private val useAccountRepo: UserAccountRepo,
    private val workspaceRepo: WorkspaceRepo,
    private val workspaceDestinationRepo: WorkspaceDestinationRepo,
    private val workspaceSourceRepo: WorkspaceSourceRepo,
    private val workspaceConnectionRepo: WorkspaceConnectionRepo,
    private val workspaceMemberRepo: WorkspaceMemberRepo,
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

        val upvotyToken = JWT.create()
            .withClaim("id", user.uri)
            .withClaim("name", user.uri)
            .sign(Algorithm.HMAC256("f4e41729b5df13965c8cb000dfd87eb4"))

        return GetUserInfoReply(RestUserInfo(user.uri, upvotyToken))
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
        val workspace = workspaceRepo.findByUserAccountIdAndWorkspaceUri(user.id, args.workspaceUri)
        val dbSource = getWorkspaceSource(workspace)

        val previousDbDest =
            workspaceDestinationRepo.findByDestinationUriAndWorkspaceId(args.config.destinationUri, workspace.id)

        val dbDest = DbWorkspaceDestination(
            id = previousDbDest?.id ?: 0,
            enabled = args.config.isEnabled,
            workspaceId = workspace.id,
            destinationUri = args.config.destinationUri,
            destinationSpecificConfig = DbUtils.mapToJson(args.config.destinationSpecificConfig), //args.config.config
            lastModificationDatetime = Instant.now()
        )

        // we don't save data if it's invalid and enabled
        val savedDest = workspaceDestinationRepo.save(dbDest)

        if (previousDbDest == null){ // new destination -> create connection
            workspaceConnectionRepo.save(DbWorkspaceConnection(
                workspaceId = workspace.id,
                sourceId = dbSource.id,
                destinationId = savedDest.id
            ))
        }

        val result = CustomerRestAdapter.toRestDestinationConfigWithInfo(savedDest)

        val success = configExporter.exportConf(dbSource.apiKey.toString(), buildConfig(workspace))
        if (!success){
            throw Exception("could not export config for workspace '${workspace.uri}'")
        }

        return PostDestinationConfigReply(
            true,
            result
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
}
