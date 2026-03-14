plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.io.swagger.parser.v3.swagger.parser)
            implementation(libs.com.squareup.kotlinpoet)
            implementation(libs.com.squareup.kotlinpoet.ksp)
        }
        commonTest.dependencies {
            implementation(libs.io.kotest.kotest.property)
            implementation(libs.io.kotest.kotest.assertions.core)
            implementation(libs.org.jetbrains.kotlin.kotlin.test)
        }
    }
}
