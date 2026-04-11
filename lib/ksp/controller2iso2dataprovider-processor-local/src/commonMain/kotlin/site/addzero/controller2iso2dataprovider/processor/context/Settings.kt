package site.addzero.controller2iso2dataprovider.processor.context

/**
 * controller2iso2dataprovider 处理器的最小配置集。
 */
object Settings {
    var sharedComposeSourceDir: String = ""
    var iso2DataProviderPackage: String = "site.addzero.generated.forms.dataprovider"
    var apiClientPackageName: String = "site.addzero.generated.api"
    var apiClientAggregatorObjectName: String = "Apis"
    var isomorphicPackageName: String = "site.addzero.generated.isomorphic"

    fun fromOptions(options: Map<String, String>) {
        sharedComposeSourceDir = options["sharedComposeSourceDir"] ?: ""
        iso2DataProviderPackage =
            options["iso2DataProviderPackage"] ?: "site.addzero.generated.forms.dataprovider"
        apiClientPackageName = options["apiClientPackageName"] ?: "site.addzero.generated.api"
        apiClientAggregatorObjectName =
            options["apiClientAggregatorObjectName"]?.takeIf(String::isNotBlank) ?: "Apis"
        isomorphicPackageName = options["isomorphicPackageName"] ?: "site.addzero.generated.isomorphic"
    }
}
