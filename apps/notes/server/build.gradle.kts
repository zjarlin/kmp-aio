plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

application {
    mainClass.set("site.addzero.notes.server.ApplicationKt")
}

val libs = versionCatalogs.named("libs")
val ktorVersion = libs.findVersion("ktor").get().requiredVersion
val sqliteVersion = libs.findVersion("org-xerial-sqlite-jdbc-v3").get().requiredVersion
val postgresVersion = libs.findVersion("org-postgresql-postgresql").get().requiredVersion

configurations.configureEach {
    exclude(group = "ch.qos.logback", module = "logback-classic")
    exclude(group = "ch.qos.logback", module = "logback-core")
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    runtimeOnly(libs.findLibrary("org-slf4j-slf4j-simple").get())
}
