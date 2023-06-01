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
import java.nio.file.Paths
import kotlin.reflect.KClass
import nyab.conf.QE
import nyab.util.blue
import nyab.util.green
import nyab.util.light_gray
import nyab.util.logBlue
import nyab.util.qCacheIt
import nyab.util.qFirstLine
import nyab.util.qLineAt
import nyab.util.qLineNumberAt
import nyab.util.qLog
import nyab.util.qReadFile
import nyab.util.red
import nyab.util.throwIt
import nyab.util.yellow
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtDeclarationContainer
import org.jetbrains.kotlin.psi.KtDeclarationModifierList
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtInitializerList
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtObjectLiteralExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.psi.psiUtil.isTopLevelKtOrJavaMember
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffsetSkippingComments
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.resolve.source.PsiSourceElement
import org.jetbrains.kotlin.resolve.source.toSourceElement
import org.jetbrains.kotlin.util.containingNonLocalDeclaration

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
fun KtElement.qIsSrcLevelTarget(): Boolean {
    if (this is KtPrimaryConstructor)
        return false

    return this.isTopLevelKtOrJavaMember() || this is KtDeclaration // || this is KtImportDirective
}

// << Root of the CallChain >>
fun KtElement.qCallChainNode(analysisCtx: QAnalysisContext): QChainNode {
    var node = analysisCtx.callChainNodes[this.qElementKey]

    if (node == null) {
        node = this.qCreateCallChainNode(analysisCtx)
        analysisCtx.callChainNodes[this.qElementKey] = node
    }

    return node
}

// << Root of the CallChain >>
fun KtElement.qClassName(): String {
    return if (this is KtClass) {
        this.name.toString()
    } else {
        ""
    }
}

// << Root of the CallChain >>
fun KtElement.qIsInChainNode(): Boolean {
    return this.qOuterShallowSrcLevelTarget(true) != null
}

// << Root of the CallChain >>
fun KtElement.qContainingCallChainNode(
    analysisCtx: QAnalysisContext,
    allowReturnMySelf: Boolean = true,
): QChainNode {
    val outer = this.qOuterShallowSrcLevelTarget(allowReturnMySelf)

    if (outer == null) {
        // Might be Import Directive
        allowReturnMySelf.logBlue
        this.qDebug()
    }

    return outer!!.qCallChainNode(analysisCtx)
}

// << Root of the CallChain >>
fun KtElement.qCreateCallChainNode(analysisCtx: QAnalysisContext): QChainNode {
    val srcLevel = this.qSrcLevel()

    return if (srcLevel == QSrcLevel.Top) { // top level element
        QChainNode(
            topLevel = this, secondLevel = null, thirdLevel = null, analysisCtx = analysisCtx
        )
    } else { // inner element
        val topLevelEle = this.qOuter(QSrcLevel.Top)!!
        val secondLevelEle = this.qOuter(QSrcLevel.Second)!!

        if (topLevelEle.qHasChildCallChainNode(QSrcLevel.Top)) {
            if ((srcLevel == QSrcLevel.Third || srcLevel == QSrcLevel.Deep) &&
                secondLevelEle.qHasChildCallChainNode(QSrcLevel.Second)
            ) { // companion object
                val thirdLevel = this.qOuter(QSrcLevel.Third)

                QChainNode(
                    topLevel = topLevelEle,
                    secondLevel = secondLevelEle,
                    thirdLevel = thirdLevel,
                    analysisCtx = analysisCtx
                )
            } else {
                QChainNode(
                    topLevel = topLevelEle, secondLevel = secondLevelEle, thirdLevel = null, analysisCtx = analysisCtx
                )
            }
        } else {
            QChainNode(
                topLevel = topLevelEle, secondLevel = null, thirdLevel = null, analysisCtx = analysisCtx
            )
        }
    }
}

// << Root of the CallChain >>
fun PsiElement.qToDebugSrcString(): String {
    val sb = StringBuilder()

    sb.append("This Element Class = [${this.javaClass.simpleName}]\n".red)

    for (child in this.allChildren) {
        if (child is PsiWhiteSpace) {
            sb.append(child.text)
            continue
        }
        sb.append(child.text)
        sb.append("[${child.javaClass.simpleName}]".yellow)

        for (child2 in child.allChildren) {
            if (child2 is PsiWhiteSpace) {
                sb.append(child.text)
                continue
            }

            sb.append(" {{ ".light_gray)
            sb.append(child2.text)
            sb.append("[${child2.javaClass.simpleName}]".blue)
            sb.append(" }} ".light_gray)

            for (child3 in child2.children) {
                sb.append(" {{{ ".light_gray)
                sb.append(child3.text)
                sb.append("[${child3.javaClass.simpleName}]".green)
                sb.append(" }}} ".light_gray)
            }
        }
    }

    return sb.toString()
}

// << Root of the CallChain >>
fun KtElement.qDebug(): KtElement {
    qToDebugString().qLog(stackDepth = 1)
    return this
}

// << Root of the CallChain >>
fun KtElement.qContainingFileAndLineNum(charset: Charset = Charsets.UTF_8): String {
    return this.containingKtFile.virtualFilePath + ":" + this.qSrcLineNum(charset)
}

// << Root of the CallChain >>
fun KtElement.qToDebugString(): String {
    return """Class          = ${this.javaClass.name.blue}
TopLevel       = ${this.isTopLevelKtOrJavaMember().toString().blue}
SrcDepth       = ${this.qSrcLevel().toString().blue}
nChildren      = ${this.children.size}
Children       = ${this.children.joinToString() { it.javaClass.simpleName }}
nDescendants   = ${this.collectDescendantsOfType<KtElement>().size}
File           = ${this.qContainingFileAndLineNum().blue}
SrcLine        = ${this.qSrcLine()}
SrcText        = ${this.text.yellow}
SrcRange       = ${this.textRange.toString().green}
ParentClass    = ${this.parent?.javaClass?.name?.blue}
Parent*2Class  = ${this.parent?.parent?.javaClass?.name?.blue}
Parent*3Class   = ${this.parent?.parent?.parent?.javaClass?.name?.blue}
ParentSrcRange = ${this.parent?.textRange?.toString()?.green}
ElementKey     = ${this.qElementKey.value.blue}
qOuter         = ${this.qOuterSrcLevelTarget(false)?.text?.qFirstLine()?.blue}
qOuterThird    = ${this.qOuter(QSrcLevel.Third)?.text?.qFirstLine()?.blue}
    """.trimIndent()
}

// << Root of the CallChain >>
fun KtElement.qHasChildCallChainNode(srcLevel: QSrcLevel): Boolean {
    val type = this.qDeclarationType()

    return when (srcLevel) {
        QSrcLevel.Top -> when (type) {
            QDeclarationType.EnumClass,
            QDeclarationType.DataClass,
            QDeclarationType.Class,
            QDeclarationType.CompanionObject,
            QDeclarationType.ObjectLiteral,
            -> true

            else -> false
        }

        QSrcLevel.Second -> when (type) {
            QDeclarationType.CompanionObject -> true
            else -> false
        }

        else -> false
    }
}

// << Root of the CallChain >>
fun KtElement.qDeclarationType(): QDeclarationType = when (this) {
    is KtClass -> {
        if (this.isEnum()) {
            QDeclarationType.EnumClass
        } else if (this.isAnnotation()) {
            QDeclarationType.AnnotationClass
        } else if (this.isData()) {
            QDeclarationType.DataClass
        } else {
            QDeclarationType.Class
        }
    }

    is KtInitializerList -> {
        QDeclarationType.ClassInitializer
    }

    is KtObjectDeclaration -> {
        if (this.isCompanion()) {
            QDeclarationType.CompanionObject
        } else {
            QDeclarationType.ObjectLiteral
        }
    }

    is KtObjectLiteralExpression -> {
        QDeclarationType.ObjectLiteral
    }

    is KtProperty -> QDeclarationType.Property
    is KtNamedFunction -> QDeclarationType.Function
    is KtTypeAlias -> QDeclarationType.TypeAlias
    else -> {
//        "QDeclarationType = Unknown : ${this::class.simpleName}".logYellow

        QDeclarationType.Unknown
    }
}

// << Root of the CallChain >>
fun KtElement.qClassBody(): KtClassBody? {
    return this.getChildrenOfType<KtClassBody>().firstOrNull()
}

// << Root of the CallChain >>
val KtElement.qElementKey: QKtElementKey
    get() {
        return QKtElementKey("${containingKtFile.virtualFilePath}::${textRange.startOffset}-${textRange.endOffset}")
    }

// << Root of the CallChain >>
inline val KtElement.qElementsAll: List<KtElement>
    get() = collectDescendantsOfType()

// << Root of the CallChain >>
fun KtElement.qVisibility(): QKtVisibility {
    return if (this is KtDeclaration) {
        when (this.visibilityModifierTypeOrDefault()) {
            KtTokens.PUBLIC_KEYWORD -> QKtVisibility.Public
            KtTokens.PRIVATE_KEYWORD -> QKtVisibility.Private
            KtTokens.INTERNAL_KEYWORD -> QKtVisibility.Internal
            KtTokens.PROTECTED_KEYWORD -> QKtVisibility.Protected
            else -> QE.Unreachable.throwIt()
        }
    } else {
        QKtVisibility.Unknown
    }
}

// << Root of the CallChain >>
fun <T : KtElement> T.qToString(): String {
    return """
        ${containingKtFile.qPath} => $name
        src =>\n$text
    """.trimIndent()
}

// << Root of the CallChain >>
fun <T : PsiSourceElement> T.qToString(): String {
    return this.psi?.text ?: this.toString()
}

// << Root of the CallChain >>
fun <T : KtDeclaration> T.qToString(): String {
    return """${containingKtFile.qPath} => $name\n$text"""
}

// << Root of the CallChain >>
val KtElement.qSrc: KotlinSourceElement
    get() = qCacheIt(this.qElementKey) { this.toSourceElement() as KotlinSourceElement }

// << Root of the CallChain >>
fun KtElement.qOuter(srcLevel: QSrcLevel): KtElement? = this.qOuter(srcLevel.intValue)

// << Root of the CallChain >>
fun PsiElement.qCommentText(): String = text.substring(0, startOffsetSkippingComments - startOffset)

// << Root of the CallChain >>
fun PsiElement.qCommentAndAnnotationText(): String =
    text.substring(0, qStartOffsetSkippingCommentsAndDeclarationModifierList() - startOffset)

// << Root of the CallChain >>
fun PsiElement.qStartOffsetSkippingCommentsAndDeclarationModifierList(): Int {
    val firstNonCommentChild = generateSequence(firstChild) { it.nextSibling }
        .firstOrNull { it !is PsiWhiteSpace && it !is PsiComment }

    if (firstNonCommentChild != null && firstNonCommentChild is KtDeclarationModifierList) {
        val firstNonAnnotationChild = generateSequence(firstNonCommentChild.firstChild) { it.nextSibling }
            .firstOrNull {
                it !is PsiWhiteSpace && it !is PsiComment && it !is KtAnnotationEntry
            }

        return firstNonAnnotationChild?.startOffset ?: (firstNonCommentChild.endOffset + 1)
    }

    return firstNonCommentChild?.startOffset ?: startOffset
}

// << Root of the CallChain >>
fun KtElement.qMaybeFirstTokenIndexSkippingCommentAndAnnotation(modifiedSrc: String = this.text): Int {
    val commentAndAnnotation = this.qCommentAndAnnotationText() // .qCommentText()

    return if (commentAndAnnotation.isEmpty()) {
        0
    } else {
        modifiedSrc.indexOf(commentAndAnnotation) + commentAndAnnotation.length
    }
}

// << Root of the CallChain >>
fun KtElement.qSrcLevelAsInt(): Int {
    var level = 0
    var outer: KtElement? = this

    while (outer != null) {
        outer = outer.qOuterSrcLevelTarget(false)

        if (outer == null) return level

        level++

        if (outer.isTopLevelKtOrJavaMember()) return level
    }

    return level
}

// << Root of the CallChain >>
fun KtElement.qSrcLevel(): QSrcLevel {
    val level = this.qSrcLevelAsInt()
    return QSrcLevel.fromIntValue(level)
}

// << Root of the CallChain >>
fun KtElement.qOuterShallowSrcLevelTarget(allowReturnMySelf: Boolean): KtElement? {
    var outer = this.qOuterSrcLevelTarget(allowReturnMySelf)

    while (outer != null && outer.qSrcLevel() == QSrcLevel.Deep) {
        val outerOuter = outer.qOuterSrcLevelTarget(false) ?: return outer

        outer = outerOuter
    }

    return outer
}

// << Root of the CallChain >>
fun KtElement.qOuterSrcLevelTarget(allowReturnMySelf: Boolean): KtElement? {
    var outer: PsiElement? = this

    if (this.isTopLevelKtOrJavaMember() || this.parent is PsiFile) {
        return if (allowReturnMySelf) {
            this
        } else {
            null
        }
    }

    if (this.qIsSrcLevelTarget() && allowReturnMySelf) {
        return this
    }

    while (true) {
        val parent = outer!!.parent ?: return null

        if (parent !is KtElement) {
            return null
//            outer = parent
//            continue
        }

        outer = parent

        val outerKt = parent as KtElement

        return if (outerKt is KtFile) {
            null
        } else if (outerKt.isTopLevelKtOrJavaMember()) {
            outerKt
        } else if (outerKt is KtPropertyAccessor) {
            outerKt.property
        } else if (outerKt is KtParameter || outerKt is KtParameterList) {
            outerKt.containingNonLocalDeclaration()
        } else if (outerKt is KtPrimaryConstructor) {
            continue
        } else if ( // outerKt is KtPrimaryConstructor ||
            outerKt is KtImportList ||
            outerKt is KtSecondaryConstructor || outerKt is KtObjectDeclaration || outerKt is KtProperty || outerKt is KtNamedFunction || outerKt is KtClass
        ) {
            outerKt
        } else {
//            if (outerKt !is KtFunctionLiteral && outerKt !is KtIfExpression && outerKt !is KtContainerNodeForControlStructureBody && outerKt !is KtClassBody && outerKt !is KtObjectLiteralExpression && outerKt !is KtLambdaArgument && outerKt !is KtCallExpression && outerKt !is KtLambdaExpression && outerKt !is KtBlockExpression && outerKt !is KtWhileExpression && outerKt !is KtForExpression && outerKt !is KtReturnExpression && outerKt !is KtDotQualifiedExpression && outerKt !is KtValueArgument && outerKt !is KtValueArgumentList && outerKt !is KtPropertyDelegate && outerKt !is KtTryExpression && outerKt !is KtTypeParameterList && outerKt !is KtDestructuringDeclaration) {
//                outerKt.text.logYellow
//                outerKt.javaClass.name.logRed
//            }

            continue
        }
    }
}

// << Root of the CallChain >>
fun KtElement.qInnerDeclarations(): List<KtDeclaration> {
    return if (this is KtDeclarationContainer) {
        this.declarations
    } else {
        emptyList()
    }
}

// << Root of the CallChain >>
fun KtElement.qOuter(targetLv: Int): KtElement? {
    val curLv = qSrcLevelAsInt()

    if (curLv == targetLv) return this

    var inner: KtElement = this
    var outer: KtElement? = null

    repeat(curLv - targetLv) {
        outer = inner.qOuterSrcLevelTarget(false)

        if (outer == null) {
            return@qOuter inner
        } else if (outer!!.isTopLevelKtOrJavaMember()) {
            return@qOuter outer
        } else {
            inner = outer!!
        }
    }

    return outer
}

// << Root of the CallChain >>
fun KtElement.qDetail(): String = "${this.qPath}:${this.qSrcLineNum()}\n${this.qSrcLine()}"

// << Root of the CallChain >>
fun KtElement.qSimpleName(parenthesesIfFunction: Boolean = true): String {
    val n = name ?: this.text.qFirstLine().trim()

    val name = if (this.isExtensionDeclaration() && this is KtCallableDeclaration) {
        val typeName = this.receiverTypeReference?.qSimpleName()?.removeSuffix("?")
        if (typeName != null) {
            "$typeName.$n"
        } else {
            n
        }
    } else {
        n
    }

    return if (this is KtFunction) {
        if (parenthesesIfFunction) {
            "$name()"
        } else {
            name
        }
    } else {
        name
    }
}

// << Root of the CallChain >>
fun KtElement.qFqName(): String {
    val pkgName = this.containingKtFile.packageFqName.toString()
    return if (pkgName.isNotEmpty()) {
        pkgName + "." + qSimpleName()
    } else {
        this.qSimpleName()
    }
}

// << Root of the CallChain >>
fun KtElement.qContainingTopLevelCompactElement(analysisCtx: QAnalysisContext): QTopLevelCompactElement {
    val topLevel = this.qOuter(QSrcLevel.Top)!!
    val key = topLevel.qElementKey

    val compactEle = analysisCtx.containingCompactElements[key]

    if (compactEle != null) {
        return compactEle
    }

    val classBody = topLevel.qClassBody()

    val nodes = mutableListOf<QChainNode>()

    if (classBody == null) {
        val newCompactEle =
            QTopLevelCompactElement(listOf(topLevel.qContainingCallChainNode(analysisCtx, true)), analysisCtx)
        analysisCtx.containingCompactElements[key] = newCompactEle
        return newCompactEle
    } else {
        val topNode = topLevel.qContainingCallChainNode(analysisCtx, true)
        nodes += topNode

        for (secondLevel in classBody.children) {
            if (secondLevel !is KtElement) continue

            nodes += secondLevel.qContainingCallChainNode(analysisCtx, true)

            if (secondLevel !is KtObjectDeclaration || !secondLevel.isCompanion())
                continue

            val secondLevelClassBody = secondLevel.qClassBody() ?: continue

            for (thirdLevel in secondLevelClassBody.children) {
                if (thirdLevel !is KtElement) continue

                nodes += thirdLevel.qContainingCallChainNode(analysisCtx, true)
            }
        }
    }

    val newCompactEle = QTopLevelCompactElement(nodes, analysisCtx)
    analysisCtx.containingCompactElements[key] = newCompactEle

    return newCompactEle
}

// << Root of the CallChain >>
fun KtElement.qClassBodyChildren(): List<KtElement> {
    val classBody = this.getChildrenOfType<KtClassBody>().firstOrNull()
    return classBody?.getChildrenOfType<KtElement>()?.toList() ?: emptyList()
}

// << Root of the CallChain >>
fun KtElement.qIsPublic() = this is KtModifierListOwner && this.isPublic

// << Root of the CallChain >>
fun KtElement.qIsEquals(cls: KClass<*>) = this.qFqName() == cls.qualifiedName

// << Root of the CallChain >>
fun KtElement.qIsEqualsAny(vararg cls: KClass<*>) = cls.any { this.qIsEquals(it) }

// << Root of the CallChain >>
fun KtElement.qSkipWriting(analysisCtx: QAnalysisContext): Boolean {
    val node = this.qContainingCallChainNode(analysisCtx, true)

    return !node.isMarked()
}

// << Root of the CallChain >>
val KtElement.qPath: Path
    get() = Paths.get(this.containingKtFile.virtualFilePath)

// << Root of the CallChain >>
fun KtElement.qSrcLineNum(charset: Charset = Charsets.UTF_8): Int =
    qCacheIt("${this.qPath}#${this.textRange}#LineNum") {
        val file = this.qPath
        val content = file.qReadFile(charset, useCache = true).replace("\r", "")
        content.qLineNumberAt(this.textOffset)
    }

// << Root of the CallChain >>
fun KtElement.qSrcLine(charset: Charset = Charsets.UTF_8): String = qCacheIt("${this.qPath}:${this.textRange}#Line") {
    val file = this.qPath
    val content = file.qReadFile(charset, useCache = true).replace("\r", "")

    content.qLineAt(content.qLineNumberAt(this.textOffset) - 1)
}

// << Root of the CallChain >>
fun KtElement.qIndentPart(): String {
    val whitespaces = StringBuilder()
    var curEle = this.prevSibling
    while (curEle != null && curEle is PsiWhiteSpace) {
        if (!curEle.text.contains("\n")) {
            curEle = curEle.prevSibling
            continue
        }

        val indentPart = curEle.text.takeLastWhile { it.isWhitespace() && it != '\n' && it != '\r' }
        whitespaces.append(indentPart)
        curEle = curEle.prevSibling
    }

    return whitespaces.toString()
}