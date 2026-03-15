package site.addzero.vibepocket.platform

expect object DirectoryLauncher {
    fun openDirectory(path: String): Boolean
}
