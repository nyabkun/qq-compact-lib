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

import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*
import kotlin.Comparator
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.readText
import kotlin.reflect.KClass
import nyab.conf.QE
import nyab.conf.QMyCompactLib
import nyab.conf.QMyCompactLibGradle
import nyab.conf.QMyDep
import nyab.conf.QMyPath
import nyab.match.QM
import nyab.util.QDateVersion
import nyab.util.QFuncSrc
import nyab.util.QHash
import nyab.util.QIfExistsCopyFile
import nyab.util.QIfExistsWrite
import nyab.util.QOnlyIfStr
import nyab.util.QRunResult
import nyab.util.QShColor
import nyab.util.conf.QDep
import nyab.util.log
import nyab.util.logGreen
import nyab.util.noStyle
import nyab.util.path
import nyab.util.qCacheIt
import nyab.util.qCamelCaseToKebabCase
import nyab.util.qCopyFileIntoDir
import nyab.util.qDeleteDirContents
import nyab.util.qFind
import nyab.util.qFirstLine
import nyab.util.qGetKotlinPackageNameQuickAndDirty
import nyab.util.qGetTopLevelFunctionsQuickAndDirty
import nyab.util.qHasKotlinMainFunctionQuickAndDirty
import nyab.util.qHash
import nyab.util.qInvokeInstanceFunctionByName
import nyab.util.qIsSubDirOrFileOf
import nyab.util.qNiceIndent
import nyab.util.qNumberOfLines
import nyab.util.qReadFile
import nyab.util.qRelativeTo
import nyab.util.qRunInShell
import nyab.util.qSecondLine
import nyab.util.qStartsWithAny
import nyab.util.qThisFileName
import nyab.util.qThisFilePackageName
import nyab.util.qTimeIt
import nyab.util.qToday
import nyab.util.qWithNewLineSuffix
import nyab.util.qWithNewLineSurround
import nyab.util.qWrite
import nyab.util.re
import nyab.util.sep
import nyab.util.slash
import nyab.util.throwItFile
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFileAnnotationList
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.isTopLevelKtOrJavaMember
import org.jetbrains.kotlin.psi.psiUtil.startOffset

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
@DslMarker
annotation class QCompactLibDsl

// << Root of the CallChain >>
private fun Any.qInvokeBuild(vararg params: Any?): Any? {
    return qInvokeInstanceFunctionByName(params = params, name = "build")
}

// << Root of the CallChain >>
@QCompactLibDsl
class QRootOfChainTopLevelScope(
    val topLevelElement: KtElement,
    val filePath: String,
    val fileName: String,
    val lib: QCompactLib
)

// << Root of the CallChain >>
@QCompactLibDsl
class QRootOfChainNonTopLevelScope(
    val chainNode: QChainNode,
    val topLevel: QTopLevelCompactElement,
    val lib: QCompactLib
)

// << Root of the CallChain >>
@QCompactLibDsl
class QCompactLibRootOfChainScope() {
    // << Root of the CallChain >>
    private var mainTopLevel: QRootOfChainTopLevelScope.() -> Boolean = DEFAULT_ROOT_OF_CHAIN
    // << Root of the CallChain >>
    private var testTopLevel: QRootOfChainTopLevelScope.() -> Boolean = DEFAULT_ROOT_OF_CHAIN

    // << Root of the CallChain >>
    private var mainNonTopLevel: QRootOfChainNonTopLevelScope.() -> Boolean = { true }
    // << Root of the CallChain >>
    private var testNonTopLevel: QRootOfChainNonTopLevelScope.() -> Boolean = { true }

    // << Root of the CallChain >>
    fun mainTopLevel(block: QRootOfChainTopLevelScope.() -> Boolean) {
        mainTopLevel = block
    }

    // << Root of the CallChain >>
    fun testTopLevel(block: QRootOfChainTopLevelScope.() -> Boolean) {
        testTopLevel = block
    }

    // << Root of the CallChain >>
    fun mainNonTopLevel(block: QRootOfChainNonTopLevelScope.() -> Boolean) {
        mainNonTopLevel = block
    }

    // << Root of the CallChain >>
    fun testNonTopLevel(block: QRootOfChainNonTopLevelScope.() -> Boolean) {
        testNonTopLevel = block
    }

    // << Root of the CallChain >>
    private fun build(): QCompactLibRootOfChain {
        return QCompactLibRootOfChain(mainTopLevel, mainNonTopLevel, testTopLevel, testNonTopLevel)
    }
}

// << Root of the CallChain >>
class QCompactLibRootOfChain(
    val mainTopLevel: QRootOfChainTopLevelScope.() -> Boolean,
    val mainNonTopLevel: QRootOfChainNonTopLevelScope.() -> Boolean,
    val testTopLevel: QRootOfChainTopLevelScope.() -> Boolean,
    val testNonTopLevel: QRootOfChainNonTopLevelScope.() -> Boolean,
)

// << Root of the CallChain >>
private val DEFAULT_ROOT_OF_CHAIN: QRootOfChainTopLevelScope.() -> Boolean = { false }

// << Root of the CallChain >>
@QCompactLibDsl
class QCompactLibScope internal constructor(
    val libName: String,
    val author: String
) {
    // << Root of the CallChain >>
    fun baseFileName(baseFileName: String) {
        this.baseFileNames(baseFileName)
    }

    // << Root of the CallChain >>
    fun baseFileNames(baseFileName: String, vararg more: String) {
        destMainSrcFileName = "${baseFileName}.kt"

        destTestSrcFileName = "${baseFileName}Test.kt"

        exampleSrcFileName = "${baseFileName}Example.kt"

        rootOfChain {
            mainTopLevel {
                fileName == "${baseFileName}.kt" || more.any { baseName -> fileName == "$baseName.kt" }
            }
            testTopLevel {
                fileName == "${baseFileName}Test.kt"
            }
        }
    }

    // << Root of the CallChain >>
    private val srcCode = QCompactLibSrcCodeScope()

    // << Root of the CallChain >>
    private val rootOfChain = QCompactLibRootOfChainScope()

    // << Root of the CallChain >>
    fun rootOfChain(block: QCompactLibRootOfChainScope.() -> Unit) {
        rootOfChain.block()
    }

    // << Root of the CallChain >>
    private val srcDirs = QCompactLibSrcDirSetScope()

    // << Root of the CallChain >>
    private val destJarFileName: (version: String) -> String = { version ->
        "$libName-$version.jar"
    }

    // << Root of the CallChain >>
    var createSingleFileSrc: Boolean = true

    // << Root of the CallChain >>
    val visibilityUnchangedClasses: MutableList<KClass<*>> = mutableListOf()

    // << Root of the CallChain >>
    val fullPropagationClasses: MutableList<KClass<*>> = mutableListOf()

    // << Root of the CallChain >>
    val noPropagationClasses: MutableList<KClass<*>> = mutableListOf()

    // << Root of the CallChain >>
    var createLibJarFromSplitFileSrc: Boolean = true

    // << Root of the CallChain >>
    private val destSourcesJarFileName: (version: String) -> String = { version ->
        "$libName-$version-sources.jar"
    }

    // << Root of the CallChain >>
    private val ifFileExists: QCompactLibIfFileExistsScope = QCompactLibIfFileExistsScope()

    // << Root of the CallChain >>
    private val nodeVisitor: QNodeVisitorScope = QNodeVisitorScope()

    // << Root of the CallChain >>
    var versionUpdateStrategy: QVersionUpdateStrategy = QVersionUpdateStrategy.UpdateOnlyIfSplitSrcHashChanged

    // << Root of the CallChain >>
    var versionSuffix: String = ""

    // << Root of the CallChain >>
    private var splitSrcFileImportFilter: QSplitImportFilterScope.() -> Boolean = QSplitImportFilterScope::default

    // << Root of the CallChain >>
    private var nextVersion: QNextVersionScope.() -> String = QNextVersionScope::default
    // << Root of the CallChain >>
    fun nextVersion(version: String) {
        nextVersion = { version }
    }

    // << Root of the CallChain >>
    fun nextVersion(block: QNextVersionScope.() -> String) {
        this.nextVersion = block
    }

    // << Root of the CallChain >>
    fun splitSrcFileImportFilter(block: QSplitImportFilterScope.() -> Boolean) {
        splitSrcFileImportFilter = block
    }

    // << Root of the CallChain >>
    private var singleSrcFileImportFilter: QSingleImportFilterScope.() -> Boolean = QSingleImportFilterScope::default

    // << Root of the CallChain >>
    fun singleSrcFileImportFilter(block: QSingleImportFilterScope.() -> Boolean) {
        singleSrcFileImportFilter = block
    }

    // << Root of the CallChain >>
    private var readme: QReadmeScope.() -> String = QReadmeScope::default

    // << Root of the CallChain >>
    fun readme(block: QReadmeScope.() -> String) {
        readme = block
    }

    // << Root of the CallChain >>
    private var jitpackYml: QJitpackYmlScope.() -> String = QJitpackYmlScope::default

    // << Root of the CallChain >>
    fun jitpackYml(block: QJitpackYmlScope.() -> String) {
        jitpackYml = block
    }

    // << Root of the CallChain >>
    private var singleFileAnnotation: QSingleFileAnnotationCreationScope.() -> String =
        QSingleFileAnnotationCreationScope::default

    // << Root of the CallChain >>
    fun singleFileAnnotation(block: QSingleFileAnnotationCreationScope.() -> String) {
        singleFileAnnotation = block
    }

    // << Root of the CallChain >>
    private var splitFileAnnotation: QSplitFileAnnotationCreationScope.() -> String =
        QSplitFileAnnotationCreationScope::default

    // << Root of the CallChain >>
    var group: String = if (!author.qStartsWithAny("com.", "org.")) {
        "com.github.$author"
    } else {
        author
    }

    // << Root of the CallChain >>
    var readmeDescription: String = "#TODO#"

    // << Root of the CallChain >>
    var references: String = ""

    // << Root of the CallChain >>
    var strictImportCheck: Boolean = false

    // << Root of the CallChain >>
    var jdkForBuild: String = "openjdk17"

    // << Root of the CallChain >>
    var jvmSourceCompatibility = "17"

    // << Root of the CallChain >>
    var jvmTargetCompatibility = "17"

    // << Root of the CallChain >>
    var kotlinVersion = "1.8.20"

    // << Root of the CallChain >>
    var mavenArtifactId: String = libName.qCamelCaseToKebabCase()

    // << Root of the CallChain >>
    var gitHubUserName = author

    // << Root of the CallChain >>
    var gitHubRepoName = libName

    // << Root of the CallChain >>
    var enableSpotless = false

    // << Root of the CallChain >>
    var runTest: Boolean = true

    // << Root of the CallChain >>
    var runExample: Boolean = true

    // << Root of the CallChain >>
    var destMainSrcFileName: String = ""

    // << Root of the CallChain >>
    var destTestSrcFileName: String = ""

    // << Root of the CallChain >>
    var exampleSrcDirToSearch: QCompactLibSrcDirSet.() -> List<Path> = {
        val mainSrcDir = this.main[0]

        listOf(mainSrcDir.resolveSibling("src-example"), mainSrcDir.resolveSibling("src-build"), mainSrcDir)
    }

    // << Root of the CallChain >>
    var exampleSrcFileName: String = ""

    // << Root of the CallChain >>
    var destProjDir: Path = (QMyPath.compact sep libName).toAbsolutePath()

    // << Root of the CallChain >>
    var exampleSrcFile: Path? = null

    // << Root of the CallChain >>
    var destSingleMainDir: Path = destProjDir sep "src-single"

    // << Root of the CallChain >>
    var destSingleTestDir: Path = destProjDir sep "src-test-single"

    // << Root of the CallChain >>
    var destSplitMainDir: Path = destProjDir sep "src-split"

    // << Root of the CallChain >>
    var destSplitTestDir: Path = destProjDir sep "src-test-split"

    // << Root of the CallChain >>
    var destExampleDir: Path = destProjDir sep "src-example"

    // << Root of the CallChain >>
    var dependenciesToCheck: List<QDep> = QMyDep.ALL // TODO Dsl

    // << Root of the CallChain >>
    var srcCharset: Charset = Charsets.UTF_8

    // << Root of the CallChain >>
    private var buildGradleKts: QBuildGradleKtsScope.() -> String = { default() }

    // << Root of the CallChain >>
    fun buildGradleKts(block: QBuildGradleKtsScope.() -> String) {
        buildGradleKts = block
    }

    // << Root of the CallChain >>
    var srcBetweenImportsAndFirstElement: String = """
        // $libName is self-contained single-file library created by $author.
        //  - It can be added to your codebase with a simple copy and paste.
        //  - You can modify and redistribute it under the MIT License.
        //  - Please add a package declaration if necessary.
    """.trimIndent()

    // << Root of the CallChain >>
    var srcBetweenImportsAndFirstElementTest: String = """
        // $libName is self-contained single-file library created by $author.
        // For this test, the source file is also a single-file, self-contained, and contains a runnable main function.
        //  - It can be added to your codebase with a simple copy and paste.
        //  - You can modify and redistribute it under the MIT License.
        //  - Please add a package declaration if necessary.
    """.trimIndent()

    // << Root of the CallChain >>
    var srcBetweenImportsAndFirstElementSplit: String = """
        // $libName is a self-contained single-file library created by $author.
        // This is a split-file version of the library, this file is not self-contained.
    """.trimIndent()

    // << Root of the CallChain >>
    var license: QLicenseScope.() -> String = QMyCompactLib.license

    // << Root of the CallChain >>
    var srcHeader: QLicenseScope.() ->String = QMyCompactLib.src_header

    // << Root of the CallChain >>
    var gitIgnore: String = QMyCompactLib.git_ignore

    // << Root of the CallChain >>
    var topLevelElementsSorter: Comparator<QTopLevelCompactElement> = Comparator<QTopLevelCompactElement> { a, b ->
        a.analysisCtx.isTest.compareTo(b.analysisCtx.isTest)
    }.thenComparator { a, b ->
        a.filePath.compareTo(b.filePath)
    }.thenComparator { a, b ->
        a.lineNumber.compareTo(b.lineNumber)
    }

    // << Root of the CallChain >>
    private fun build(): QCompactLib {
        val srcDirSet = srcDirs.qInvokeBuild() as QCompactLibSrcDirSet

        if (exampleSrcFile == null) {
            exampleSrcFile = exampleSrcDirToSearch(srcDirSet).firstNotNullOfOrNull {
                it.qFind(nameMatcher = QM.exact(exampleSrcFileName), maxDepth = 100)
            }
        } else {
            exampleSrcFileName = exampleSrcFile!!.name
        }

        exampleSrcFile.log

        return QCompactLib(
            phase = QPhase(QLibPhase::class, fg = QShColor.Yellow),
            libName = libName,
            author = author,
            nextVersion = nextVersion,
            versionUpdateStrategy = versionUpdateStrategy,
            versionSuffix = versionSuffix,
            srcCode = srcCode.qInvokeBuild() as QCompactLibSrcCode,
            rootOfChain = rootOfChain.qInvokeBuild() as QCompactLibRootOfChain,
            srcDirs = srcDirSet,
            createSingleFileSrc = createSingleFileSrc,
            createLibJarFromSplitFileSrc = createLibJarFromSplitFileSrc,
            nodeVisitor = nodeVisitor.qInvokeBuild() as QNodeVisitor,
            gitHubUserName = gitHubUserName,
            betweenImportsAndFirstElement = srcBetweenImportsAndFirstElement,
            betweenImportsAndFirstElementSplit = srcBetweenImportsAndFirstElementSplit,
            betweenImportsAndFirstElementTest = srcBetweenImportsAndFirstElementTest,
            buildGradleKts = buildGradleKts,
            dependenciesToCheck = dependenciesToCheck,
            destExampleDir = destExampleDir,
            destExampleSrcFileName = exampleSrcFileName,
            destSingleMainDir = destSingleMainDir,
            destSplitMainDir = destSplitMainDir,
            destMainSrcFileName = destMainSrcFileName,
            destProjDir = destProjDir,
            destSingleTestDir = destSingleTestDir,
            destSplitTestDir = destSplitTestDir,
            destTestSrcFileName = destTestSrcFileName,
            destJarFileName = destJarFileName,
            destSourcesJarFileName = destSourcesJarFileName,
            runExample = runExample,
            runTest = runTest,
            exampleSrcFile = exampleSrcFile,
            gitHubRepoName = gitHubRepoName,
            gitIgnore = gitIgnore,
            group = group,
            jdkForBuild = jdkForBuild,
            jitpackYml = jitpackYml,
            jvmSourceCompatibility = jvmSourceCompatibility,
            jvmTargetCompatibility = jvmTargetCompatibility,
            kotlinVersion = kotlinVersion,
            license = license,
            mavenArtifactId = mavenArtifactId,
            readme = readme,
            readmeDescription = readmeDescription,
            references = references,
            singleSrcFileImportFilter = singleSrcFileImportFilter,
            splitSrcFileImportFilter = splitSrcFileImportFilter,
            srcCharset = srcCharset,
            srcHeader = srcHeader,
            strictImportCheck = strictImportCheck,
            ifFileExists = ifFileExists.qInvokeBuild() as QCompactLibIfFileExists,
            singleFileAnnotation = singleFileAnnotation,
            splitFileAnnotation = splitFileAnnotation,
            topLevelElementsSorter = topLevelElementsSorter,
            enableSpotless = enableSpotless,
            fullPropagationClasses = fullPropagationClasses,
            noPropagationClasses = noPropagationClasses,
            visibilityUnchangedClasses = visibilityUnchangedClasses
        )
    }

    // << Root of the CallChain >>
    fun srcCode(block: QCompactLibSrcCodeScope.() -> Unit) {
        srcCode.block()
    }

    // << Root of the CallChain >>
    fun srcDirs(block: QCompactLibSrcDirSetScope.() -> Unit) {
        srcDirs.block()
    }

    // << Root of the CallChain >>
    fun nonRootNodeVisitor(block: QNodeVisitorScope.() -> Unit) {
        nodeVisitor.block()
    }

    // << Root of the CallChain >>
    fun ifFileExists(block: QCompactLibIfFileExistsScope.() -> Unit) {
        ifFileExists.block()
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QLicenseScope(val lib: QCompactLib, val year: Int, val author: String)

// << Root of the CallChain >>
@QCompactLibDsl
class QNextVersionScope internal constructor(
    val currentLocalVersion: String,
    val latestReleaseVersion: String,
    val suffix: String,
    val lib: QCompactLib
) {
    // << Root of the CallChain >>
    fun default(): String {
        val curDateVersion = QDateVersion.parse(latestReleaseVersion)

        val today = qToday()

        val buildCount =
            if (curDateVersion == null || curDateVersion.date != today || curDateVersion.suffix != suffix) {
                1
            } else {
                curDateVersion.buildCount + 1
            }


        return QDateVersion(prefix = "v", suffix = suffix, today, buildCount).text()
    }
}

// << Root of the CallChain >>
enum class QVersionUpdateStrategy {
    // << Root of the CallChain >>
    UpdateOnlyIfSplitSrcHashChanged,
    // << Root of the CallChain >>
    ForceUpdate,
    // << Root of the CallChain >>
    NoUpdate;
}

// << Root of the CallChain >>
data class QVersion(val version: String, val srcHash: String) {
    companion object {
        // << Root of the CallChain >>
        val NONE = QVersion("", "")

        // << Root of the CallChain >>
        fun parse(versionFile: Path): QVersion {
            return try {
                val content = versionFile.qReadFile()
                val version = content.qFirstLine()
                val hash = content.qSecondLine()

                QVersion(version, hash)
            } catch (e: Exception) {
                NONE
            }
        }
    }

    // << Root of the CallChain >>
    fun write(versionFile: Path): Path {
        return versionFile.qWrite("$version\n$srcHash")
    }
}

// << Root of the CallChain >>
class QCompactLib internal constructor(
    val phase: QPhase<QLibPhase>,
    val libName: String,
    val author: String,
    val nextVersion: QNextVersionScope.() -> String,
    val versionUpdateStrategy: QVersionUpdateStrategy,
    val versionSuffix: String,
    val srcCode: QCompactLibSrcCode,
    val srcDirs: QCompactLibSrcDirSet,
    val createSingleFileSrc: Boolean,
    val createLibJarFromSplitFileSrc: Boolean,
    val ifFileExists: QCompactLibIfFileExists,
    val rootOfChain: QCompactLibRootOfChain,
    val group: String,
    val readmeDescription: String,
    val references: String,
    val strictImportCheck: Boolean,
    val jdkForBuild: String,
    val jvmSourceCompatibility: String,
    val jvmTargetCompatibility: String,
    val kotlinVersion: String,
    val mavenArtifactId: String,
    val gitHubUserName: String,
    val gitHubRepoName: String,
    val splitSrcFileImportFilter: QSplitImportFilterScope.() -> Boolean,
    val singleSrcFileImportFilter: QSingleImportFilterScope.() -> Boolean,
    val destMainSrcFileName: String,
    val destTestSrcFileName: String,
    val destExampleSrcFileName: String,
    val destProjDir: Path,
    val exampleSrcFile: Path?,
    val destSingleMainDir: Path,
    val destSingleTestDir: Path,
    val destSplitMainDir: Path,
    val destSplitTestDir: Path,
    val destExampleDir: Path,
    val destJarFileName: (version: String) -> String,
    val destSourcesJarFileName: (version: String) -> String,
    val runExample: Boolean,
    val runTest: Boolean,
    val dependenciesToCheck: List<QDep> = QMyDep.ALL,
    val srcCharset: Charset = Charsets.UTF_8,
    val buildGradleKts: QBuildGradleKtsScope.() -> String,
    val betweenImportsAndFirstElement: String,
    val betweenImportsAndFirstElementTest: String,
    val betweenImportsAndFirstElementSplit: String,
    val license: QLicenseScope.() -> String = QMyCompactLib.license,
    val srcHeader: QLicenseScope.() -> String = QMyCompactLib.src_header,
    val gitIgnore: String = QMyCompactLib.git_ignore,
    val readme: QReadmeScope.() -> String,
    val jitpackYml: QJitpackYmlScope.() -> String,
    val singleFileAnnotation: (QSingleFileAnnotationCreationScope).() -> String,
    val splitFileAnnotation: (QSplitFileAnnotationCreationScope).() -> String,
    val topLevelElementsSorter: Comparator<QTopLevelCompactElement>,
    val nodeVisitor: QNodeVisitor,
    val enableSpotless: Boolean,
    var visibilityUnchangedClasses: MutableList<KClass<*>>,
    var fullPropagationClasses: MutableList<KClass<*>>,
    var noPropagationClasses: MutableList<KClass<*>>
) {
    companion object {
        // << Root of the CallChain >>
        const val VERSION_PLACEHOLDER = "#VERSION_PLACEHOLDER#"
    }

    // << Root of the CallChain >>
    val hasTest = rootOfChain.testTopLevel != DEFAULT_ROOT_OF_CHAIN

    // << Root of the CallChain >>
    val destArtifactsDir: Path = destProjDir.resolve("build/libs")

    // << Root of the CallChain >>
    val currentVersion: QVersion = QVersion.parse(destProjDir.resolve("VERSION"))

    // << Root of the CallChain >>
    val currentVersionJarFile: Path = destArtifactsDir.resolve(destJarFileName(currentVersion.version))

    // << Root of the CallChain >>
    val currentVersionSourcesJarFile: Path = destArtifactsDir.resolve(destSourcesJarFileName(currentVersion.version))

    // << Root of the CallChain >>
    init {
        phase.nextPhase(QLibPhase.ConfigurationFinished)
    }

    // << Root of the CallChain >>
    fun destMainSingleSrcFilePathFromProjDir(): Path {
        return destProjDir.relativize(destSingleMainSrcFilePath()).normalize()
    }

    // << Root of the CallChain >>
    fun destMainSplitSrcDirPathFromProjDir(): Path {
        return destProjDir.relativize(destSplitMainDir).normalize()
    }

    // << Root of the CallChain >>
    fun destTestSingleSrcFilePathFromProjDir(): Path {
        return destProjDir.relativize(destSingleTestSrcFilePath()).normalize()
    }

    // << Root of the CallChain >>
    fun destTestSplitSrcFilePathFromProjDir(): Path {
        val srcFile = this.destSplitTestDir.qFind(QM.exact(this.destTestSrcFileName), maxDepth = 100)

        return if (srcFile != null) {
            destProjDir.relativize(srcFile).normalize()
        } else {
            destProjDir.relativize(this.destSplitTestDir.resolve(this.destTestSrcFileName)).normalize()
        }
    }

    // << Root of the CallChain >>
    fun destExampleSrcFilePathFromProjDir(): Path {
        return destProjDir.relativize(destExampleSrcFilePath()).normalize()
    }

    // << Root of the CallChain >>
    fun destSingleMainSrcFilePath(): Path {
        return destSingleMainDir sep destMainSrcFileName
    }

    // << Root of the CallChain >>
    fun destSingleTestSrcFilePath(): Path {
        return destSingleTestDir sep destTestSrcFileName
    }

    // << Root of the CallChain >>
    fun destExampleSrcFilePath(): Path {
        return destExampleDir sep destExampleSrcFileName
    }
}

// << Root of the CallChain >>
private fun QAnalysisContext.mainClassFqName(): String {
    val mainFuncNode = this.rootOfChainNode.values.firstOrNull {
        it.isTopLevel() && it.simpleName() == "main()" // TODO test this
    } ?: return ""

    val file = mainFuncNode.containingFile
    return file.qMainClassFqName(lib.srcCharset)
}

// << Root of the CallChain >>
private fun QCompactLib.exampleMainClassFqName(): String {
    return exampleSrcFile?.qMainClassFqName(srcCharset) ?: ""
}

// << Root of the CallChain >>
private fun Path.qMainClassFqName(charset: Charset): String = qCacheIt(pathString) {
    if (!this.qHasKotlinMainFunctionQuickAndDirty(charset))
        return@qCacheIt ""

    val mainFile = this
    val pkgName = mainFile.qGetKotlinPackageNameQuickAndDirty()

    val baseName = mainFile.fileName.toString().substringBeforeLast('.')
    if (pkgName.isNotEmpty()) {
        pkgName + "." + baseName + "Kt"
    } else {
        baseName + "Kt"
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QFinalSrcCreationScope internal constructor(
    val srcBase: QSingleSrcBase,
    val analysisCtx: QAnalysisContext,
) {
    // << Root of the CallChain >>
    fun default(): String {
        return srcBase.toSrcCode()
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QNonTopLevelSrcCodeCreationScope internal constructor(
    val element: KtElement,
    val level: QSrcLevel,
    val indent: String,
    val analysisCtx: QAnalysisContext,
    val srcSetType: QSrcSetType,
) {
    // << Root of the CallChain >>
    fun default(): String {
        val comment = element.qCallChainNode(analysisCtx).comment()

        return if (comment.isNotEmpty()) {
            """${indent}$comment
${indent}${element.text}"""
        } else {
            element.text
        }
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QJitpackYmlScope internal constructor(
    val lib: QCompactLib,
    val version: String,
    val currentJitpackYml: String
) {
    // << Root of the CallChain >>
    fun default(): String {
        return QMyCompactLib.jitpack_yml(this)
    }
}

// << Root of the CallChain >>
data class QCodeExample(
    val functionName: String,
    val functionContent: String
) {
    // << Root of the CallChain >>
    val isMainFunction: Boolean
        get() = functionName == "main"
}

// << Root of the CallChain >>
@QCompactLibDsl
class QReadmeScope internal constructor(
    val lib: QCompactLib,
    val libName: String,
    val latestReleaseVersion: String,
    val nextVersion: String,
    val currentReadme: String,
    val destSplitMainSrcDirPath: String,
    val destSingleMainSrcFilePath: String,
    val destSingleTestSrcFilePath: String,
    val destSplitTestSrcFilePath: String,
    val destExampleSrcFilePath: String,
    val publicAPIs: List<QTopLevelCompactElement>,
    val exampleMainFunction: QFuncSrc?,
    val exampleNonMainFunctions: List<QFuncSrc>,
    val mainDependencyLines: List<String>,
    val testDependencyLines: List<String>,
) {
    // << Root of the CallChain >>
    fun default(): String {
        return """
            ${defaultTitleSection().qNiceIndent(3)}
            
            ${defaultExampleSection().qNiceIndent(3)}
            
            ${defaultPublicAPISection().qNiceIndent(3)}
            
            ${defaultDependenciesSection().qNiceIndent(3)}
            
            ${defaultMavenSection().qNiceIndent(3)}
    
            ${defaultAboutSection().qNiceIndent(3)}
            
            ${defaultReferenceSection().qNiceIndent(3)}
        """.trimIndent()
    }

    // << Root of the CallChain >>
    fun defaultTitleSection(): String {
        return QMyCompactLib.titleSection(this)
    }

    // << Root of the CallChain >>
    fun defaultMavenSection(): String {
        return QMyCompactLib.mavenSection(this)
    }

    // << Root of the CallChain >>
    fun defaultDependenciesSection(): String {
        return QMyCompactLib.dependenciesSection(this)
    }

    // << Root of the CallChain >>
    fun defaultAboutSection(): String {
        return QMyCompactLib.aboutSection(this)
    }

    // << Root of the CallChain >>
    fun defaultPublicAPISection(): String {
        return QMyCompactLib.publicAPISection(this)
    }

    // << Root of the CallChain >>
    fun defaultExampleSection(): String {
        return QMyCompactLib.exampleSection(this)
    }

    // << Root of the CallChain >>
    fun defaultReferenceSection(): String {
        return QMyCompactLib.referenceSection(this)
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QBuildGradleKtsScope internal constructor(
    val currentBuildGradleKts: String,
    val version: String,
    val mainCtx: QAnalysisContext,
    val testCtx: QAnalysisContext?,
    val mainDependencyLines: List<String>,
    val testDependencyLines: List<String>,
    val mainSingleMainClass: String,
    val mainSplitMainClass: String,
    val testSingleMainClass: String,
    val testSplitMainClass: String,
    val exampleMainClass: String,
) {
    // << Root of the CallChain >>
    val lib
        get() = mainCtx.lib

    // << Root of the CallChain >>
    fun default(): String {
        return QMyCompactLibGradle.build_gradle_kts(this)
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QSplitImportFilterScope internal constructor(
    val importPath: String,
    val ktFile: KtFile,
    val fileName: String,
    val lib: QCompactLib,
    val analysisCtx: QAnalysisContext,
    val srtSet: QSrcSetType,
) {
    // << Root of the CallChain >>
    fun default(): Boolean {
        return analysisCtx.splitFileImportCandidates(ktFile).any { candidate ->
            if (candidate == importPath)
                return@any true
            if (!lib.strictImportCheck) {
                return@any importPath.endsWith(".$candidate") || importPath.endsWith(".*")
            }

            false
        }
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QSingleImportFilterScope internal constructor(
    val importPath: String,
    val lib: QCompactLib,
    val analysisCtx: QAnalysisContext,
    val srtSet: QSrcSetType,
) {
    // << Root of the CallChain >>
    fun default(): Boolean {
        return analysisCtx.singleFileImportCandidates().any { candidate ->
            if (candidate == importPath)
                return@any true
            if (candidate == "or" || candidate == "and" || candidate == "")
                if (!lib.strictImportCheck) {
                    return@any importPath.endsWith(".$candidate") || importPath.endsWith(".*")
                }

            false
        }
    }
}

// << Root of the CallChain >>
class QCompactLibIfFileExists internal constructor(
    val ifExistsReadeMeFile: QIfExistsWrite,
    val ifExistsBuildGradleKtsFile: QIfExistsWrite,
    val ifExistsSettingsGradleKtsFile: QIfExistsWrite,
    val ifExistsGitIgnoreFile: QIfExistsWrite,
    val ifExistsLicenseFile: QIfExistsWrite,
    val ifExistsJitpackYmlFile: QIfExistsWrite
)

// << Root of the CallChain >>
@QCompactLibDsl
class QCompactLibIfFileExistsScope internal constructor() {
    // << Root of the CallChain >>
    var ifExistsReadMeFile: QIfExistsWrite = QIfExistsWrite.BackupAndOverwriteAtomic
    // << Root of the CallChain >>
    var ifExistsBuildGradleKtsFile: QIfExistsWrite = QIfExistsWrite.OverwriteAtomic
    // << Root of the CallChain >>
    var ifExistsSettingsGradleKtsFile: QIfExistsWrite = QIfExistsWrite.OverwriteAtomic
    // << Root of the CallChain >>
    var ifExistsGitIgnoreFile: QIfExistsWrite = QIfExistsWrite.OverwriteAtomic
    // << Root of the CallChain >>
    var ifExistsLicenseFile: QIfExistsWrite = QIfExistsWrite.DoNothing
    // << Root of the CallChain >>
    var ifExistsJitpackYmlFile: QIfExistsWrite = QIfExistsWrite.DoNothing
    // << Root of the CallChain >>
    private fun build(): QCompactLibIfFileExists {
        return QCompactLibIfFileExists(
            ifExistsReadeMeFile = ifExistsReadMeFile,
            ifExistsBuildGradleKtsFile = ifExistsBuildGradleKtsFile,
            ifExistsSettingsGradleKtsFile = ifExistsSettingsGradleKtsFile,
            ifExistsGitIgnoreFile = ifExistsGitIgnoreFile,
            ifExistsLicenseFile = ifExistsLicenseFile,
            ifExistsJitpackYmlFile = ifExistsJitpackYmlFile
        )
    }
}

// << Root of the CallChain >>
private fun QCompactLib.clearExistingSrcFiles() {
    arrayOf(destSplitMainDir, destSplitTestDir, destSingleMainDir, destSingleTestDir, destExampleDir).forEach { dir ->
        if (dir != destProjDir && dir.qIsSubDirOrFileOf(destProjDir)) {
            dir.qDeleteDirContents()
        }
    }
}

// << Root of the CallChain >>
class QCompactLibStat(
    val statFile: Path,
    val mainPublicAPIs: List<QTopLevelCompactElement>,
    val mainNumberOfMarkedNodes: Int,
    val testNumberOfMarkedNodes: Int,
    val mainNumberOfAllNodes: Int,
    val testNumberOfAllNodes: Int,
    val mainNumberOfRootOfChainNodes: Int,
    val testNumberOfRootOfChainNodes: Int,
    val mainSingleSrcFileNumberOfLines: Int,
    val testSingleSrcFileNumberOfLines: Int,
    val mainSplitSrcNumberOfFiles: Int,
    val testSplitSrcNumberOfFiles: Int
) {
    // << Root of the CallChain >>
    private fun apiToString(onlyRootOfChain: Boolean = false): String {
        return mainPublicAPIs.asSequence().filter {
            !onlyRootOfChain || it.isChainRoot()
        }.sortedBy {
            it.lineNumber
        }.sortedBy {
            it.type.ordinal
        }.sortedByDescending {
            it.isChainRoot()
        }.joinToString("\n") {
            val chain = if (it.isChainRoot()) {
                ""
            } else {
                " (Chained)"
            }

            "${it.simpleName}$chain - ${it.type.name}"
        }
    }

    // << Root of the CallChain >>
    override fun toString(): String {
        return """
            # Public API [main]
            ${apiToString().qNiceIndent(3)}
            
            # single src file number of lines [main]
            $mainSingleSrcFileNumberOfLines
            # split src file number of files [main]
            $mainSplitSrcNumberOfFiles
            # number of marked nodes [main]
            $mainNumberOfMarkedNodes
            # number of all nodes [main]
            $mainNumberOfAllNodes
            # number of root of chain nodes [main]
            $mainNumberOfRootOfChainNodes
            # single src file number of lines [test]
            $testSingleSrcFileNumberOfLines
            # split src file number of files [test]
            $testSplitSrcNumberOfFiles
            # number of marked nodes [test]
            $testNumberOfMarkedNodes
            # number of all nodes [test]
            $testNumberOfAllNodes
            # number of root of chain nodes [test]
            $testNumberOfRootOfChainNodes
        """.trimIndent()
    }
}

// << Root of the CallChain >>
private suspend fun QCompactLib.createLibrary(): QCompactLibResult {
    this.phase.nextPhase(QLibPhase.MainSrcAnalysis)

    val mainCtx = QAnalysisContext(this, false, emptySet())

    val licenseScope = QLicenseScope(this, Calendar.getInstance().get(Calendar.YEAR), author)

    val mainSingleSrcBase = qCreateSingleSrcBase(mainCtx, licenseScope)

    mainCtx.singleSrcBase = mainSingleSrcBase

    val mainDependencies = mainSingleSrcBase.dependencies

    val testDependencies = mutableListOf<QDep>()

    var destTestSingleFile: Path? = null

    var testCtx: QAnalysisContext? = null

    this.phase.nextPhase(QLibPhase.CleanExistingSrc)

    this.clearExistingSrcFiles()

    this.phase.nextPhase(QLibPhase.ReadExampleSrc)

    val exampleFunctions = exampleSrcFile.qReadCodeExamples(srcCharset)

    if (this.exampleSrcFile?.exists() == true) {
        exampleSrcFile.qCopyFileIntoDir(
            this.destExampleDir,
            ifExists = QIfExistsCopyFile.Overwrite
        )
    }

    this.phase.nextPhase(QLibPhase.CreateTestSrcAnalysisAndSrc)

    if (hasTest) {
        testCtx = QAnalysisContext(this, true, emptySet())

        val testBase = qCreateSingleSrcBase(testCtx, licenseScope)

        testCtx.singleSrcBase = testBase

        testDependencies += testBase.dependencies.filterNot { testDep ->
            mainDependencies.any { srcDep ->
                srcDep.name == testDep.name
            }
        }

        testCtx.phase.nextPhase(QAnalysisPhase.CreateSingleSrc)

        destTestSingleFile = if (createSingleFileSrc) {
            val destTestFileSrc = this.srcCode.finalSrc(QFinalSrcCreationScope(testBase, testCtx))

            val destFile = this.destSingleTestDir.resolve(this.destTestSrcFileName)

            destFile.qWrite(
                text = destTestFileSrc,
                ifExists = QIfExistsWrite.OverwriteAtomic,
                createParentDirs = true,
                charset = this.srcCharset
            )
        } else {
            null
        }

        testCtx.phase.nextPhase(QAnalysisPhase.CreateSplitSrc)

        qCreateSplitSrcFiles(testCtx)
    }

    this.phase.nextPhase(QLibPhase.CreateMainSrc)

    mainCtx.phase.nextPhase(QAnalysisPhase.CreateSingleSrc)

    val destMainSingleFile = if (createSingleFileSrc) {
        val destMainSingleSrc = srcCode.finalSrc(QFinalSrcCreationScope(mainSingleSrcBase, mainCtx))

        val destFile = this.destSingleMainDir.resolve(this.destMainSrcFileName)

        destFile.qWrite(
            text = destMainSingleSrc,
            ifExists = QIfExistsWrite.OverwriteAtomic,
            createParentDirs = true,
            charset = this.srcCharset
        )
    } else {
        null
    }

    mainCtx.phase.nextPhase(QAnalysisPhase.CreateSplitSrc)

    qCreateSplitSrcFiles(mainCtx)

    this.phase.nextPhase(QLibPhase.CheckSrcHash)

    val newHash = this.destSplitMainDir.qHash(QHash.SHA_512, useModifiedDateForDir = false)

    val isInitialBuild = currentVersion == QVersion.NONE

    val versionUpdateRequired = when (this.versionUpdateStrategy) {
        QVersionUpdateStrategy.UpdateOnlyIfSplitSrcHashChanged -> {
            if (!this.currentVersionJarFile.exists() || !this.currentVersionJarFile.exists()) {
                true
            } else {
                isInitialBuild || newHash != currentVersion.srcHash
            }
        }

        QVersionUpdateStrategy.ForceUpdate -> {
            true
        }

        QVersionUpdateStrategy.NoUpdate -> {
            isInitialBuild
        }
    }

    versionUpdateRequired.log

    val latestRelease = this.getGitHubLatestReleaseVersion()

    val version = if (versionUpdateRequired) QNextVersionScope(
        currentLocalVersion = currentVersion.version,
        latestReleaseVersion = latestRelease,
        versionSuffix,
        this
    ).nextVersion() else currentVersion.version

    this.phase.nextPhase(QLibPhase.CreateBuildGradle)

    val buildGradleKtsFile = this.createBuildGradleKts(version, mainDependencies, testDependencies, mainCtx, testCtx)

    val settingsGradleKtsSrc = "rootProject.name = \"${libName}\"\n"
    val settingsGradleKtsFile = destProjDir.resolve("settings.gradle.kts")
    settingsGradleKtsFile.qWrite(settingsGradleKtsSrc, ifExists = ifFileExists.ifExistsSettingsGradleKtsFile)

    this.phase.nextPhase(QLibPhase.GradleBuild)

    this.gradleWrapper()

    var buildSuccess = this.gradleBuild()
    this.enableSpotless
    if (buildSuccess && this.enableSpotless) {
        this.gradleSpotlessApply()
        buildSuccess = this.gradleBuild()
    }

    this.phase.nextPhase(QLibPhase.CreateGitIgnore)

    val gitIgnoreFile = destProjDir.resolve(".gitignore")
    gitIgnoreFile.qWrite(gitIgnore, ifExists = ifFileExists.ifExistsGitIgnoreFile)

    this.phase.nextPhase(QLibPhase.CreateLicense)

    val licenseFile = destProjDir.resolve("LICENSE")

    licenseFile.qWrite(license(licenseScope), ifExists = ifFileExists.ifExistsLicenseFile)

    this.phase.nextPhase(QLibPhase.CreateVersionFile)

    val jar = destArtifactsDir.resolve(destJarFileName(version))
    val srcJar = destArtifactsDir.resolve(destSourcesJarFileName(version))
    val artifacts = listOf(jar, srcJar).joinToString(";") { destProjDir.qRelativeTo(it).slash }

    val versionFile = destProjDir.resolve("VERSION")
    versionFile.qWrite(version + "\n" + newHash + "\n" + artifacts, ifExists = QIfExistsWrite.OverwriteAtomic)

    this.phase.nextPhase(QLibPhase.CreateJitpackYmlFile)

    val jitpackYmlFile = destProjDir.resolve("jitpack.yml")

    val curJitpackYml = if (jitpackYmlFile.exists()) {
        jitpackYmlFile.qReadFile()
    } else {
        ""
    }

    val jitpackYmlSrc = jitpackYml(
        QJitpackYmlScope(
            lib = this,
            version = version,
            currentJitpackYml = curJitpackYml
        )
    )

    jitpackYmlFile.qWrite(jitpackYmlSrc, ifExists = ifFileExists.ifExistsJitpackYmlFile)

    phase.nextPhase(QLibPhase.CreateStat)

    val stat = createStat(
        version = version,
        mainCtx = mainCtx,
        testCtx = testCtx,
        destMainSingleFile = destMainSingleFile,
        destTestSingleFile = destTestSingleFile,
        mainSingleSrcBase = mainSingleSrcBase
    )

    this.phase.nextPhase(QLibPhase.CreateReadme)

    val readMeFile = createReadme(latestRelease, version, mainCtx, testCtx, exampleFunctions)

    this.phase.nextPhase(QLibPhase.Result)

    return QCompactLibResult(
        lib = this,
        stat = stat,
        latestReleaseVersion = latestRelease,
        version = version,
        oldHash = newHash,
        newHash = currentVersion.srcHash,
        isVersionUpdated = versionUpdateRequired,
        mainContext = mainCtx,
        testContext = testCtx,
        singleMainSrcFile = destMainSingleFile,
        singleTestSrcFile = destTestSingleFile,
        readMeFile = readMeFile,
        buildGradleKtsFile = buildGradleKtsFile,
        settingsGradleKtsFile = settingsGradleKtsFile,
        gitIgnoreFile = gitIgnoreFile,
        buildSuccess = buildSuccess,
    )
}

// << Root of the CallChain >>
private fun QCompactLib.createReadme(
    latestReleaseVersion: String,
    nextVersion: String,
    mainCtx: QAnalysisContext,
    testCtx: QAnalysisContext?,
    exampleFunctions: List<QFuncSrc>,
): Path {
    val readMeFile = destProjDir.resolve("README.md")

    val curReadMeContent = if (readMeFile.exists()) {
        readMeFile.qReadFile()
    } else {
        ""
    }

    val exampleMainFunc: QFuncSrc? = exampleFunctions.find {
        it.name == "main"
    }

    val exampleNonMainFunctions: List<QFuncSrc> = exampleFunctions.filter {
        it.name != "main"
    }

    val readMeSrc =
        "<!--- version = $nextVersion --->\n\n" + readme(
            QReadmeScope(
                lib = this,
                libName = libName,
                latestReleaseVersion = latestReleaseVersion.ifEmpty { nextVersion },
                nextVersion = nextVersion,
                currentReadme = curReadMeContent,
                destSplitMainSrcDirPath = this.destMainSplitSrcDirPathFromProjDir().slash,
                destSingleMainSrcFilePath = this.destMainSingleSrcFilePathFromProjDir().slash,
                destSingleTestSrcFilePath = this.destTestSingleSrcFilePathFromProjDir().slash,
                destSplitTestSrcFilePath = this.destTestSplitSrcFilePathFromProjDir().slash,
                destExampleSrcFilePath = this.destExampleSrcFilePathFromProjDir().slash,
                mainDependencyLines = mainCtx.dependencyLines,
                testDependencyLines = testCtx?.dependencyLines ?: emptyList(),
                exampleMainFunction = exampleMainFunc,
                exampleNonMainFunctions = exampleNonMainFunctions,
                publicAPIs = mainCtx.publicTopLevelCompactElements.filter { it.isChainRoot() }
            )
        )

    readMeFile.qWrite(readMeSrc, ifExists = ifFileExists.ifExistsReadeMeFile)

    return readMeFile
}

// << Root of the CallChain >>
fun QCompactLib.createStat(
    version: String,
    mainCtx: QAnalysisContext,
    testCtx: QAnalysisContext?,
    destMainSingleFile: Path?,
    destTestSingleFile: Path?,
    mainSingleSrcBase: QSingleSrcBase
): QCompactLibStat {

    val statFile = destProjDir.resolve("stat.txt")

    val chainNodeCount = """
        # chain node hit count [main]
${mainCtx.nodeHitCount(10).qNiceIndent(2, false)}

        ${
        if (testCtx != null && testCtx.nodeHitCount.isNotEmpty()) {
            """
                # chain node hit count [test]
${testCtx.nodeHitCount(10).qNiceIndent(4, false)}
                """.qNiceIndent(2)
        } else {
            ""
        }
    }""".trimIndent()

    val mainNumberOfRootOfChainNodes = mainCtx.rootOfChainNode.size
    val mainNumberOfMarkedNodes = mainCtx.markedNodes.size
    val mainNumberOfAllNodes = mainCtx.callChainNodes.size
    val mainSingleSrcFileNumberOfLines = destMainSingleFile?.qNumberOfLines() ?: -1
    val mainSplitSrcNumberOfFiles = mainCtx.splitFiles.size
    val testNumberOfRootOfChainNodes = testCtx?.rootOfChainNode?.size ?: -1
    val testNumberOfMarkedNodes = testCtx?.markedNodes?.size ?: -1
    val testNumberOfAllNodes = testCtx?.callChainNodes?.size ?: -1
    val testSingleSrcFileNumberOfLines = destTestSingleFile?.qNumberOfLines() ?: -1
    val testSplitSrcNumberOfFiles = testCtx?.splitFiles?.size ?: -1

    val stat = QCompactLibStat(
        statFile = statFile,
        mainPublicAPIs = mainCtx.publicTopLevelCompactElements,
        mainNumberOfRootOfChainNodes = mainNumberOfRootOfChainNodes,
        mainNumberOfMarkedNodes = mainNumberOfMarkedNodes,
        mainSingleSrcFileNumberOfLines = mainSingleSrcFileNumberOfLines,
        mainSplitSrcNumberOfFiles = mainSplitSrcNumberOfFiles,
        mainNumberOfAllNodes = mainNumberOfAllNodes,
        testNumberOfRootOfChainNodes = testNumberOfRootOfChainNodes,
        testNumberOfMarkedNodes = testNumberOfMarkedNodes,
        testSingleSrcFileNumberOfLines = testSingleSrcFileNumberOfLines,
        testSplitSrcNumberOfFiles = testSplitSrcNumberOfFiles,
        testNumberOfAllNodes = testNumberOfAllNodes,
    )

    mainNumberOfMarkedNodes.logGreen
    mainSingleSrcFileNumberOfLines.logGreen

    statFile.qWrite(
        "Version: $version\n\n" + stat.toString() + "\n\n" + chainNodeCount.noStyle,
        ifExists = QIfExistsWrite.OverwriteAtomic
    )

    return stat
}

// << Root of the CallChain >>
private fun Path?.qReadCodeExamples(charset: Charset): List<QFuncSrc> {
    if (this == null)
        return emptyList()

    val fileContent = this.qReadFile(charset)

    return fileContent.qGetTopLevelFunctionsQuickAndDirty()
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.runCommand(cmd: String, quiet: Boolean = false): QRunResult {
    return cmd.qRunInShell(pwd = destProjDir, quiet = quiet)
}

// << Root of the CallChain >>
private fun QCompactLib.createBuildGradleKts(
    version: String,
    mainDependencies: List<QDep>,
    testDependencies: List<QDep>,
    mainCtx: QAnalysisContext,
    testCtx: QAnalysisContext?
): Path {
    if (mainDependencies.isNotEmpty()) {
        val dep = mainDependencies.filter {
            it.rawSrcLine != null && !it.isKotlinStdLib
        }.map { it.rawSrcLine }.distinct().joinToString("\n")

        mainCtx.dependencyLines += dep
    }

    if (testDependencies.isNotEmpty()) {
        val dep = testDependencies.filter {
            it.rawSrcLine != null && !it.isKotlinStdLib
        }.mapNotNull { it.rawSrcLine }.distinct().joinToString("\n")

        testCtx!!.dependencyLines += dep
    }

    val buildGradleKtsFile = destProjDir.resolve("build.gradle.kts")

    val gradleScope = QBuildGradleKtsScope(
        currentBuildGradleKts = if (buildGradleKtsFile.exists()) buildGradleKtsFile.readText(srcCharset) else "",
        version = version,
        mainCtx = mainCtx,
        testCtx = testCtx,
        mainDependencyLines = mainCtx.dependencyLines,
        testDependencyLines = testCtx?.dependencyLines ?: emptyList(),
        mainSingleMainClass = mainCtx.mainClassFqName().substringAfterLast('.'),
        testSingleMainClass = testCtx?.mainClassFqName()?.substringAfterLast('.') ?: "",
        mainSplitMainClass = mainCtx.mainClassFqName(),
        testSplitMainClass = testCtx?.mainClassFqName() ?: "",
        exampleMainClass = this.exampleMainClassFqName(),
    )
    val gradleSrc = this.buildGradleKts(gradleScope)

    buildGradleKtsFile.qWrite(gradleSrc, ifExists = this.ifFileExists.ifExistsBuildGradleKtsFile)

    return buildGradleKtsFile
}

// << Root of the CallChain >>
private fun qCreateSplitSrcFiles(ctx: QAnalysisContext) {
    for (file in ctx.splitFiles.values) {
        file.destPath.qWrite(
            file.toSrcCode()
        )
    }
}

// << Root of the CallChain >>
suspend fun qCompactLib(
    libName: String,
    author: String,
    block: QCompactLibScope.() -> Unit
): QCompactLibResult {
    return qTimeIt("Create Compact Library : $libName") {
        val builder = QCompactLibScope(
            libName = libName,
            author = author
        )

        builder.block()

        val lib = builder.qInvokeBuild() as QCompactLib

        val result = lib.createLibrary()

        lib.phase.nextPhase(QLibPhase.End)

        result
    }.result
}

// << Root of the CallChain >>
private fun qRegisterRootOfChain(analysisCtx: QAnalysisContext): List<QTopLevelCompactElement> {
    val lib = analysisCtx.lib

    // Register root nodes

    val topLevelFilter = if (!analysisCtx.isTest) {
        lib.rootOfChain.mainTopLevel
    } else {
        lib.rootOfChain.testTopLevel
    }

    val nonTopLevelFilter = if (!analysisCtx.isTest) {
        lib.rootOfChain.mainNonTopLevel
    } else {
        lib.rootOfChain.testNonTopLevel
    }

    val rootElements = analysisCtx.ktFiles.flatMap {
        it.qAllTopLevelElements(true)
    }.filter {
        topLevelFilter(
            QRootOfChainTopLevelScope(
                it,
                it.containingKtFile.virtualFilePath,
                it.containingKtFile.name,
                lib
            )
        )
    }.map {
        it.qContainingTopLevelCompactElement(analysisCtx)
    }

    for (root in rootElements) {
        // force register top level node
        root.topLevelNode.registerAsRootOfChainNode()

        if (root.isTopLevelOnly)
            continue

        for (node in root.chainNodes) {
            if (node.isTopLevel()) {
                // already registered
                continue
            }

            if (nonTopLevelFilter(QRootOfChainNonTopLevelScope(node, root, lib))) {
                node.registerAsRootOfChainNode()
            }
        }
    }

    val rootKtFiles = rootElements.map {
        it.ktFile
    }.distinct()

    analysisCtx.rootKtFiles = rootKtFiles

    return rootElements
}

// << Root of the CallChain >>
private fun qCreateSingleSrcBase(
    analysisCtx: QAnalysisContext,
    lisenceScope: QLicenseScope
): QSingleSrcBase {
    val srcSet = if (analysisCtx.isTest) QSrcSetType.TestSingle else QSrcSetType.MainSingle

    analysisCtx.phase.nextPhase(QAnalysisPhase.RegisterRootOfChain)

    val rootElements = qRegisterRootOfChain(analysisCtx)

    analysisCtx.doCompilerAnalysis()

    qResolveCallChain(analysisCtx, rootElements)

    analysisCtx.phase.nextPhase(QAnalysisPhase.CallChainAnalysisFinalWork)

    analysisCtx.finishCallChainAnalysis()

    analysisCtx.phase.nextPhase(QAnalysisPhase.CreateSingleSrcBase)

    val lib = analysisCtx.lib

    val topLevelCompactElements = analysisCtx.markedTopLevelCompactElements

    val fileAnnotation = lib.singleFileAnnotation(QSingleFileAnnotationCreationScope(analysisCtx, analysisCtx.isTest))

    val headerComment = lib.srcHeader(lisenceScope)

    val beforeImports = if (headerComment.isNotEmpty() && fileAnnotation.isNotEmpty()) {
        headerComment + "\n\n" + fileAnnotation
    } else if (headerComment.isNotEmpty()) {
        headerComment
    } else {
        fileAnnotation
    }

    val compactElementsFromRootAPIFiles =
        topLevelCompactElements.filter { it.isChainRoot() && it.topLevelElement !is KtFileAnnotationList }

    val compactElementsFromNonRootAPIFiles =
        topLevelCompactElements.filter { !it.isChainRoot() && it.topLevelElement !is KtFileAnnotationList }

    val srcFromRootAPIFiles = compactElementsFromRootAPIFiles.sortedWith(lib.topLevelElementsSorter)
        .joinToString("\n\n") { compactEle ->
            compactEle.toSrcCode(srcSet)
        }

    val srcFromNonRootAPIFiles =
        compactElementsFromNonRootAPIFiles.sortedWith(lib.topLevelElementsSorter)
            .joinToString("\n\n") { compactEle ->
                compactEle.toSrcCode(srcSet)
            }

    val importPathsFromRootAPIFiles = analysisCtx.rootKtFiles.flatMap { it.qImportPathList() }.distinct()

    val importPathsFromNonRootAPIFiles = compactElementsFromNonRootAPIFiles.flatMap { it.ktFile.qImportPathList() }

    val allImports = (importPathsFromRootAPIFiles + importPathsFromNonRootAPIFiles).filter { importPath ->
        lib.singleSrcFileImportFilter(QSingleImportFilterScope(importPath, lib, analysisCtx, srcSet))
    }.distinct().sorted()

    val dependencies = mutableListOf<QDep>()

    for (dep in lib.dependenciesToCheck) {
        for (imp in allImports) {
            if (dep.checkImportPath(imp)) {
                dependencies += dep
                break
            }
        }
    }

    return QSingleSrcBase(
        analysisCtx = analysisCtx,
        destFileName = if (!analysisCtx.isTest) lib.destMainSrcFileName else lib.destTestSrcFileName,
        srcBeforeImports = beforeImports,
        srcBetweenImportsAndFirstElement = if (!analysisCtx.isTest) lib.betweenImportsAndFirstElement else lib.betweenImportsAndFirstElementTest,
        importPaths = allImports,
        srcFromRootAPIFiles = srcFromRootAPIFiles.trim(),
        srcFromNonRootAPIFiles = srcFromNonRootAPIFiles.trim(),
        topLevelElements = topLevelCompactElements,
        dependencies = dependencies,
        isTest = analysisCtx.isTest,
        lineSeparator = "\n"
    )
}

// << Root of the CallChain >>
data class QSingleSrcBase internal constructor(
    val analysisCtx: QAnalysisContext,
    val destFileName: String,
    val srcBeforeImports: String,
    val srcBetweenImportsAndFirstElement: String,
    val importPaths: List<String>,
    val srcFromRootAPIFiles: String,
    val srcFromNonRootAPIFiles: String,
    val topLevelElements: List<QTopLevelCompactElement>,
    val dependencies: List<QDep>,
    val isTest: Boolean,
    val lineSeparator: String,
) {
    // << Root of the CallChain >>
    fun toSrcCode(): String {
        val imports = importPaths.joinToString(lineSeparator) { "import $it" }

        var src = if (srcBeforeImports.trim().isNotEmpty()) {
            srcBeforeImports + "\n\n"
        } else {
            ""
        }

        if (imports.isNotEmpty()) {
            src += imports.qWithNewLineSuffix(1, QOnlyIfStr.NotEmpty) + "\n\n"
        }

        if (srcBetweenImportsAndFirstElement.isNotEmpty()) {
            src += srcBetweenImportsAndFirstElement + "\n\n"
        }

        src += srcFromRootAPIFiles.qWithNewLineSuffix(2, QOnlyIfStr.Always)

        if (srcFromNonRootAPIFiles.isNotBlank()) {
            src += """
// region Src from Non-Root API Files -- Auto Generated by $qThisFilePackageName.$qThisFileName
// ================================================================================
${srcFromNonRootAPIFiles.qWithNewLineSurround(2, QOnlyIfStr.Always)}
// ================================================================================
// endregion Src from Non-Root API Files -- Auto Generated by $qThisFilePackageName.$qThisFileName"""
        }

        return src
    }
}

// << Root of the CallChain >>
fun KtFile.qSrcBeforeImports(br: String = "\n"): String {
    val importStartOffset = this.qImportOffsetRange().first

    return this.children.filter { ele ->
        if (ele.text.isBlank()) return@filter false

        if (ele is KtElement) {
            if (ele is KtImportDirective) return@filter false
        }

        ele.endOffset < importStartOffset
    }.joinToString(br + br) { it.text }
}

// << Root of the CallChain >>
fun KtFile.qAllTopLevelElements(excludePackageAndImportDirective: Boolean = true): List<KtElement> {
    return this.getChildrenOfType<KtElement>().filter {
        it.isTopLevelKtOrJavaMember() && (!excludePackageAndImportDirective || (it !is KtImportDirective && it !is KtPackageDirective))
    }
}

// << Root of the CallChain >>
fun KtFile.qImportPathList(): List<String> {
    return importDirectives.map { it.text.replaceFirst("""import +(\S+)""".re, "$1") }
}

// << Root of the CallChain >>
fun KtFile.qImportOffsetRange(): IntRange {
    val firstImport = this.importDirectives.firstOrNull()
    val lastImport = this.importDirectives.lastOrNull()

    if (firstImport != null) {
        return IntRange(firstImport.startOffset, lastImport!!.endOffset)
    }

    val firstDecl = this.declarations.firstOrNull()

    if (firstDecl != null) {
        val offset = firstDecl.prevSibling.textRange.endOffset + 1
        return IntRange(offset, offset)
    }

    QE.ImportOffsetFail.throwItFile(this.virtualFilePath.path)
}

// << Root of the CallChain >>
private fun qResolveCallChain(
    analysisCtx: QAnalysisContext,
    rootElements: List<QTopLevelCompactElement>
) {
    analysisCtx.phase.nextPhase(QAnalysisPhase.CallChainResolvingStarted)

    val stack = mutableListOf<QChainNode>()

    stack += rootElements.flatMap {
        it.chainNodes
    }.filter {
        it.isRootOfChain()
    }

    while (stack.isNotEmpty()) {
        val node = stack.removeAt(stack.size - 1) // stack.pop()

        if (node.topLevel is KtPackageDirective || node.topLevel is KtImportDirective)
            continue

        node.markThis()

        val nextNodes = node.nodesChainTo().filterNot { toNode ->
            analysisCtx.markedNodes.containsKey(toNode.key)
        }

        stack += nextNodes
    }

    analysisCtx.phase.nextPhase(QAnalysisPhase.CallChainResolvingFinished)
}