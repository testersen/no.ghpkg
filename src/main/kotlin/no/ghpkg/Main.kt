@file:Suppress("unused")

package no.ghpkg

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.plugins.PublishingPlugin
import java.net.URI

class GithubPackagesPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		installOn(project, project.repositories)
		project.plugins.withType(PublishingPlugin::class.java) {
			installOn(project, project.extensions.getByType(PublishingExtension::class.java))
		}
	}

	private fun installOn(project: Project, repositories: RepositoryHandler) {
		(repositories as ExtensionAware).extensions.create("git", Git::class.java, project, repositories)
	}

	private fun installOn(project: Project, publishing: PublishingExtension) {
		installOn(project, publishing.repositories)
	}
}

private val INVALID_CHARS = Regex("[^A-Za-z0-9_.]+")
private fun fixRepoName(name: String) = name.replace(INVALID_CHARS, "-")

open class Git(private val project: Project, private val repositories: RepositoryHandler) {
	fun hub(owner: String, repository: String): MavenArtifactRepository = repositories.maven { repo ->
		repo.name = fixRepoName("GithubPackages $owner/$repository")
		repo.url = URI.create("https://maven.pkg.github.com/$owner/$repository")
		repo.credentials { cred ->
			cred.username = project.findProperty("gpr.actor") as String? ?: System.getenv("GITHUB_ACTOR")
				?: throw Exception("\$GITHUB_ACTOR (or gpr.actor from ~/.gradle/gradle.properties) was not found!")
			cred.password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
				?: throw Exception("\$GITHUB_TOKEN (or gpr.token from ~/.gradle/gradle.properties) was not found!")
		}
	}
}
