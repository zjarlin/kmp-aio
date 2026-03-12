plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
}

application {
    mainClass.set("com.kcloud.server.ApplicationKt")
}

dependencies {
    implementation(project(":apps:kcloud"))
}
