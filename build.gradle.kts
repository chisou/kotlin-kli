import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    `maven-publish`
}

group = "org.chisou.arch"
version = "1.2"
val artifact = "kli"

val slf4jVersion  = "1.7.26"

val kotlintestVersion = "2.0.7"
val kluentVersion = "1.49"

val junitRunnerVersion = "3.3.1"


repositories {
    mavenCentral()
    jcenter()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
//    runtime ("org.slf4j:slf4j-simple:$slf4jVersion")

    testImplementation("io.kotlintest:kotlintest:$kotlintestVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.chisou.test:slf4j-mockito:1.1")

    testRuntime("io.kotlintest:kotlintest-runner-junit5:$junitRunnerVersion")
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifactId = artifact
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}