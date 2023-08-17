@file:Suppress("unused")

package no.ghpkg

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

class GithubPackagesPlugin : Plugin<Project> {
    override fun apply(project: Project) {}
}

private val INVALID_CHARS = Regex("[^A-Za-z0-9_\\-.]+")
private val MANY_DASH = Regex("-+")
private fun fixRepoName(name: String) = name.replace(INVALID_CHARS, "-").replace(MANY_DASH, "-")

fun RepositoryHandler.github(owner: String, repository: String) {
    maven { repo ->
        repo.name = fixRepoName("GithubPackages $owner/$repository")
        repo.url = URI.create("https://maven.pkg.github.com/$owner/$repository")
        repo.credentials { cred ->
            cred.username = System.getenv("GITHUB_ACTOR")
            cred.password = System.getenv("GITHUB_TOKEN")
        }
    }
}
