plugins {
    kotlin("jvm") version "1.9.0"
    id("com.gradle.plugin-publish") version "1.1.0"
    `java-gradle-plugin`
}

group = "no.ghpkg"
version = "0.1.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    @Suppress("UnstableApiUsage") website.set("https://github.com/testersen/no.ghpkg")
    @Suppress("UnstableApiUsage") vcsUrl.set("https://github.com/testersen/no.ghpkg")
    plugins {
        create("githubPackages") {
            id = "no.ghpkg"
            displayName = "Github Packages Repositories"
            description = "Quickly with common format add github package repositories"
            implementationClass = "no.ghpkg.GithubPackagesPlugin"
            @Suppress("UnstableApiUsage") tags.set(listOf("ghpkg"))
        }
    }
}

kotlin {
    jvmToolchain(17)
}
