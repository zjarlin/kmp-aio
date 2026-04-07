plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

/** API 生成目录。 */
val generatedApiOutputDir =
    project(":apps:kcloud:plugins:host-config:ui")
        .projectDir
        .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/hostconfig/api/external")
        .absolutePath

/** 共享源码目录。 */
val sharedSourceDir =
    project(":apps:kcloud:plugins:host-config:shared")
        .projectDir
        .resolve("src/commonMain/kotlin")
        .absolutePath

/** 前端源码目录。 */
val sharedComposeSourceDir =
    project(":apps:kcloud:plugins:host-config:ui")
        .projectDir
        .resolve("src/commonMain/kotlin")
        .absolutePath

/** 当前 server 源码目录。 */
val backendServerSourceDir = projectDir.resolve("src/jvmMain/kotlin").absolutePath

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.hostconfig.api.external")
    arg("apiClientOutputDir", generatedApiOutputDir)
    arg("sharedSourceDir", sharedSourceDir)
    arg("sharedComposeSourceDir", sharedComposeSourceDir)
    arg("backendServerSourceDir", backendServerSourceDir)
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:host-config:shared"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
        }
    }
}
