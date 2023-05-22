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

import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierType

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
object QSrcCreationUtils {
    // << Root of the CallChain >>
    fun srcCodeWithPrivateModifier(
        topLevelElement: KtElement,
        currentVisibility: QKtVisibility,
        compactSrc: String,
    ): String = when (currentVisibility) {
        QKtVisibility.Private, QKtVisibility.Unknown -> {
            compactSrc
        }
        QKtVisibility.Internal -> {
            val commentEndIdx = topLevelElement.qMaybeFirstTokenIndexSkippingCommentAndAnnotation(compactSrc)
            val srcCommentPart = compactSrc.substring(0, commentEndIdx)
            val srcAfterComment = compactSrc.substring(commentEndIdx)
            srcCommentPart + srcAfterComment.replaceFirst("internal ", "private ")
        }
        QKtVisibility.Protected -> {
            val commentEndIdx = topLevelElement.qMaybeFirstTokenIndexSkippingCommentAndAnnotation(compactSrc)
            val srcCommentPart = compactSrc.substring(0, commentEndIdx)
            val srcAfterComment = compactSrc.substring(commentEndIdx)
            srcCommentPart + srcAfterComment.replaceFirst("protected ", "private ")
        }
        QKtVisibility.Public -> {
            changePublicToNewVisibility(topLevelElement, compactSrc, "private")
        }
    }

    // << Root of the CallChain >>
    fun srcCodeWithInternalModifier(
        topLevelElement: KtElement,
        currentVisibility: QKtVisibility,
        orgSrc: String,
    ): String = when (currentVisibility) {
        QKtVisibility.Private, QKtVisibility.Unknown -> {
            orgSrc
        }
        QKtVisibility.Internal -> {
            orgSrc
        }
        QKtVisibility.Protected -> {
            val commentEndIdx = topLevelElement.qMaybeFirstTokenIndexSkippingCommentAndAnnotation(orgSrc)
            val srcCommentPart = orgSrc.substring(0, commentEndIdx)
            val srcAfterComment = orgSrc.substring(commentEndIdx)

            srcCommentPart + srcAfterComment.replaceFirst("protected ", "internal ")
        }
        QKtVisibility.Public -> {
            changePublicToNewVisibility(topLevelElement, orgSrc, "internal")
        }
    }

    // << Root of the CallChain >>
    fun changePublicToNewVisibility(
        topLevelElement: KtElement,
        modifiedSrc: String,
        newVisibility: String,
    ): String {
        val funcOrClassIdx = if (topLevelElement is KtModifierListOwner) {
            topLevelElement.qMaybeFirstTokenIndexSkippingCommentAndAnnotation(modifiedSrc)
        } else {
            topLevelElement.children.filterIsInstance<KtModifierListOwner>().first().startOffsetInParent
        }

        val srcCommentPart = modifiedSrc.substring(0, funcOrClassIdx)
        val srcAfterComment = modifiedSrc.substring(funcOrClassIdx)

        return if (topLevelElement !is KtDeclaration || topLevelElement.visibilityModifierType() == null) {
            "$srcCommentPart$newVisibility $srcAfterComment"
        } else {
            srcCommentPart + srcAfterComment.replaceFirst("public ", "$newVisibility ")
        }
    }
}