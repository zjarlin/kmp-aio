package site.addzero.coding.playground.annotations

data class GeneratedCodeFileDescriptor(
    val fileId: String,
    val targetId: String = "",
    val packageName: String,
    val fileName: String,
    val declarationIds: List<String>,
)

data class GeneratedCodeDeclarationDescriptor(
    val declarationId: String,
    val fileId: String,
    val fqName: String,
    val kind: String,
    val presetType: String,
)

interface GeneratedCodeIndexContract {
    fun files(): List<GeneratedCodeFileDescriptor>
    fun declarations(): List<GeneratedCodeDeclarationDescriptor>
    fun findByFqName(fqName: String): GeneratedCodeDeclarationDescriptor?
    fun findByDeclarationId(declarationId: String): GeneratedCodeDeclarationDescriptor?
}
