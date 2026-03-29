package site.addzero.kcloud

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform