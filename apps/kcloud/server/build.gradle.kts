plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
}


kotlin {
    dependencies {
    }
}
val serverMainClass = "site.addzero.kcloud.ApplicationKt"


kotlin.jvm().mainRun {
    mainClass.set(serverMainClass)
}

tasks.named<JavaExec>("runJvm") {
    mainClass.set(serverMainClass)
}
