package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenClass
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenContext
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenContextBinding
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenContextBindingValue
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenContextDefinition
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenContextParamDefinition
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenMethod
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenProperty
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.by

/**
 * 集中定义代码生成上下文查询抓取器。
 */
object CodegenContextFetchers {
    val contextSummary: Fetcher<CodegenContext> = newFetcher(CodegenContext::class).by {
        allScalarFields()
        protocolTemplate {
            allScalarFields()
        }
    }

    val contextDetail: Fetcher<CodegenContext> = newFetcher(CodegenContext::class).by {
        allScalarFields()
        protocolTemplate {
            allScalarFields()
        }
        classes {
            allScalarFields()
            bindings {
                allScalarFields()
                definition {
                    allScalarFields()
                }
                values {
                    allScalarFields()
                    paramDefinition {
                        allScalarFields()
                    }
                }
            }
            methods {
                allScalarFields()
                bindings {
                    allScalarFields()
                    definition {
                        allScalarFields()
                    }
                    values {
                        allScalarFields()
                        paramDefinition {
                            allScalarFields()
                        }
                    }
                }
            }
            properties {
                allScalarFields()
                bindings {
                    allScalarFields()
                    definition {
                        allScalarFields()
                    }
                    values {
                        allScalarFields()
                        paramDefinition {
                            allScalarFields()
                        }
                    }
                }
            }
        }
    }

    val definitionDetail: Fetcher<CodegenContextDefinition> = newFetcher(CodegenContextDefinition::class).by {
        allScalarFields()
        params {
            allScalarFields()
        }
    }

    val classDetail: Fetcher<CodegenClass> = newFetcher(CodegenClass::class).by {
        allScalarFields()
        methods {
            allScalarFields()
        }
        properties {
            allScalarFields()
        }
        bindings {
            allScalarFields()
            definition {
                allScalarFields()
            }
            values {
                allScalarFields()
            }
        }
    }

    val methodDetail: Fetcher<CodegenMethod> = newFetcher(CodegenMethod::class).by {
        allScalarFields()
        bindings {
            allScalarFields()
            definition {
                allScalarFields()
            }
            values {
                allScalarFields()
                paramDefinition {
                    allScalarFields()
                }
            }
        }
    }

    val propertyDetail: Fetcher<CodegenProperty> = newFetcher(CodegenProperty::class).by {
        allScalarFields()
        bindings {
            allScalarFields()
            definition {
                allScalarFields()
            }
            values {
                allScalarFields()
                paramDefinition {
                    allScalarFields()
                }
            }
        }
    }

    val bindingDetail: Fetcher<CodegenContextBinding> = newFetcher(CodegenContextBinding::class).by {
        allScalarFields()
        definition {
            allScalarFields()
        }
        values {
            allScalarFields()
            paramDefinition {
                allScalarFields()
            }
        }
    }

    val bindingValueDetail: Fetcher<CodegenContextBindingValue> = newFetcher(CodegenContextBindingValue::class).by {
        allScalarFields()
        paramDefinition {
            allScalarFields()
        }
    }

    val paramDefinitionDetail: Fetcher<CodegenContextParamDefinition> = newFetcher(CodegenContextParamDefinition::class).by {
        allScalarFields()
    }
}
