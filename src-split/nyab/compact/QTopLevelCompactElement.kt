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

import java.nio.file.Path
import nyab.util.qCacheIt
import nyab.util.qCamelCaseToSpaceSeparated
import nyab.util.qLineNumberAt
import nyab.util.qLineSeparator
import nyab.util.qReadFile
import nyab.util.slash
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclarationContainer
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.isObjectLiteral
import org.jetbrains.kotlin.psi.psiUtil.isTopLevelKtOrJavaMember

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QTopLevelCompactElement(
    val chainNodes: List<QChainNode>,
    val analysisCtx: QAnalysisContext,
) {
    // << Root of the CallChain >>
    val lib: QCompactLib = analysisCtx.lib

    // << Root of the CallChain >>
    fun isMarked(): Boolean = chainNodes.any { it.isMarked() }

    // << Root of the CallChain >>
    val topLevelElement: KtElement = chainNodes[0].topLevel

    // << Root of the CallChain >>
    val topLevelNode: QChainNode = chainNodes[0].topLevel.qCallChainNode(analysisCtx)

    // << Root of the CallChain >>
    val secondLevelElements: List<KtElement> = chainNodes.mapNotNull {
        it.secondLevel
    }

    // << Root of the CallChain >>
    val thirdLevelElements: List<KtElement> = chainNodes.mapNotNull {
        it.thirdLevel
    }

    // << Root of the CallChain >>
    fun minChainLength() = chainNodes.filter { it.isMarked() }.minOf {
        it.chainNodesFromRoot().size
    }

    // << Root of the CallChain >>
    val type: QTopLevelType
        get() {
            return if (topLevelElement is KtNamedFunction) {
                if( topLevelElement.isExtensionDeclaration() ) {
                    QTopLevelType.ExtensionFunction
                } else {
                    QTopLevelType.Function
                }
            } else if (topLevelElement is KtProperty) {
                if( topLevelElement.isExtensionDeclaration() ) {
                    QTopLevelType.ExtensionProperty
                } else {
                    QTopLevelType.Property
                }
            } else if (topLevelElement is KtClass) {
                if (topLevelElement.isEnum()) {
                    QTopLevelType.EnumClass
                } else if (topLevelElement.isAnnotation()) {
                    QTopLevelType.AnnotationClass
                } else if (topLevelElement.isData()) {
                    QTopLevelType.DataClass
                } else if (topLevelElement.isInterface()) {
                    QTopLevelType.Interface
                } else if (topLevelElement.isAbstract()) {
                    QTopLevelType.AbstractClass
                } else if (topLevelElement.isValue()) {
                    QTopLevelType.ValueClass
                } else {
                    QTopLevelType.Class
                }
            } else if (topLevelElement is KtClassOrObject && topLevelElement.isObjectLiteral()) {
                QTopLevelType.ObjectLiteral
            } else {
                QTopLevelType.Other
            }
        }

    // << Root of the CallChain >>
    val ktFile: KtFile = topLevelElement.containingKtFile

    // << Root of the CallChain >>
    val br: String = ktFile.text.qLineSeparator().value

    // << Root of the CallChain >>
    val companionDeclaration: KtObjectDeclaration? by lazy {
        for (ele in secondLevelElements) {
            if (ele is KtObjectDeclaration && ele.isCompanion()) return@lazy ele
        }

        return@lazy null
    }

    // << Root of the CallChain >>
    val companionClassBody: KtDeclarationContainer? =
        companionDeclaration?.getChildrenOfType<KtClassBody>()?.firstOrNull()

    // << Root of the CallChain >>
    val nodesChainedFrom: List<QChainNode> = chainNodes.mapNotNull { it.nodeChainFrom() }.toList()

    // << Root of the CallChain >>
    fun markDownSrcCodeLink(): String = qCacheIt("#mk_link${topLevelElement.qElementKey}") {
        val path = destSplitSrcFileRelativePath().slash
        val lineRange = destSplitSrcFileLineNum().toMarkdownLineRange()

        "(${path}${lineRange})"
    }

    // << Root of the CallChain >>
    private fun IntRange.toMarkdownLineRange(): String {
        return if( this.first == this.last ) {
            "#L$start"
        } else {
            "#L$start-L$last"
        }
    }

    // << Root of the CallChain >>
    fun destSplitSrcFileRelativePath(): Path {
        val dest = analysisCtx.splitFiles[ktFile.virtualFilePath]!!.destPath
        return analysisCtx.lib.destProjDir.relativize(dest)
    }

    // << Root of the CallChain >>
    fun destSplitSrcFileAbsolutePath(): Path {
        return analysisCtx.splitFiles[ktFile.virtualFilePath]!!.destPath
    }

    // << Root of the CallChain >>
    fun destSplitSrcFileLineNum(): IntRange {
        val srcCode = toSrcCode(if (analysisCtx.isTest) QSrcSetType.TestSplit else QSrcSetType.MainSplit)
        val content = destSplitSrcFileAbsolutePath().qReadFile(analysisCtx.lib.srcCharset)
        val idx = content.indexOf(srcCode)
        return IntRange(content.qLineNumberAt(idx), content.qLineNumberAt(idx + srcCode.length))
    }

    // << Root of the CallChain >>
    fun ktElementsInMarkedNodes(): List<KtElement> {
        return this.chainNodes.filter {
            it.isMarked()
        }.flatMap {
            it.ktElementsToResolve()
        }
    }

    // << Root of the CallChain >>
    val rawSrcCode: String = topLevelElement.text

    // << Root of the CallChain >>
    val rawSrcTextOffset: Int = topLevelElement.textOffset

    // << Root of the CallChain >>
    val lineNumber: Int by lazy {
        ktFile.qReadContent().qLineNumberAt(rawSrcTextOffset)
    }

    // << Root of the CallChain >>
    val originalVisibility: QKtVisibility = topLevelElement.qVisibility()

    // << Root of the CallChain >>
    fun visibilityChange(srcSetType: QSrcSetType): QKtVisibilityChange =
        qCacheIt("vc#" + topLevelElement.qElementKey.value + ":" + srcSetType.name) {
            lib.srcCode.visibilityChange(QVisibilityChangeScope(lib, this, analysisCtx, srcSetType))
        }

    // << Root of the CallChain >>
    val isTopLevelOnly: Boolean = chainNodes.size == 1 && chainNodes[0].targetElement.isTopLevelKtOrJavaMember()

    // << Root of the CallChain >>
    fun isChainRoot() = chainNodes.any { it.isRootOfChain() }

    // << Root of the CallChain >>
    val offsetRange: IntRange = IntRange(
        topLevelElement.textRange.startOffset, topLevelElement.textRange.endOffset + 1
    ) // close range

    // << Root of the CallChain >>
    val pkgFqName: String = ktFile.packageFqName.asString()

    // << Root of the CallChain >>
    val simpleName: String = topLevelElement.qSimpleName()

    // << Root of the CallChain >>
    val fqName: String = if (pkgFqName.isNotEmpty()) {
        "$pkgFqName.$simpleName"
    } else {
        simpleName
    }

    // << Root of the CallChain >>
    val filePath: String = ktFile.virtualFilePath

    // << Root of the CallChain >>
    val topLevelType: QDeclarationType = when (topLevelElement) {
        is KtClass -> {
            if (topLevelElement.isEnum()) {
                QDeclarationType.EnumClass
            } else if (topLevelElement.isAnnotation()) {
                QDeclarationType.AnnotationClass
            } else if (topLevelElement.isData()) {
                QDeclarationType.DataClass
            } else {
                QDeclarationType.Class
            }
        }
//        is KtObjectDeclaration -> {
//            QDeclarationType.ObjectLiteral
//            QDeclarationType.CompanionObject
//        }
        is KtProperty -> QDeclarationType.Property
        is KtNamedFunction -> QDeclarationType.Function
        is KtTypeAlias -> QDeclarationType.TypeAlias
        else -> {
//            "QDeclarationType = Unknown : ${topLevelElement::class.simpleName}".logYellow

            QDeclarationType.Unknown
        }
    }

    // << Root of the CallChain >>
    fun className(): String = topLevelElement.qClassName()

    // << Root of the CallChain >>
    override fun toString(): String {
        return topLevelElement.qSimpleName() +
                "\nchainNodes.size = " + chainNodes.size + "\n" +
                chainNodes.joinToString("  ") { it.toString() }
    }

    // << Root of the CallChain >>
    private fun containingImportPath(name: String): String? {
        val importFqNames = ktFile.importList?.imports?.map {
            it.importedFqName.toString()
        }

        val found = importFqNames?.find {
            it == name
        }

        if (found != null)
            return found

        return importFqNames?.find {
            it.endsWith(".$name")
        }
    }

    // << Root of the CallChain >>
    fun finalVisibility(mainOrTest: QSrcSetType): QKtVisibility {
        return when (
            lib.srcCode.visibilityChange(
                QVisibilityChangeScope(lib, this, analysisCtx, mainOrTest)
            )
        ) {
            QKtVisibilityChange.ToPrivate ->
                QKtVisibility.Private

            QKtVisibilityChange.ToInternal ->
                QKtVisibility.Internal

            QKtVisibilityChange.NoChange ->
                originalVisibility
        }
    }
}

// << Root of the CallChain >>
enum class QTopLevelType {
    // << Root of the CallChain >>
    Function,
    // << Root of the CallChain >>
    ExtensionFunction,
    // << Root of the CallChain >>
    Property,
    // << Root of the CallChain >>
    ExtensionProperty,
    // << Root of the CallChain >>
    ObjectLiteral,
    // << Root of the CallChain >>
    Class,
    // << Root of the CallChain >>
    Interface,
    // << Root of the CallChain >>
    AbstractClass,
    // << Root of the CallChain >>
    EnumClass,
    // << Root of the CallChain >>
    DataClass,
    // << Root of the CallChain >>
    AnnotationClass,
    // << Root of the CallChain >>
    ValueClass,
    // << Root of the CallChain >>
    Other;

    // << Root of the CallChain >>
    fun readableName(): String {
        return name.qCamelCaseToSpaceSeparated(toLowerCase = true)
    }
}