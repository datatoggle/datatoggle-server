package com.datatoggle.server

import com.datatoggle.server.api.user.v0.UserConfigCache
import com.datatoggle.server.tools.FileLoader.Companion.getFileFromResource
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.io.InputStream
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner


@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
	val serviceAccount: InputStream = getFileFromResource("secrets/firebase_private_key.json")

	val options: FirebaseOptions = FirebaseOptions.builder()
		.setCredentials(GoogleCredentials.fromStream(serviceAccount))
		.build()

	FirebaseApp.initializeApp(options)

	runApplication<ServerApplication>(*args)
}


@Component
class AppStartupRunner(private val userConfigCache: UserConfigCache) : ApplicationRunner {

	override fun run(args: ApplicationArguments) {
		runBlocking {
			userConfigCache.initCache()
		}
	}

}
