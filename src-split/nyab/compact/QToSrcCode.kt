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

import nyab.conf.QE
import nyab.util.qFirstLine
import nyab.util.qLastLine
import nyab.util.qRemoveLastNonLinebreakWhitespaces
import nyab.util.throwIt
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.psiUtil.getNextSiblingIgnoringWhitespace

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
fun QTopLevelCompactElement.toSrcCode(srcSetType: QSrcSetType): String {
    if (!this.isMarked()) {
        return "// " + topLevelElement.text.qFirstLine()
    }

    val visibilityChange = this.visibilityChange(srcSetType)

    if (!topLevelElement.qHasChildCallChainNode(QSrcLevel.Top)) {
        return lib.srcCode.topLevelSrc(
            QTopLevelSrcCodeScope(
                compactSrc = topLevelElement.text,
                element = topLevelElement,
                visibilityChange = visibilityChange,
                analysisCtx = analysisCtx,
                srcSetType = srcSetType
            )
        )
    }

    val sb = StringBuilder()
    var skipWhiteSpaceSecondLevel = false
    var previousSecondLevelWhitespaceSkipped = false

    var lastSecondLevelEnumEntryIndex = -1
    var firstSecondLevelNonEnumEntryIndex = -1
    var previousEle: PsiElement? = null
    var secondLevelIndent: String? = null
    var thirdLevelIndent: String? = null

    for (topLevelChild in topLevelElement.allChildren) {
        if (topLevelChild !is KtClassBody) {
            sb.append(topLevelChild.text)

            previousEle = topLevelChild
            continue
        }

        for (secondLevel in topLevelChild.allChildren) {
            if (skipWhiteSpaceSecondLevel && secondLevel is PsiWhiteSpace) {
                previousSecondLevelWhitespaceSkipped = true
                continue
            }

            if (secondLevel !is KtElement) {
                if (previousSecondLevelWhitespaceSkipped && (secondLevel.text == "}" || secondLevel.text == "{")) {
                    // { or } should not be commented out
                    sb.append("\n" + secondLevel.text)
                } else {
                    sb.append(secondLevel.text)
                }

                previousSecondLevelWhitespaceSkipped = false

                previousEle = secondLevel
                continue
            }

            previousSecondLevelWhitespaceSkipped = false

            if (secondLevelIndent == null) {
                secondLevelIndent = secondLevel.qIndentPart()
            }

            if (secondLevel.qSkipWriting(analysisCtx)) {
                // skip this declaration
                val next = secondLevel.getNextSiblingIgnoringWhitespace()
                skipWhiteSpaceSecondLevel = next != null

                previousEle = secondLevel
                continue
            }

            skipWhiteSpaceSecondLevel = false

            if (secondLevel.qHasChildCallChainNode(QSrcLevel.Second)) {
                if (topLevelType == QDeclarationType.EnumClass && firstSecondLevelNonEnumEntryIndex < 0) {
                    firstSecondLevelNonEnumEntryIndex = sb.length
                }

                for (secondLevelChild in secondLevel.allChildren) {
                    if (secondLevelChild !is KtClassBody) {
                        sb.append(secondLevelChild.text)
                        continue
                    }

                    var skipWhiteSpaceThirdLevel = false

                    for (thirdLevel in secondLevelChild.allChildren) {
                        if (skipWhiteSpaceThirdLevel && thirdLevel is PsiWhiteSpace) continue

                        if (thirdLevel !is KtElement) {
                            sb.append(thirdLevel.text)

                            previousEle = thirdLevel
                            continue
                        }
                        previousEle = thirdLevel

                        if (thirdLevelIndent == null) {
                            thirdLevelIndent = thirdLevel.qIndentPart()
                        }

                        if (thirdLevel.qSkipWriting(analysisCtx)) {
                            // skip this declaration
                            val next = thirdLevel.getNextSiblingIgnoringWhitespace()
                            skipWhiteSpaceThirdLevel = next != null && next !is LeafPsiElement

                            previousEle = thirdLevel
                            continue
                        }

                        skipWhiteSpaceThirdLevel = false

                        val thirdLevelSrc = lib.srcCode.nonTopLevelSrc(
                            QNonTopLevelSrcCodeCreationScope(
                                element = thirdLevel,
                                level = QSrcLevel.Third,
                                indent = thirdLevelIndent,
                                analysisCtx = analysisCtx,
                                srcSetType = srcSetType
                            )
                        )

                        sb.qRemoveLastNonLinebreakWhitespaces()

                        sb.append(thirdLevelSrc)

                        previousEle = thirdLevel
                    } // end thirdLevel
                } // end secondLevel
            } else { // secondLevel has no children
                val secondLevelSrc = lib.srcCode.nonTopLevelSrc(
                    QNonTopLevelSrcCodeCreationScope(
                        element = secondLevel,
                        level = QSrcLevel.Second,
                        indent = secondLevelIndent,
                        analysisCtx = analysisCtx,
                        srcSetType = srcSetType
                    )
                )

                sb.qRemoveLastNonLinebreakWhitespaces()

                if (sb.last() != '\n') {
                    sb.append('\n')
                }

                if (topLevelType == QDeclarationType.EnumClass && secondLevel !is KtEnumEntry && firstSecondLevelNonEnumEntryIndex < 0) {
                    firstSecondLevelNonEnumEntryIndex = sb.length
                }

                sb.append(secondLevelSrc)

                if (secondLevel is KtEnumEntry) {
                    lastSecondLevelEnumEntryIndex = sb.length - 1
                }

                previousEle = secondLevel
            }
        } // end secondLevel

        previousEle = topLevelChild
    } // end topLevelChild

    var compactSrc = sb.toString()

    if (lastSecondLevelEnumEntryIndex > 0) {
        // handle the last EnumEntry and comma / semicolon

        var whiteSpaceCount = 0
        var commaIdx = -1

        for (ch in compactSrc.toCharArray(lastSecondLevelEnumEntryIndex)) {
            if (Character.isWhitespace(ch)) {
                whiteSpaceCount++
                continue
            }

            if (ch == ',') {
                commaIdx = lastSecondLevelEnumEntryIndex + whiteSpaceCount
            }

            break
        }

        if (commaIdx > 0) {
            if (sb[commaIdx] != ',') {
                QE.Unreachable.throwIt()
            }

            sb.setCharAt(commaIdx, ';')
            compactSrc = sb.toString()
        }
    } else if (firstSecondLevelNonEnumEntryIndex > 0) {
        // Zero Enum Entries
        sb.insert(firstSecondLevelNonEnumEntryIndex, ";")
        compactSrc = sb.toString()
    }

    val src = lib.srcCode.topLevelSrc(
        QTopLevelSrcCodeScope(
            compactSrc = compactSrc,
            element = topLevelElement,
            visibilityChange = visibilityChange,
            analysisCtx = analysisCtx,
            srcSetType = srcSetType
        )
    )

    val lastLine = src.qLastLine(false)!!
    val lastLineTrimmed = lastLine.trimStart()

    return if (lastLineTrimmed == "}" && lastLine.length > lastLineTrimmed.length) {
        // "   }" to "\n}"
        "\n" + src.substring(0, src.length - lastLine.length) + lastLineTrimmed
    } else {
        src
    }
}