# Github Packages Gradle

_Easily add [GitHub Packages Repository](https://github.com/features/packages)
to your `build.gradle(.kts?)` file with a standardized credential
configuration._

```kotlin
plugins {
	kotlin("jvm") version "1.9.0"
	id("no.ghpkg") version "0.1.2"
	`maven-publish`
}

group = "org.example"
version = System.getenv().getOrDefault("VERSION", "UNVERSIONED")

repositories {
	mavenCentral()
	git.hub("octocat", "hello-world")
}

configure<PublishingExtension> {
	repositories {
		git.hub("example-org", "example-project")
	}
	publications {
		register<MavenPublication>("gpr") {
			from(components["kotlin"])
		}
	}
}
```

## Preparing your environment

You need to create a CLASSIC personal access token.

## Preparing macOS or Linux

Open a new terminal. Now you want to write the username and PAT to a shell
configuration file of your choice. Usually `.profile` works everywhere. But
depending on your login shell you may also use `.bashrc`, `.zshrc` or `.fishrc`.

You can run `echo $SHELL` to figure out which shell configuration file you
should use.

![img_1.png](.github/assets/unix/shell.png)

Replace `.profile` with your desired shell configuration file (optional).

Replace `username` with your GitHub username.

Replace `...` with your GitHub PAT.

```shell
echo "export GITHUB_ACTOR=\"username\"" >> ~/.profile
echo "export GITHUB_TOKEN=\"...\"" >> ~/.profile
```

![img.png](./.github/assets/unix/environment-variables-unix.png)

## Preparing on Windows

Press the `WIN` button and search for `environment`.

![img](./.github/assets/windows/start-edit-the-system-environment-variables.jpeg)

By now there should be a best match with
`Edit the system environment variables`. Click it and a new windows will appear.
In the new Window click on **Environment Variables**.

![img](./.github/assets/windows/system-properties.jpeg)

When you click the button a new window will open. Click the **New...** button
inside the `User variables for <username>`.

![img](./.github/assets/windows/environment-variables-window.jpeg)

Write in Variable name: `GITHUB_ACTOR` with your GitHub username.

![img](./.github/assets/windows/environment-variable-github_actor.jpeg)

Press OK. Do the same again with the `GITHUB_TOKEN`.

Now press OK and Apply.

## Verify that it's working

To verify if this is working or not you can **restart** IntelliJ IDEA, open any
project, click edit run configurations and see if you can find GITHUB_ACTOR and
GITHUB_TOKEN in the System Configurations.

If you don't see the variables in the system environment settings in the run
options in IntelliJ you might've missed something. Feel free to ask for help.

## GitHub Actions

```yaml
permissions:
  contents: read
  packages: write
steps:
  # ... prepare job ...
  - name: Publish package
    uses: gradle/gradle-build-action@749f47bda3e44aa060e82d7b3ef7e40d953bd629
    with:
      arguments: publish
    env:
      GITHUB_ACTOR: ${{ github.actor }}
      GITHUB_TOKEN: ${{ github.token }}
      VERSION: ${{ inputs.version }}
```
