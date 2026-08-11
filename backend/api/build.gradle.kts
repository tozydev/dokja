plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.ktfmt)
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(platform(libs.spring.modulith.bom))
    implementation(platform(libs.aws.sdk.kotlin.bom))

    implementation(springLibs.spring.boot.starter.data.jpa)
    implementation(springLibs.spring.boot.starter.data.redis)
    implementation(springLibs.spring.boot.starter.flyway)
    implementation(springLibs.spring.boot.starter.security)
    implementation(springLibs.spring.boot.starter.oauth2.client)
    implementation(springLibs.spring.boot.starter.oauth2.resource.server)
    implementation(springLibs.spring.boot.starter.webmvc)

    implementation(springLibs.jackson.module.kotlin)
    implementation(kotlinLibs.kotlin.reflect)
    implementation(springLibs.flyway.database.postgresql)

    implementation(awsLibs.kotlin.s3)

    implementation(springLibs.spring.modulith.starter.core)
    // implementation(springLibs.spring.modulith.starter.jpa)

    developmentOnly(springLibs.spring.boot.devtools)
    developmentOnly(springLibs.spring.boot.docker.compose)

    runtimeOnly(springLibs.postgresql)
    runtimeOnly(springLibs.spring.modulith.runtime)

    testImplementation(springLibs.spring.boot.starter.data.jpa.test)
    testImplementation(springLibs.spring.boot.starter.flyway.test)
    testImplementation(springLibs.spring.boot.starter.security.test)
    testImplementation(springLibs.spring.boot.starter.security.oauth2.client.test)
    testImplementation(springLibs.spring.boot.starter.security.oauth2.resource.server.test)
    testImplementation(springLibs.spring.boot.starter.webmvc.test)
    testImplementation(springLibs.spring.boot.testcontainers)
    testImplementation(kotlinLibs.kotlin.test.junit5)
    testImplementation(springLibs.spring.modulith.starter.test)
    testImplementation(springLibs.testcontainers.junit.jupiter)
    testImplementation(springLibs.testcontainers.postgresql)

    testRuntimeOnly(springLibs.junit.platform.launcher)
}

kotlin {
    jvmToolchain(25)

    compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

ktfmt { kotlinLangStyle() }

tasks { withType<Test> { useJUnitPlatform() } }
