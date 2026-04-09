package site.addzero.kcloud.plugins.hostconfig.screen

expect class HostConfigProjectSqliteFilePicker() {
    fun pickSqliteFile(
        onPicked: (String?) -> Unit,
    )
}
