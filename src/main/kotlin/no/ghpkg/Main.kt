@file:Suppress("unused")

package no.ghpkg

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import java.net.URI

class GithubPackagesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        installOn(project, project.repositories)
        if (project.project.plugins.hasPlugin("maven-publish")) {
            val allProjectsRepositories =
                (project.allprojects as ExtensionAware).extensions.getByType(PublishingExtension::class.java).repositories
            val subProjectsRepositories =
                (project.subprojects as ExtensionAware).extensions.getByType(PublishingExtension::class.java).repositories
            installOn(project, allProjectsRepositories)
            installOn(project, subProjectsRepositories)
        }
    }

    private fun installOn(project: Project, repositories: RepositoryHandler) {
        (repositories as ExtensionAware).extensions.create("git", Git::class.java, project, repositories)
    }
}

private val INVALID_CHARS = Regex("[^A-Za-z0-9_\\-.]+")
private val MANY_DASH = Regex("-+")
private fun fixRepoName(name: String) = name.replace(INVALID_CHARS, "-").replace(MANY_DASH, "-")

open class Git(private val project: Project, private val repositories: RepositoryHandler) {
    fun hub(owner: String, repository: String): MavenArtifactRepository = repositories.maven { repo ->
        repo.name = fixRepoName("GithubPackages $owner/$repository")
        repo.url = URI.create("https://maven.pkg.github.com/$owner/$repository")
        repo.credentials { cred ->
            cred.username = project.findProperty("gpr.actor") as String? ?: System.getenv("GITHUB_ACTOR")
            cred.password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
