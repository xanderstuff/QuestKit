import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
// import pl.allegro.tech.build.axion.release.domain.hooks.HookContext

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.yaml:snakeyaml:2.0")
    }
}

plugins {
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8.0.0"
    id("pl.allegro.tech.build.axion-release") version "1.14.4"
    // id("org.jlleitschuh.gradle.ktlint") version "11.2.0"
}

group = "io.github.xanderstuff"
version = scmVersion.version

val mcApiVersion: String by project
val repoRef: String by project

scmVersion {
    versionIncrementer("incrementMinorIfNotOnRelease", mapOf("releaseBranchPattern" to "release/.+"))

    hooks {
        // FIXME - workaround for Kotlin DSL issue https://github.com/allegro/axion-release-plugin/issues/500
//        pre(
//            "fileUpdate",
//            mapOf(
//                "file" to "CHANGELOG.md",
//                "pattern" to KotlinClosure2<String, HookContext, String>({ v, _ ->
//                    "\\[Unreleased\\]([\\s\\S]+?)\\n(?:^\\[Unreleased\\]: https:\\/\\/github\\.com\\/$repoRef\\/compare\\/[^\\n]*\$([\\s\\S]*))?\\z"
//                }),
//                "replacement" to KotlinClosure2<String, HookContext, String>({ v, c ->
//                    """
//                        \[Unreleased\]
//
//                        ## \[$v\] - ${currentDateString()}$1
//                        \[Unreleased\]: https:\/\/github\.com\/$repoRef\/compare\/v$v...HEAD
//                        \[$v\]: https:\/\/github\.com\/$repoRef\/${if (c.previousVersion == v) "releases/tag/v$v" else "compare/v${c.previousVersion}...v$v"}${'$'}2
//                    """.trimIndent()
//                })
//            )
//        )

        pre("commit")
    }
}

fun currentDateString() = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.3.0-SNAPSHOT")
    implementation("dev.jorel:commandapi-bukkit-shade:9.0.1")

    // We'll probably need stdlib
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.21")
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.8.21")

    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.8.21")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.21")
    implementation("org.jetbrains.kotlin:kotlin-script-util:1.8.21")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.8.21")

    // for @file:@DependsOn, @file:@Repository in scripts
//    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:1.8.21")
    // for @file:@DependsOn, @file:@Repository, @file:@Import in scripts
    implementation("org.jetbrains.kotlin:kotlin-main-kts:1.8.21")
}

tasks {
    wrapper {
        gradleVersion = "8.0.1"
        distributionType = Wrapper.DistributionType.ALL
    }

    processResources {
        val placeholders = mapOf(
            "version" to version,
            "apiVersion" to mcApiVersion,
            "kotlinVersion" to project.properties["kotlinVersion"]
        )

        filesMatching("plugin.yml") {
            expand(placeholders)
        }

        // create an "offline" copy/variant of the plugin.yml with `libraries` omitted
        doLast {
            val resourcesDir = sourceSets.main.get().output.resourcesDir
            val yamlDumpOptions =
                // make it pretty for the people
                DumperOptions().also {
                    it.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                    it.isPrettyFlow = true
                }
            val yaml = Yaml(yamlDumpOptions)
            val pluginYml: Map<String, Any> = yaml.load(file("$resourcesDir/plugin.yml").inputStream())
            yaml.dump(pluginYml.filterKeys { it != "libraries" }, file("$resourcesDir/offline-plugin.yml").writer())
        }
    }

    jar {
        exclude("offline-plugin.yml")
    }

    // offline jar should be ready to go with all dependencies
    shadowJar {
        //TODO: figure out if minimize() can safely be used, even partially
        // (probably need to exclude std-lib, ect. so scripts can still use those dependencies)
//        minimize()
        archiveClassifier.set("offline")
        exclude("plugin.yml")
        rename("offline-plugin.yml", "plugin.yml")
    }

    build {
        dependsOn(shadowJar)
    }
}
