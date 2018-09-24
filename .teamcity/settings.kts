import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_1.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.1"

project {

    vcsRoot(ComponentVcs)
    vcsRoot(ApplicationVcs)

    buildType(Application)
    buildType(Component)
}

object Application : BuildType({
    name = "Application"

    vcs {
        root(ApplicationVcs)

        cleanCheckout = true
        showDependenciesChanges = true
    }

    steps {
        maven {
            name = "Install component"
            goals = "package"
            pomLocation = "install/install.xml"
            workingDir = "install"
            mavenVersion = defaultProvidedVersion()
            useOwnLocalRepo = true
        }
        maven {
            name = "Build application"
            goals = "clean package"
            mavenVersion = defaultProvidedVersion()
            useOwnLocalRepo = true
        }
    }

    triggers {
        vcs {
            watchChangesInDependencies = true
        }
    }

    dependencies {
        dependency(Component) {
            snapshot {
            }

            artifacts {
                artifactRules = "component-1.0-SNAPSHOT.jar"
            }
        }
    }
})

object Component : BuildType({
    name = "Component"

    artifactRules = "target/component-1.0-SNAPSHOT.jar"

    vcs {
        root(ComponentVcs)
    }

    steps {
        maven {
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            mavenVersion = defaultProvidedVersion()
        }
    }
})

object ApplicationVcs : GitVcsRoot({
    name = "ApplicationVcs"
    url = "https://github.com/antonarhipov/application"
    branchSpec = """
        +:refs/heads/(master)
        +:refs/heads/(feature-*)
    """.trimIndent()
})

object ComponentVcs : GitVcsRoot({
    name = "ComponentVcs"
    url = "https://github.com/antonarhipov/component"
    branchSpec = """
        +:refs/heads/(master)
        +:refs/heads/(feature-*)
    """.trimIndent()
})
