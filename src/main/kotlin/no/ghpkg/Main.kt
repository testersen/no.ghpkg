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
		project.extensions.create("versioning", Versioning::class.java)
		installOn(project, project.repositories)
		project.plugins.withType(PublishingPlugin::class.java) {
			installOn(project, project.extensions.getByType(PublishingExtension::class.java).repositories)
		}
	}

	private fun installOn(project: Project, repositories: RepositoryHandler) {
		(repositories as ExtensionAware).extensions.create("git", Git::class.java, project, repositories)
		(repositories as ExtensionAware).extensions.create("github", Github::class.java, project, repositories)
	}
}

open class Versioning {
	/**
	 * Returns the content of environment variable `VERSION` if it is defined,
	 * `"UNVERSIONED"` otherwise.
	 */
	fun environment(): String {
		return System.getenv("VERSION") ?: "UNVERSIONED"
	}
}

private val INVALID_CHARS = Regex("[^A-Za-z0-9_.]+")
private fun fixRepoName(name: String) = name.replace(INVALID_CHARS, "-")

open class Git(private val project: Project, private val repositories: RepositoryHandler) {
	internal val usernameNullable: String? =
		project.findProperty("gpr.actor") as String? ?: System.getenv("GITHUB_ACTOR")
	internal val passwordNullable: String? =
		project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")

	internal val username
		get() = usernameNullable
			?: throw Exception("\$GITHUB_ACTOR (or gpr.actor from ~/.gradle/gradle.properties) was not found!")
	internal val password
		get() = passwordNullable
			?: throw Exception("\$GITHUB_TOKEN (or gpr.token from ~/.gradle/gradle.properties) was not found!")

	fun hub(owner: String, repository: String = "*"): MavenArtifactRepository = repositories.maven { repo ->
		repo.name = fixRepoName("GithubPackages $owner$repository")
		repo.url = URI.create("https://maven.pkg.github.com/$owner$repository")
		repo.credentials {
			it.username = username
			it.password = password
		}
	}
}

open class Github(project: Project, repositories: RepositoryHandler) : Git(project, repositories) {
	/**
	 * Calls [Git.hub] with the owner and repository based on the `GITHUB_` environment variables.
	 */
	fun actions() {
		if ((System.getenv("GITHUB_ACTIONS") ?: "false") != "true") return
		val (owner, repository) = System.getenv("GITHUB_REPOSITORY")?.split("/")
			?: throw Exception("\$GITHUB_REPOSITORY is missing from the environment variables! In CI/CD set this to \${{ github.repository }}")
		this.hub(owner, repository)
	}
}
