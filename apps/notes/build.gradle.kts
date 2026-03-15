import com.codingfeline.buildkonfig.compiler.FieldSpec
import site.addzero.gradle.tool.parseEnvFile

plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

val markdownRendererVersion = "0.29.0"
val buildKonfigFlavor = findProperty("buildkonfig.flavor")?.toString() ?: "dev"
val notesEnv = parseEnvFile(
    file(".env"),
    base = mapOf(
        "BASE_HOST" to "127.0.0.1",
        "BASE_PORT" to "18080",
        "BASE_URL" to "http://127.0.0.1:18080/"
    )
).let { base ->
    parseEnvFile(file(".env.$buildKonfigFlavor"), base)
}

buildkonfig {
    packageName = "site.addzero.notes"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "BASE_HOST", notesEnv.getValue("BASE_HOST"))
        buildConfigField(FieldSpec.Type.STRING, "BASE_PORT", notesEnv.getValue("BASE_PORT"))
        buildConfigField(FieldSpec.Type.STRING, "BASE_URL", notesEnv.getValue("BASE_URL"))
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mikepenz:multiplatform-markdown-renderer-m3:$markdownRendererVersion")
            implementation("com.mikepenz:multiplatform-markdown-renderer-code:$markdownRendererVersion")
        }
        jvmMain.dependencies {
            implementation(project(":apps:notes:server"))
        }
    }
}
