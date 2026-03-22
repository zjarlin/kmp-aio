package site.addzero.vibepocket

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform