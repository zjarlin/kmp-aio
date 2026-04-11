package site.addzero.entity2iso.processor.context

object Settings {
    var isomorphicPkg: String = "site.addzero.generated.isomorphic"
    var isomorphicGenDir: String = ""
    var isomorphicSerializableEnabled: Boolean = true

    fun fromOptions(options: Map<String, String>) {
        isomorphicPkg = options["isomorphicPkg"] ?: "site.addzero.generated.isomorphic"
        isomorphicGenDir = options["isomorphicGenDir"] ?: ""
        isomorphicSerializableEnabled =
            options["isomorphicSerializableEnabled"]?.equals("true", ignoreCase = true) ?: true
    }
}
