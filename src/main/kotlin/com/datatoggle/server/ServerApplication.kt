package com.datatoggle.server

import com.datatoggle.server.tools.FileLoader.Companion.getFileFromResource
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.InputStream

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
