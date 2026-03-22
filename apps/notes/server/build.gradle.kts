plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}

apply(from = rootProject.file("gradle/spring2ktor-ksp-cache-workaround.gradle.kts"))

application {
    mainClass.set("site.addzero.notes.server.ApplicationKt")
}

val libs = versionCatalogs.named("libs")
val ktorVersion = libs.findVersion("ktor").get().requiredVersion
val sqliteVersion = libs.findVersion("org-xerial-sqlite-jdbc-v3").get().requiredVersion
val postgresVersion = libs.findVersion("org-postgresql-postgresql").get().requiredVersion

ksp {
    arg("springKtor.generatedPackage", "site.addzero.notes.server.generated.springktor")
}

dependencies {
    implementation(project(":lib:starter-koin"))
    implementation(projects.lib.starterStatuspages)

    implementation("site.addzero:spring2ktor-server-core:2026.03.13")
    ksp("site.addzero:spring2ktor-server-processor:2026.03.13")
    compileOnly("org.springframework:spring-web:5.3.21")

    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    runtimeOnly(libs.findLibrary("ch-qos-logback-logback-classic").get())
}
