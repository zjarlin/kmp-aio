plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}
kotlin{
    dependencies {
        implementation(libs.io.swagger.parser.v3.swagger.parser)
        implementation(libs.com.squareup.kotlinpoet)
        implementation(libs.com.squareup.kotlinpoet.ksp)
        testImplementation(libs.io.kotest.kotest.property)
        testImplementation(libs.io.kotest.kotest.assertions.core)
        testImplementation(libs.org.jetbrains.kotlin.kotlin.test)
    }
}
