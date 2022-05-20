import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0" // https://github.com/jlleitschuh/ktlint-gradle

    // to deploy to google cloud artifact registry, following https://cloud.google.com/artifact-registry/docs/java/store-java
    id("maven-publish")
    id("com.google.cloud.artifactregistry.gradle-plugin") version "2.1.5"
}

group = "com.datatoggle"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    // to deploy to google cloud artifact registry, following https://cloud.google.com/artifact-registry/docs/java/store-java
    maven("artifactregistry://europe-west1-maven.pkg.dev/datatoggle-b83b6/java-for-cloud-run-repo")
}

dependencies {
    // default deps from spring initializer
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.r2dbc:r2dbc-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")

    implementation("com.google.firebase:firebase-admin:7.1.1")
    implementation("com.google.cloud.sql:cloud-sql-connector-r2dbc-postgres:1.3.0") // to connect to cloud sql
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// to deploy to google cloud artifact registry, following https://cloud.google.com/artifact-registry/docs/java/store-java
publishing {
    repositories {
        maven("artifactregistry://europe-west1-maven.pkg.dev/datatoggle-b83b6/java-for-cloud-run-repo")
    }
    // https://docs.gradle.org/current/userguide/publishing_maven.html#sec:identity_values_in_the_generated_pom
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

// https://docs.gradle.org/current/userguide/upgrading_version_6.html#publishing_spring_boot_applications
