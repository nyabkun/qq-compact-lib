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

import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QCompactLibSrcCode(
    val visibilityChange: QVisibilityChangeScope.() -> QKtVisibilityChange,
    val nonTopLevelSrc: QNonTopLevelSrcCodeCreationScope.() -> String,
    val topLevelSrc: QTopLevelSrcCodeScope.() -> String,
    val finalSrc: QFinalSrcCreationScope.() -> String
)

// << Root of the CallChain >>
@QCompactLibDsl
class QCompactLibSrcCodeScope() {
    // << Root of the CallChain >>
    private var visibilityChange: QVisibilityChangeScope.() -> QKtVisibilityChange = {
        default()
    }

    // << Root of the CallChain >>
    private var nonTopLevelSrc: QNonTopLevelSrcCodeCreationScope.() -> String = {
        default()
    }

    // << Root of the CallChain >>
    private var topLevelSrc: QTopLevelSrcCodeScope.() -> String = {
        default()
    }

    // << Root of the CallChain >>
    private var finalSrc: QFinalSrcCreationScope.() -> String = {
        default()
    }

    // << Root of the CallChain >>
    private fun build(): QCompactLibSrcCode {
        return QCompactLibSrcCode(
            visibilityChange = visibilityChange,
            nonTopLevelSrc = nonTopLevelSrc,
            topLevelSrc = topLevelSrc,
            finalSrc = finalSrc
        )
    }

    // << Root of the CallChain >>
    fun visibilityChange(block: QVisibilityChangeScope.() -> QKtVisibilityChange) {
        visibilityChange = block
    }

    // << Root of the CallChain >>
    fun nonTopLevelSrc(block: QNonTopLevelSrcCodeCreationScope.() -> String) {
        nonTopLevelSrc = block
    }

    // << Root of the CallChain >>
    fun topLevelSrc(block: QTopLevelSrcCodeScope.() -> String) {
        topLevelSrc = block
    }

    // << Root of the CallChain >>
    fun finalSrc(block: QFinalSrcCreationScope.() -> String) {
        finalSrc = block
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QTopLevelSrcCodeScope(
    val compactSrc: String,
    val element: KtElement,
    val visibilityChange: QKtVisibilityChange,
    val analysisCtx: QAnalysisContext,
    val srcSetType: QSrcSetType,
) {
    // << Root of the CallChain >>
    fun default(): String {
        val comment = element.qCallChainNode(analysisCtx).comment() + "\n"

        return comment + when (visibilityChange) {
            QKtVisibilityChange.NoChange -> compactSrc
            QKtVisibilityChange.ToPrivate -> QSrcCreationUtils.srcCodeWithPrivateModifier(
                element,
                element.qVisibility(),
                compactSrc
            )

            QKtVisibilityChange.ToInternal -> QSrcCreationUtils.srcCodeWithInternalModifier(
                element,
                element.qVisibility(),
                compactSrc
            )
        }
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QVisibilityChangeScope internal constructor(
    val lib: QCompactLib,
    val compactEle: QTopLevelCompactElement,
    val analysisCtx: QAnalysisContext,
    val srcSetType: QSrcSetType,
) {
    // << Root of the CallChain >>
    fun default(): QKtVisibilityChange {
        if (lib.visibilityUnchangedClasses.any { it.qualifiedName == compactEle.fqName }) {
            return QKtVisibilityChange.NoChange
        }

        return if (compactEle.isChainRoot()) {
            QKtVisibilityChange.NoChange
        } else if (!srcSetType.isSingleFileSelfContained) {
            QKtVisibilityChange.ToInternal
        } else {
            QKtVisibilityChange.ToPrivate
        }
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QSingleFileAnnotationCreationScope internal constructor(
    val analysisCtx: QAnalysisContext,
    val isTest: Boolean
) {
    // << Root of the CallChain >>
    fun default(): String {
        return "@file:Suppress(" + analysisCtx.fileAnnotationLists.flatMap { list ->
            list.annotationEntries
        }.joinToString(",") { entry ->
            entry.text
        } + ")"
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QSplitFileAnnotationCreationScope internal constructor(
    val analysisCtx: QAnalysisContext,
    val isTest: Boolean,
    val ktFile: KtFile,
) {
    // << Root of the CallChain >>
    fun default(): String {
        return ktFile.fileAnnotationList?.text ?: ""
    }
}

// << Root of the CallChain >>
enum class QSrcSetType(val isSingleFileSelfContained: Boolean) {
    // << Root of the CallChain >>
    MainSingle(true),
    // << Root of the CallChain >>
    MainSplit(false),
    // << Root of the CallChain >>
    TestSingle(true),
    // << Root of the CallChain >>
    TestSplit(false),
    // << Root of the CallChain >>
    Example(false);
}