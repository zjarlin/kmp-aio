package site.addzero.coding.playground.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class GeneratedCodeIndexProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return GeneratedCodeIndexProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options,
        )
    }
}
