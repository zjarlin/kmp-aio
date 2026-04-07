package site.addzero.repro.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.koin.core.error.InstanceCreationException
import org.koin.core.error.NoDefinitionFoundException
import org.koin.dsl.koinApplication
import site.addzero.repro.app.di.BannerLikeStarterOnlyKoinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.repro.app.di.DefaultWideScanOnlyKoinApplication
import site.addzero.repro.app.di.DefaultWideScanWithAutoProviderStarterKoinApplication
import site.addzero.repro.app.di.NoScanStarterOnlyKoinApplication
import site.addzero.repro.app.di.PlainScanOnlyKoinApplication
import site.addzero.repro.app.di.PlainScanWithExplicitProviderKoinApplication
import site.addzero.repro.app.di.WideScanOnlyKoinApplication
import site.addzero.repro.app.di.WideScanWithAutoProviderStarterKoinApplication
import site.addzero.repro.feature.onestarter.BannerLikeStarterService
import site.addzero.repro.feature.onestarter.NoScanStarterService
import site.addzero.repro.feature.gamma.GammaScreenState
import site.addzero.repro.feature.provider.ProviderBackedScreenState

class WideScanKoinTest {
    @Test
    fun singleStarterModuleWithModuleScanAndConfigurationSelfRegisters() {
        val app = koinApplication {
            withConfiguration<BannerLikeStarterOnlyKoinApplication>()
        }

        try {
            val service = app.koin.get<BannerLikeStarterService>()
            assertEquals("banner-like", service.label())
        } finally {
            app.close()
        }
    }

    @Test
    fun configurationWithoutComponentScanDoesNotPickUpSingleClass() {
        val app = koinApplication {
            withConfiguration<NoScanStarterOnlyKoinApplication>()
        }

        try {
            assertFailsWith<NoDefinitionFoundException> {
                app.koin.get<NoScanStarterService>()
            }
        } finally {
            app.close()
        }
    }

    @Test
    fun defaultConfigurationCanOmitCustomLabel() {
        val app = koinApplication {
            withConfiguration<DefaultWideScanOnlyKoinApplication>()
        }

        try {
            val state = app.koin.get<GammaScreenState>()
            assertEquals("gamma(beta(alpha))", state.label())
        } finally {
            app.close()
        }
    }

    @Test
    fun wideCommonMainScanResolvesThreeLeafSingleChains() {
        val app = koinApplication {
            withConfiguration<WideScanOnlyKoinApplication>()
        }

        try {
            val state = app.koin.get<GammaScreenState>()
            assertEquals("gamma(beta(alpha))", state.label())
        } finally {
            app.close()
        }
    }

    @Test
    fun providerStarterDoesNotAutoRegisterWhenConfigurationLabelsDoNotMatch() {
        val app = koinApplication {
            withConfiguration<PlainScanOnlyKoinApplication>()
        }

        try {
            val error = assertFailsWith<Exception> {
                app.koin.get<ProviderBackedScreenState>()
            }
            var cursor: Throwable? = error
            var leaf: NoDefinitionFoundException? = null
            while (cursor != null) {
                if (cursor is NoDefinitionFoundException) {
                    leaf = cursor
                    break
                }
                cursor = cursor.cause
            }
            assertTrue(
                leaf?.message.orEmpty().contains("ProvidedPalette") ||
                    leaf?.message.orEmpty().contains("ProviderBackedScreenState")
            )
        } finally {
            app.close()
        }
    }

    @Test
    fun defaultConfigurationCanUseLibrarySelfRegisteredStarterWithoutExplicitModules() {
        val app = koinApplication {
            withConfiguration<DefaultWideScanWithAutoProviderStarterKoinApplication>()
        }

        try {
            val state = app.koin.get<ProviderBackedScreenState>()
            assertEquals("ocean/dashboard", state.label())
        } finally {
            app.close()
        }
    }

    @Test
    fun explicitModuleInclusionFixesPlainProviderModule() {
        val app = koinApplication {
            withConfiguration<PlainScanWithExplicitProviderKoinApplication>()
        }

        try {
            val state = app.koin.get<ProviderBackedScreenState>()
            assertEquals("ocean/dashboard", state.label())
        } finally {
            app.close()
        }
    }

    @Test
    fun customConfigurationCanAlsoUseSelfRegisteredStarterWhenLabelsMatch() {
        val app = koinApplication {
            withConfiguration<WideScanWithAutoProviderStarterKoinApplication>()
        }

        try {
            val state = app.koin.get<ProviderBackedScreenState>()
            assertEquals("ocean/dashboard", state.label())
        } finally {
            app.close()
        }
    }
}
