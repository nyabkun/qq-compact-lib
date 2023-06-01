/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.compact

import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString
import nyab.build.qCurClassPathJarLibraries
import nyab.conf.qJAVA_HOME
import nyab.util.file
import nyab.util.path
import nyab.util.qList
import nyab.util.qLog
import nyab.util.sep
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.compiler.createSourceFilesFromSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.com.intellij.mock.MockApplication
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.application.ApplicationManager
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmAnalysisFlags
import org.jetbrains.kotlin.config.JvmDefaultMode
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.load.java.JavaClassesTracker
import org.jetbrains.kotlin.load.java.JavaTypeEnhancementState
import org.jetbrains.kotlin.load.java.Jsr305Settings
import org.jetbrains.kotlin.load.java.ReportLevel
import org.jetbrains.kotlin.load.java.lazy.ModuleClassResolver
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BodyResolver
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.DeclarationResolver
import org.jetbrains.kotlin.resolve.DescriptorResolver
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzer
import org.jetbrains.kotlin.resolve.OverloadResolver
import org.jetbrains.kotlin.resolve.OverrideResolver
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode
import org.jetbrains.kotlin.resolve.TypeResolver
import org.jetbrains.kotlin.resolve.VarianceChecker
import org.jetbrains.kotlin.resolve.calls.CallExpressionResolver
import org.jetbrains.kotlin.resolve.calls.CallResolver
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
// TODO cache result
class QCompactLibAnalysis(val analysisCtx: QAnalysisContext, val lib: QCompactLib, val isTest: Boolean) {
    // << Root of the CallChain >>
    fun doAnalyse() {
        analysisResult
    }

    // << Root of the CallChain >>
    val messageCollector: MessageCollector = MessageCollector.NONE
//    PrintingMessageCollector(System.out, MessageRenderer.PLAIN_RELATIVE_PATHS, false)

    // << Root of the CallChain >>
    val myClassPaths: List<Path> = qCurClassPathJarLibraries

    // << Root of the CallChain >>
    val kotlinStdLibPaths: List<String> =
        (System.getenv("KOTLIN_HOME").path sep "lib").qList().map { it.pathString }.toList()

    // << Root of the CallChain >>
    val compilerConfiguration: CompilerConfiguration by lazy {
        // https://stackoverflow.com/a/61865167/5570400
        setIdeaIoUseFallback()

        CompilerConfiguration().apply {
            // https://discuss.kotlinlang.org/t/embedded-scripting-warning/8975
            // WARN: Failed to initialize native filesystem for Windows
            // java.lang.RuntimeException: Could not find installation home path. Please make sure bin/idea.properties is present in the installation directory.
            this.addJvmClasspathRoots(myClassPaths.map { it.toFile() })

            put(JVMConfigurationKeys.OUTPUT_DIRECTORY, File("./.q_compact_build"))
            put(JVMConfigurationKeys.USE_PSI_CLASS_FILES_READING, true)
//        val libPath = qCurClassPathJarLibraries.map { it.pathString }.toMutableList()
//        libPath.addAll(qKotlinStdLibPaths)
            put(JVMConfigurationKeys.ENABLE_JVM_PREVIEW, true)
//        put(JVMConfigurationKeys.INCLUDE_RUNTIME, true)
            put(JVMConfigurationKeys.FRIEND_PATHS, myClassPaths.map { it.pathString })
//        val libPath = qKotlinStdLibPaths.map { it.path }.toMutableList()
//        libPath += qMyClassPaths
//        val libs = libPath.distinct().map { it.slash }.toList()
//        put(JVMConfigurationKeys.KLIB_PATHS, libs)
//        put(JVMConfigurationKeys.KLIB_PATHS, qKotlinStdLibPaths)
//        put(CommonConfigurationKeys.KLIB_NORMALIZE_ABSOLUTE_PATH, true)
            put(CommonConfigurationKeys.USE_FIR, true)
            put(CommonConfigurationKeys.USE_FIR_EXTENDED_CHECKERS, true)
            put(CommonConfigurationKeys.DISABLE_INLINE, true)
            put(CommonConfigurationKeys.REPORT_OUTPUT_FILES, true)
            put(CommonConfigurationKeys.INCREMENTAL_COMPILATION, true)
            put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, false)
            put(JVMConfigurationKeys.OUTPUT_DIRECTORY, ".q_compact_build".file)

            put(JVMConfigurationKeys.DO_NOT_CLEAR_BINDING_CONTEXT, true)
            put(JVMConfigurationKeys.JDK_HOME, qJAVA_HOME.toFile())
            put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
            put(JVMConfigurationKeys.DISABLE_OPTIMIZATION, true)
            put(CommonConfigurationKeys.MODULE_NAME, "Auto Generated Compact Lib")
            put(
                CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS,
                LanguageVersionSettingsImpl(
                    languageVersion = LanguageVersion.LATEST_STABLE,
                    apiVersion = ApiVersion.LATEST_STABLE,
                    analysisFlags = mapOf(
                        JvmAnalysisFlags.javaTypeEnhancementState to JavaTypeEnhancementState(
//                        Jsr305Settings(ReportLevel.STRICT, ReportLevel.STRICT)
                            Jsr305Settings(ReportLevel.IGNORE, ReportLevel.IGNORE)
                        ) { ReportLevel.IGNORE },
                        JvmAnalysisFlags.jvmDefaultMode to JvmDefaultMode.ENABLE
                    )
                )
            )

            put(JVMConfigurationKeys.PARAMETERS_METADATA, false)
            put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_1_8)
        }
    }

    // << Root of the CallChain >>
    val kotlinCoreEnvironment: KotlinCoreEnvironment =
        // https://github.com/escanda/kotlin/commit/3ead2e9cd4d13d04f6a18b46d06d68f8a5fda037
        // > createForProduction creates and caches JavaCoreApplicationEnvironment instance, which can alter behavior of subsequent tests
        KotlinCoreEnvironment.createForProduction(
            // https://plugins.jetbrains.com/docs/intellij/disposers.htm
            Disposer.newDisposable(),
            compilerConfiguration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )

    // << Root of the CallChain >>
    val applicationManager: MockApplication = ApplicationManager.getApplication() as MockApplication

    // << Root of the CallChain >>
    val project: MockProject by lazy {
        kotlinCoreEnvironment.project as MockProject
    }

    // << Root of the CallChain >>
    val projectEnvironment by lazy {
        kotlinCoreEnvironment.projectEnvironment.qLog()
    }

    // << Root of the CallChain >>
    val srcRoots = if (!isTest) {
        lib.srcDirs.main
    } else {
        lib.srcDirs.main + lib.srcDirs.test
    }

    // << Root of the CallChain >>
    val kotlinSrcRoots: List<KotlinSourceRoot> =
        srcRoots.map { path -> KotlinSourceRoot(path.toAbsolutePath().toString(), false) }.toList()

    // << Root of the CallChain >>
    val bindingTrace: CliBindingTrace = NoScopeRecordCliBindingTrace()

    // << Root of the CallChain >>
    private val packagePartProvider = kotlinCoreEnvironment::createPackagePartProvider

    // << Root of the CallChain >>
    private val declarationProviderFactory = ::FileBasedDeclarationProviderFactory

    // << Root of the CallChain >>
    // TODO
    val klibList: List<KotlinLibrary> = emptyList()
// java.nio.file.NoSuchFileException: /compact/linkdata/module
//    qCurClassPathJarLibraries.map { createKotlinLibrary(org.jetbrains.kotlin.konan.file.File(it), "compact", false, false)}

    // << Root of the CallChain >>
    val targetEnvironment = CompilerEnvironment

    // << Root of the CallChain >>
    val ktFiles: List<KtFile> by lazy {
        createSourceFilesFromSourceRoots(compilerConfiguration, project, kotlinSrcRoots)
    }

    // << Root of the CallChain >>
    val searchScope = TopDownAnalyzerFacadeForJVM.newModuleSearchScope(project, ktFiles)

    // << Root of the CallChain >>
    val componentProvider: ComponentProvider = TopDownAnalyzerFacadeForJVM.createContainer(
        project,
        ktFiles,
        bindingTrace,
        compilerConfiguration,
        packagePartProvider,
        declarationProviderFactory,
        targetEnvironment,
        searchScope,
        klibList,
        null,
        emptyList(),
        emptyList(),
        emptyMap()
    )

    // << Root of the CallChain >>
    val moduleDescriptor = componentProvider.get<ModuleDescriptor>()

    // << Root of the CallChain >>
    val lazyTopDownAnalyzer = componentProvider.get<LazyTopDownAnalyzer>()

    // << Root of the CallChain >>
    val analysisResult: AnalysisResult by lazy {
        analysisCtx.phase.nextPhase(QAnalysisPhase.CompilerAnalysisStarted)

        lazyTopDownAnalyzer.analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, ktFiles)

        componentProvider.get<JavaClassesTracker>().onCompletedAnalysis(moduleDescriptor)

        val result = AnalysisResult.success(bindingTrace.bindingContext, moduleDescriptor)

        analysisCtx.phase.nextPhase(QAnalysisPhase.CompilerAnalysisFinished)

        result
    }

    // << Root of the CallChain >>
    val bindingContext: BindingContext by lazy {
        analysisResult.bindingContext
    }

    // << Root of the CallChain >>
    val classResolver: ModuleClassResolver = componentProvider.get()

    // << Root of the CallChain >>
    val overrideResolver: OverrideResolver = componentProvider.get()

    // << Root of the CallChain >>
    val varianceChecker: VarianceChecker = componentProvider.get()

    // << Root of the CallChain >>
    val overloadResolver: OverloadResolver = componentProvider.get()

    // << Root of the CallChain >>
    val bodyResolver: BodyResolver = componentProvider.get()

    // << Root of the CallChain >>
    val descriptorResolver: DescriptorResolver = componentProvider.get()

    // << Root of the CallChain >>
    val declarationResolver: DeclarationResolver = componentProvider.get()

    // << Root of the CallChain >>
    val resolveSession: ResolveSession = componentProvider.get()

    // << Root of the CallChain >>
    val typeResolver: TypeResolver = componentProvider.get()

    // << Root of the CallChain >>
    val callResolver: CallResolver = componentProvider.get()

    // << Root of the CallChain >>
    val callExpressionResolver: CallExpressionResolver = componentProvider.get()
}