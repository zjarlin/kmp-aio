package site.addzero.kcloud.platform

expect object DirectoryLauncher {
    fun openDirectory(path: String): Boolean
}
