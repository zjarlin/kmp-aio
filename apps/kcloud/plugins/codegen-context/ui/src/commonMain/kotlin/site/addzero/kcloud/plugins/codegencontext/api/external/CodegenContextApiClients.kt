package site.addzero.kcloud.plugins.codegencontext.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Single

@Single
class CodegenContextApiClient(
    ktorfit: Ktorfit,
) : CodegenContextApi by ktorfit.createCodegenContextApi()

@Single
class CodegenTemplateApiClient(
    ktorfit: Ktorfit,
) : CodegenTemplateApi by ktorfit.createCodegenTemplateApi()
