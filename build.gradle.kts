import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.bundling.Jar

plugins {
    kotlin("jvm") version "1.3.21"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

group = "org.chisou.arch"
version = "1.3.1"

val artifactName = project.name
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()
val vcsUrl = "https://github.com/chisou/kotlin-kli"

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

    testImplementation("io.kotlintest:kotlintest:$kotlintestVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.chisou.test:slf4j-mockito:1.2")

    testRuntime("io.kotlintest:kotlintest-runner-junit5:$junitRunnerVersion")
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

val testSourcesJar by tasks.registering(Jar::class) {
    classifier = "testSources"
    from(sourceSets["test"].allSource)
}

publishing {
    publications {
        create<MavenPublication>(artifactName) {
            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(testSourcesJar.get())
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    publish = true
    setPublications(artifactName)
    with(pkg){
        repo = "maven"
        name = artifactName
        vcsUrl = vcsUrl
        setLicenses("Apache-2.0")
        with(version) {
            name = artifactVersion
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}