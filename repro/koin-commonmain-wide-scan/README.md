# Koin commonMain wide scan repro

This is an isolated KMP repro for one very specific question:

Can a `commonMain` root application use one broad `@ComponentScan("site.addzero.repro")`
to discover `@Single` classes from several child modules?

This repro intentionally isolates DI aggregation only. It uses `commonMain` KMP modules without
adding Compose rendering, because Compose UI does not change how Koin annotation scanning and
configuration discovery behave at compile time.

The repo includes three experiments:

1. `one main + three child modules`, where every child module exposes only `@Single` classes.
2. A provider-module case that distinguishes plain `@Module` from starter-style `@Configuration`.
3. A banner-like single-entry starter case that distinguishes `@Module + @Configuration` from `@Module + @ComponentScan + @Configuration`.

## What this repro is testing

- The root app Koin entry lives in `commonMain`.
- The root app uses a wide scan:

```kotlin
@Module
@Configuration("wide-scan")
@ComponentScan("site.addzero.repro")
class WideScanRootModule
```

- Three child modules (`feature-alpha`, `feature-beta`, `feature-gamma`) contain only `@Single` classes.
- `feature-provider` contains both:
  - one `@Single` class
  - one plain `@Module` provider without `@Configuration`
- `feature-onestarter` contains both:
  - one banner-like starter module with `@Module + @ComponentScan + @Configuration`
  - one control starter module with `@Module + @Configuration` but no `@ComponentScan`

## What is being compared

This repro now covers six app-side patterns:

1. Default configuration, no explicit `modules = [...]`
2. Custom configuration label, no explicit `modules = [...]`
3. Custom configuration label, explicit `modules = [PlainProviderModule::class, ProviderGraphModule::class]`
4. Library-side self-registered provider starter module with `@Configuration`
5. Banner-like single-entry starter module with `@Module + @ComponentScan + @Configuration`
6. Control starter module with `@Module + @Configuration` but no `@ComponentScan`

## Expected result

- Wide `commonMain` scan does resolve the three child `@Single` chains.
- `configurations = ["wide-scan"]` is optional only if you are happy to use the default configuration space.
- `modules = [...]` is optional only if the library exports a self-registered starter module, not just a plain provider module.
- If app and library configuration labels do not match, starter auto-registration does not happen.
- A single starter entry module that relies on external same-package `@Single` classes still needs `@ComponentScan`.

## Run

Use the generated wrapper:

```bash
./gradlew :app:jvmTest
```

Or use a local Gradle installation:

```bash
gradle :app:jvmTest
```

## Verified result

Verified locally on April 7, 2026 with:

```bash
./gradlew :app:jvmTest
```

Observed Koin compiler behavior from the build logs:

- The wide `commonMain` root scan really does cross module boundaries for plain `@Single` classes:

```text
Scanning packages: site.addzero.repro (recursive)
Discovered: AlphaRepository
Discovered: BetaService
Discovered: GammaScreenState
Discovered: ProviderBackedScreenState
```

- The library self-registered starter modules are auto-discovered only when they are annotated with `@Configuration` and their labels match the app root.
- A single starter entry module such as:

```kotlin
@Module
@ComponentScan
@Configuration("banner-like")
class BannerLikeStarterModule
```

  self-registers correctly.
- Removing `@ComponentScan` from that starter keeps the module discoverable, but the external same-package `@Single` class is no longer registered.

## Answer to “what can be omitted?”

Short answer:

- `configurations = ["wide-scan"]`
  - Can be omitted if you switch both app and starter modules to the default configuration.
  - Cannot be omitted if you want an isolated custom configuration space.
- `modules = [PlainProviderModule::class, ProviderGraphModule::class]`
  - Can be omitted if the library defines a public starter module such as:

```kotlin
@Module(includes = [PlainProviderModule::class])
@Configuration
class DefaultProviderStarterModule
```

  - Cannot be omitted if the library only ships a plain `@Module` provider without a `@Configuration` starter wrapper.
- `@ComponentScan` on a single starter module
  - Can be omitted only if that module does not need to pick up external annotated classes.
  - Cannot be omitted for banner-like starters that expect same-package external `@Single` classes to self-register.

## Verified matrix

All of the following were verified by `./gradlew :app:jvmTest`:

1. `DefaultWideScanOnlyKoinApplication`
   - No custom configuration name
   - No explicit `modules = [...]`
   - Works for leaf `@Single` graph
2. `DefaultWideScanWithAutoProviderStarterKoinApplication`
   - No custom configuration name
   - No explicit `modules = [...]`
   - Works because library self-registers `DefaultProviderStarterModule`
3. `WideScanWithAutoProviderStarterKoinApplication`
   - Uses `configurations = ["wide-scan"]`
   - No explicit `modules = [...]`
   - Works because library self-registers `WideScanProviderStarterModule`
4. `PlainScanOnlyKoinApplication`
   - Uses `configurations = ["plain-scan"]`
   - No explicit `modules = [...]`
   - Fails for `ProvidedPalette`, because no library starter matches `plain-scan`
5. `PlainScanWithExplicitProviderKoinApplication`
   - Uses `configurations = ["plain-scan"]`
   - Explicitly adds `PlainProviderModule` and `ProviderGraphModule`
   - Works, but this is app-side manual wiring rather than starter-style self-registration
6. `BannerLikeStarterOnlyKoinApplication`
   - Uses `configurations = ["banner-like"]`
   - No explicit `modules = [...]`
   - Works because the library exports one public `@Module + @ComponentScan + @Configuration` starter entry
7. `NoScanStarterOnlyKoinApplication`
   - Uses `configurations = ["no-scan"]`
   - No explicit `modules = [...]`
   - Fails for `NoScanStarterService`, proving `@Configuration` alone does not populate external same-package `@Single` classes

## Key files

- `app/src/commonMain/kotlin/site/addzero/repro/app/di/WideScanApplications.kt`
- `app/src/commonTest/kotlin/site/addzero/repro/app/WideScanKoinTest.kt`
- `feature-alpha/src/commonMain/kotlin/site/addzero/repro/feature/alpha/AlphaRepository.kt`
- `feature-beta/src/commonMain/kotlin/site/addzero/repro/feature/beta/BetaService.kt`
- `feature-gamma/src/commonMain/kotlin/site/addzero/repro/feature/gamma/GammaScreenState.kt`
- `feature-provider/src/commonMain/kotlin/site/addzero/repro/feature/provider/ProviderFeature.kt`
- `feature-provider/src/commonMain/kotlin/site/addzero/repro/feature/onestarter/OneStarterFeature.kt`
