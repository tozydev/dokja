@file:Suppress("UnstableApiUsage")

import dev.aga.gradle.versioncatalogs.Generator.generate
import dev.aga.gradle.versioncatalogs.GeneratorConfig

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.aga.gradle.version-catalog-generator") version "4.2.2"
}

dependencyResolutionManagement {
    repositories { mavenCentral() }
    versionCatalogs {
        val kebabCaseSuffixGenerator: (String, String, String) -> String = { _, _, artifact ->
            artifact.replace(Regex("[._]"), "-").replaceFirstChar { it.lowercase() }
        }

        generate("springLibs") {
            fromToml("spring-boot-dependencies", "spring-modulith-bom") {
                generateLibraryVersions = false
                aliasPrefixGenerator = { group, artifact ->
                    when (
                        val prefix = GeneratorConfig.DEFAULT_ALIAS_PREFIX_GENERATOR(group, artifact)
                    ) {
                        in listOf("bind", "activemq") -> prefix
                        "jackson" -> "jackson2"
                        else -> ""
                    }
                }
                aliasSuffixGenerator = kebabCaseSuffixGenerator
            }
        }
        generate("kotlinLibs") {
            fromToml("kotlin-bom") {
                generateLibraryVersions = false
                aliasPrefixGenerator = { _, _ -> "" }
                aliasSuffixGenerator = kebabCaseSuffixGenerator
            }
        }
        generate("awsLibs") {
            fromToml("aws-sdk-kotlin-bom") {
                generateLibraryVersions = false
                aliasSuffixGenerator = kebabCaseSuffixGenerator
            }
        }
    }
}

rootProject.name = "dokja-backend"

include(":api")
