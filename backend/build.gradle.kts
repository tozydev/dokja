import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.ktfmt)
}

ktfmt { kotlinLangStyle() }

tasks {
    register("check") {
        description = "Runs all checks."
        dependsOn(ktfmtCheck, ktfmtCheckScripts)
    }

    register<KtfmtFormatTask>("ktfmtPrecommit") {
        description =
            "Reformats only the files passed via --include-only (used by the pre-commit hook)."
        source = project.fileTree(rootDir)
        include("**/*.kt", "**/*.kts")
    }
}
