package site.addzero.coding.playground.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GeneratedManagedDeclaration(
    val declarationId: String,
    val fileId: String,
    val presetType: String,
    val targetId: String = "",
    val managedBy: String = "coding-playground",
)
