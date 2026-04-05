plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.ksp.ksp-jvm-cache-preparation")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

val libs = versionCatalogs.named("libs")
val generatedApiOutputDir = project(":apps:kcloud:plugins:vibepocket")
    .projectDir
    .resolve("generated/commonMain/kotlin/site/addzero/kcloud/api")
    .absolutePath

dependencies {
    add("kspJvm", libs.findLibrary("org-babyfish-jimmer-jimmer-ksp").get())
    add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
    add("kspJvm", libs.findLibrary("site-addzero-controller2api-processor").get())
}

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.api")
    arg("apiClientOutputDir", generatedApiOutputDir)
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:shared"))
            implementation(project(":apps:kcloud:plugins:vibepocket"))
            implementation(project(":lib:config-center"))
            implementation(project(":lib:api:api-music-spi"))
            implementation(project(":lib:api:api-suno"))
            implementation(project(":lib:api:api-netease"))
            implementation(project(":lib:api:api-qqmusic"))
            implementation(project(":apps:kcloud:plugins:system:config-center"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
            implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
            implementation(libs.findLibrary("spring2ktor-server-core").get())
            implementation("site.addzero:tool-api-suno:2026.02.06")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.57")
            implementation("org.unbescape:unbescape:1.1.6.RELEASE")
            implementation("javazoom:jlayer:1.0.1")
            compileOnly(libs.findLibrary("org-springframework-spring-web").get())
        }
    }
}


tasks.register("generateRouteApis") {
    group = "code generation"
    description = "Generate Ktorfit APIs from Spring-style route sources."
    dependsOn("kspKotlinJvm")
}
