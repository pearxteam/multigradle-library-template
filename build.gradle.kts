import com.github.breadmoirai.githubreleaseplugin.GithubReleaseExtension
import net.pearx.multigradle.util.MultiGradleExtension

val projectChangelog: String by project
val projectDescription: String by project

val pearxRepoUsername: String? by project
val pearxRepoPassword: String? by project
val sonatypeOssUsername: String? by project
val sonatypeOssPassword: String? by project
val githubAccessToken: String? by project
val devBuildNumber: String? by project

plugins {
    id("net.pearx.multigradle.simple.project")
    id("org.jetbrains.kotlin.multiplatform") apply (false)
    id("com.github.breadmoirai.github-release")
    `maven-publish`
    signing
}

group = "net.pearx.@PROJECT_NAME@"
description = projectDescription

configure<MultiGradleExtension> {
    if (devBuildNumber != null) {
        projectVersion = "$projectVersion-dev-$devBuildNumber"
    }
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set(artifactId)
            description.set(projectDescription)
            url.set("https://github.com/pearxteam/@PROJECT_NAME@")
            licenses {
                license {
                    name.set("Mozilla Public License, Version 2.0")
                    url.set("https://mozilla.org/MPL/2.0/")
                    distribution.set("repo")
                }
            }
            organization {
                name.set("PearX Team")
                url.set("https://pearx.net/")
            }
            developers {
                developer {
                    id.set("mrapplexz")
                    name.set("mrapplexz")
                    email.set("me@pearx.net")
                    url.set("https://pearx.net/members/mrapplexz")
                    organization.set("PearX Team")
                    organizationUrl.set("https://pearx.net/")
                    roles.set(listOf("developer"))
                    timezone.set("Asia/Yekaterinburg")
                }
            }
            scm {
                url.set("https://github.com/pearxteam/@PROJECT_NAME@")
                connection.set("scm:git:git://github.com/pearxteam/@PROJECT_NAME@")
                developerConnection.set("scm:git:git://github.com/pearxteam/@PROJECT_NAME@")
            }
            issueManagement {
                system.set("GitHub")
                url.set("https://github.com/pearxteam/@PROJECT_NAME@/issues")
            }
            ciManagement {
                system.set("GitHub")
                url.set("https://github.com/pearxteam/@PROJECT_NAME@/actions")
            }
        }
    }
    repositories {
        maven {
            credentials {
                username = pearxRepoUsername
                password = pearxRepoPassword
            }
            name = "pearx-repo-develop"
            url = uri("https://repo.pearx.net/maven2/develop/")
        }
        maven {
            credentials {
                username = pearxRepoUsername
                password = pearxRepoPassword
            }
            name = "pearx-repo-release"
            url = uri("https://repo.pearx.net/maven2/release/")
        }
        maven {
            credentials {
                username = sonatypeOssUsername
                password = sonatypeOssPassword
            }
            name = "sonatype-oss-release"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
}

tasks {
    val publishDevelop by registering {
        group = "publishing"
        dependsOn(withType<PublishToMavenRepository>().matching { it.repository.name.endsWith("-develop") })
    }
    val publishRelease by registering {
        group = "publishing"
        dependsOn(withType<PublishToMavenRepository>().matching { it.repository.name.endsWith("-release") })
    }
    val release by registering {
        group = "publishing"
        dependsOn(publishRelease)
        dependsOn(named("githubRelease"))
    }
}

configure<SigningExtension> {
    sign(publishing.publications)
}

configure<GithubReleaseExtension> {
    setToken(githubAccessToken)
    setOwner("pearxteam")
    setRepo("@PROJECT_NAME@")
    setTargetCommitish("master")
    setBody(projectChangelog)
    //setReleaseAssets((publishing.publications["maven"] as MavenPublication).artifacts.map { it.file })
}