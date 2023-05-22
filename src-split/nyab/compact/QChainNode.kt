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
import nyab.conf.QE
import nyab.test.shouldBe
import nyab.util.qUnreachable
import nyab.util.qWithMaxLengthMiddleDots
import nyab.util.throwIt
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassInitializer
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QChainNode(
    val topLevel: KtElement,

    val secondLevel: KtElement?,

    val thirdLevel: KtElement?,

    val analysisCtx: QAnalysisContext,

    val listCyclicDuplicatesCallTo: MutableList<QChainNode> = mutableListOf(),
) {
    // << Root of the CallChain >>
    val targetElement: KtElement = thirdLevel ?: (secondLevel ?: topLevel)

    // << Root of the CallChain >>
    val text: String = targetElement.text

    // << Root of the CallChain >>
    val containingFile: Path = topLevel.containingKtFile.qPath

    // << Root of the CallChain >>
    fun isRootOfChain() = root() != null // isMarked() && chainFrom() == null

    // << Root of the CallChain >>
    fun root(): QChainNode? {
        return analysisCtx.rootOfChainNode[this.key]
    }

    // << Root of the CallChain >>
    fun registerAsRootOfChainNode() {
        analysisCtx.rootOfChainNode[this.key] = this
    }

    // << Root of the CallChain >>
    val targetSrcLevel: QSrcLevel = if (thirdLevel != null) {
        QSrcLevel.Third
    } else if (secondLevel != null) {
        QSrcLevel.Second
    } else {
        QSrcLevel.Top
    }

    // << Root of the CallChain >>
    val pkgFqName: String by lazy {
        containingTopLevelCompactElement().pkgFqName
    }

    // << Root of the CallChain >>
    fun isTopLevel(): Boolean = targetSrcLevel == QSrcLevel.Top

    // << Root of the CallChain >>
    fun isSecondLevel(): Boolean = targetSrcLevel == QSrcLevel.Second

    // << Root of the CallChain >>
    fun isThirdLevel(): Boolean = targetSrcLevel == QSrcLevel.Third

    // << Root of the CallChain >>
    val targetElementClassBody: KtClassBody? = targetElement.qClassBody()

    // << Root of the CallChain >>
    val hasChildNode: Boolean = targetElement.qHasChildCallChainNode(targetSrcLevel)

    // << Root of the CallChain >>
    fun descendantNodes(): List<QChainNode> {
        val compactEle = topLevelCompactElement()
        return compactEle.chainNodes.filter {
            !it.isTopLevel() &&
                it.outerNodes().map { outer -> outer.key }.contains(this.key)
        }.toList()
    }

    // << Root of the CallChain >>
    fun outerNodes(): List<QChainNode> {
        return when (targetSrcLevel) {
            QSrcLevel.Top ->
                emptyList()
            QSrcLevel.Second ->
                listOf(topLevel.qCallChainNode(analysisCtx))
            QSrcLevel.Third ->
                listOf(topLevel.qCallChainNode(analysisCtx), secondLevel!!.qCallChainNode(analysisCtx))
            QSrcLevel.Deep ->
                listOf(
                    topLevel.qCallChainNode(analysisCtx),
                    secondLevel!!.qCallChainNode(analysisCtx),
                    thirdLevel!!.qCallChainNode(analysisCtx)
                )
        }
    }

    // << Root of the CallChain >>
    fun topLevelCompactElement(): QTopLevelCompactElement {
        return topLevel.qContainingTopLevelCompactElement(analysisCtx)
    }

    // << Root of the CallChain >>
    fun isOverridden(): Boolean = targetElement is KtDeclaration && targetElement.hasModifier(KtTokens.OVERRIDE_KEYWORD)

    // << Root of the CallChain >>
    fun hasOpenKeyword(): Boolean = targetElement is KtDeclaration && targetElement.hasModifier(KtTokens.OPEN_KEYWORD)

    // << Root of the CallChain >>
    fun isInOpenClass(): Boolean =
        !isTopLevel() && topLevel is KtDeclaration && topLevel.hasModifier(KtTokens.OPEN_KEYWORD)

    // << Root of the CallChain >>
    fun isEnumEntry(): Boolean =
        targetElement is KtEnumEntry

    // << Root of the CallChain >>
    fun isInCompanionObject(): Boolean =
        targetSrcLevel == QSrcLevel.Third && secondLevel is KtObjectDeclaration && secondLevel.isCompanion()

    // << Root of the CallChain >>
    fun isInAbstractClass(): Boolean = topLevel is KtDeclaration && topLevel.hasModifier(KtTokens.ABSTRACT_KEYWORD)

    // << Root of the CallChain >>
    fun isInInterface(): Boolean = topLevel is KtClass && topLevel.isInterface() && thirdLevel == null

    // << Root of the CallChain >>
    fun chainFrom(): QChain? {
        val node = analysisCtx.chainFrom[this.key]

        if (node != null)
            node.targetNode.key shouldBe this.key

        return node
    }

    // << Root of the CallChain >>
    fun nodeChainFrom(): QChainNode? {
        return chainFrom()?.fromNode
    }

    // << Root of the CallChain >>
    fun chainNodesFromRoot(): List<QChainNode> {
        val list = mutableListOf<QChainNode>()

        list += this

        list += chainListFromRoot().map { it.fromNode }

        return list
    }

    // << Root of the CallChain >>
    private fun chainText(chain: QChain): String {
        return "${chain.targetNode.simpleName()} <-[${chain.type.shortName}]-"
    }

    // << Root of the CallChain >>
    fun chainsToString(chains: List<QChain>, maxLength: Int = 200): String {
        if (chains.isEmpty()) {
            return if (this.isRootOfChain()) {
                "<< Root of the CallChain >>"
            } else {
                "CallChain[size=0] No Chain Information"
            }
        }

        var chainText = chains.joinToString(" ") { chain ->
            chainText(chain)
        }

        val root = chains.last().fromNode

        chainText += " ${root.simpleName()}"

        if (root.isRootOfChain()) {
            chainText += "[Root]"
        }

        return "CallChain[size=${chains.size + 1}] = $chainText".qWithMaxLengthMiddleDots(maxLength)
    }

    // << Root of the CallChain >>
    fun chainListFromRoot(): List<QChain> {
        if (isRootOfChain()) {
            return emptyList()
        }

        var from = chainFrom() ?: return emptyList()

        val chainList = mutableListOf<QChain>()

        chainList += from

        val cyclicCheck = mutableSetOf<QKtElementKey>()

        while (true) {
            val next = from.fromNode.chainFrom()

            if (next != null && !from.fromNode.isRootOfChain()) {
                if (cyclicCheck.contains(next.key)) {
                    return chainList
                } else {
                    cyclicCheck.add(next.key)
                }

                chainList += next
                from = next
            } else {
                return chainList
            }
        }
    }

    // << Root of the CallChain >>
    fun nodesChainTo(): List<QChainNode> {
        return chainTo().map {
            it.targetNode
        }
    }

    // << Root of the CallChain >>
    fun chainTo(): List<QChain> {
        val alreadyResolved = analysisCtx.resolvedChains[this.key]
        if (alreadyResolved != null) {
            return alreadyResolved
        }

        val callChains = callOrRefTo()
        val propagatedChains = propagateTo()

        val chainList = (callChains + propagatedChains).distinctBy { it.key }

        chainList.all { it.fromNode.key == this.key } shouldBe true

        analysisCtx.resolvedChains[this.key] = chainList

        return chainList
    }

    // << Root of the CallChain >>
    private fun callOrRefTo(): List<QChain> {
        val elementsToResolve = ktElementsToResolve()

        return elementsToResolve.mapNotNull { ele ->
            ele.qResolveToChain(this, analysisCtx)
        }.toList()
    }

    // << Root of the CallChain >>
    private fun propagateTo(): List<QChain> {
        val visitor = analysisCtx.lib.nodeVisitor

        val isTargetTopLevel = this.isTopLevel()

        val topLevelNode = this.topLevel.qCallChainNode(analysisCtx)

        val chainType =
            if (isTargetTopLevel) QChainType.PropagatedFromTopLevel else QChainType.PropagatedFromSibling

        return topLevelNode.descendantNodes().mapNotNull { targetNode ->
            if (targetNode.key == this.key)
                return@mapNotNull null

            if (analysisCtx.chainFrom.containsKey(targetNode.key) && analysisCtx.chainFrom[targetNode.key] != null) {
                // already resolved
                return@mapNotNull null
            }

            if (visitor.isMustDiscard(targetNode)) {
                return@mapNotNull null
            }

            if (visitor.isMustMark(targetNode)) {
                return@mapNotNull QChain(targetNode, this, chainType, true, analysisCtx)
            }

            if (visitor.propagated(
                    QNodePropagatedScope(
                            target = targetNode,
                            from = this,
                            type = chainType.toPropagationType()
                        )
                ) == QNodeVisited.MarkThisNodeAndContinueChain
            ) {
                QChain(targetNode, this, chainType, false, analysisCtx)
            } else {
                null
            }
        }
    }

    // << Root of the CallChain >>
    fun markThis() {
        analysisCtx.markedNodes[key] = this
    }

    // << Root of the CallChain >>
    fun isMarked(): Boolean {
        return analysisCtx.markedNodes.containsKey(key)
    }

    // << Root of the CallChain >>
    fun isCompanionObject(): Boolean {
        return targetElement is KtObjectDeclaration && targetElement.isCompanion()
    }

    // << Root of the CallChain >>
    fun isClassInitializer(): Boolean {
        return targetElement is KtClassInitializer
    }

    // << Root of the CallChain >>
    fun isPrimaryConstructor(): Boolean {
        return targetElement is KtPrimaryConstructor
    }

    // << Root of the CallChain >>
    fun isSecondaryConstructor(): Boolean {
        return targetElement is KtSecondaryConstructor
    }

    // << Root of the CallChain >>
    fun isConstructor(): Boolean {
        return targetElement is KtConstructor<*>
    }

//    fun isNecessary(): Boolean {
//        if( isInCompanionObject() ) {
//            return false
//        }
//
//        return isClassInitializer() || isOverridden() || isInOpenClass() || isCompanionObject() || isInInterface()
//    }

    // << Root of the CallChain >>
    fun callChainAsString(maxLength: Int = 200): String {
        val chains = chainListFromRoot()

        return chainsToString(chains, maxLength)
    }

    // << Root of the CallChain >>
    fun comment(): String {
        if (this.isCompanionObject()) {
            return ""
        }

        return "// " + callChainAsString()
    }

    // << Root of the CallChain >>
    fun simpleName(withPsiClassName: Boolean = false, withSrcLevel: Boolean = false): String {
        val name = if (thirdLevel != null) {
            topLevel.qSimpleName() + "." + thirdLevel.qSimpleName() + if (withPsiClassName) " [${thirdLevel.javaClass.simpleName}]" else ""
        } else if (secondLevel != null) {
            topLevel.qSimpleName() + "." + secondLevel.qSimpleName() + if (withPsiClassName) " [${secondLevel.javaClass.simpleName}]" else ""
        } else {
            topLevel.qSimpleName() + if (withPsiClassName) " [${topLevel.javaClass.simpleName}]" else ""
        }

        return name + if (withSrcLevel) " <${targetElementLevelText()}>" else ""
    }

//    fun targetElementSrcWithRequiredClassBody(br: String): String {
//        val requiredDeclarations = targetElementClassBody!!.declarations.filter {
//            it.qContainingCallChainNode(analysisCtx)
//        }
//
//        val requiredClassBody = requiredDeclarations.joinToString("$br$br")
//
//        return targetElementSrcTextWithClassBody(requiredClassBody)
//    }

//    private fun targetElementSrcTextWithClassBody(newClassBody: String): String {
//        return if (targetElementClassBody != null) {
//            val range = targetElementClassBody.textRangeInParent
//            targetElement.text.replaceRange(IntRange(range.startOffset, range.endOffset), newClassBody)
//        } else {
//            targetElement.text
//        }
//    }

    // << Root of the CallChain >>
    val key: QKtElementKey = targetElement.qElementKey

    // << Root of the CallChain >>
    fun targetElementLevel(): Int {
        return if (thirdLevel != null) {
            2
        } else if (secondLevel != null) {
            1
        } else {
            0
        }
    }

    // << Root of the CallChain >>
    fun targetElementLevelText(): String {
        return if (thirdLevel != null) {
            "Third Level"
        } else if (secondLevel != null) {
            "Second Level"
        } else {
            "Top Level"
        }
    }

    // << Root of the CallChain >>
    fun propertyAccessors(): List<KtElement> {
        return if (targetElement is KtProperty) {
            targetElement.accessors
        } else {
            emptyList()
        }
    }

    // << Root of the CallChain >>
    fun ktElementsToResolve(): List<KtElement> {
        val classBody = topLevel.children.filterIsInstance<KtClassBody>().firstOrNull()

        val elements = if (classBody != null) {
            targetElement.children.filter { it !is KtClassBody }
                .flatMap { it.collectDescendantsOfType<KtElement>() }
        } else {
            targetElement.collectDescendantsOfType<KtElement>()
        }

        val accessorsDescendants = this.propertyAccessors().flatMap {
            it.collectDescendantsOfType<KtElement>()
        }

        val beforeFilterByType = if (accessorsDescendants.isNotEmpty()) {
            elements + accessorsDescendants
        } else {
            elements
        }

        return beforeFilterByType
//                .filter {
//            it !is KtOperationExpression
//            it !is KtImportDirective
//            it !is KtPackageDirective
//            it !is KtOperationExpression
//            it !is KtOperationReferenceExpression
//            it !is KtBlockExpression//TODO
//        }
    }

    // << Root of the CallChain >>
    override fun toString(): String {
        return simpleName(withPsiClassName = true, withSrcLevel = true)
    }

//    fun chainType(): QChainNodeVisitResult {
//        return analysisCtx.libConf.nodeVisitor(this)
//    }

    // << Root of the CallChain >>
    fun containingTopLevelCompactElement(): QTopLevelCompactElement {
        return topLevel.qContainingTopLevelCompactElement(analysisCtx)
    }

    // << Root of the CallChain >>
    fun chainRoot(): QChainNode {
        var from: QChainNode? = nodeChainFrom() ?: return this

//        val cyclicCheck = mutableSetOf<QKtElementKey>()

        while (from != null) {
            val next = from.nodeChainFrom() ?: return from
            from = next
        }

        qUnreachable()
    }

    // << Root of the CallChain >>
    fun allChainDescendants(): Sequence<QChainNode> = sequence {
        // Breadth-first search

        val cycleCheck = mutableSetOf<QKtElementKey>()

        val stack = mutableListOf<QChainNode>()

        stack += this@QChainNode

        var firstNode = true

        // mark
        cycleCheck.add(this@QChainNode.key)

        while (stack.isNotEmpty()) {
            val node = stack.removeAt(stack.size - 1) // stack.pop()

            if (firstNode) {
                firstNode = false
            } else {
                yield(node)
            }

            for (n in node.chainTo().reversed()) {
                if (cycleCheck.contains(n.key)) {
                    // already visited
                    continue
                }

                stack += n.targetNode

                cycleCheck.add(n.key)
            }
        }
    }

    // << Root of the CallChain >>
    override fun equals(other: Any?): Boolean {
        if( other == null || other !is QChainNode) {
            return false
        }

        return this.key.value == other.key.value
    }

    // << Root of the CallChain >>
    override fun hashCode(): Int {
        return this.key.value.hashCode()
    }
}

// << Root of the CallChain >>
enum class QChainType(val shortName: String) {
    // << Root of the CallChain >>
    ByTypeReference("Ref"),
    // << Root of the CallChain >>
    ByCall("Call"),
    // << Root of the CallChain >>
    PropagatedFromSibling("Propag"),
    // << Root of the CallChain >>
    PropagatedFromTopLevel("Propag");

    // << Root of the CallChain >>
    fun toPropagationType(): QPropagationType {
        return when (this) {
            PropagatedFromSibling ->
                QPropagationType.FromTopLevel
            PropagatedFromTopLevel ->
                QPropagationType.FromSibling
            ByTypeReference, ByCall ->
                qUnreachable()
        }
    }
}

// << Root of the CallChain >>
enum class QPropagationType {
    // << Root of the CallChain >>
    FromSibling,
    // << Root of the CallChain >>
    FromTopLevel
}

// << Root of the CallChain >>
class QChain(
    val targetNode: QChainNode,
    val fromNode: QChainNode,
    val type: QChainType,
    val mustBeMarked: Boolean,
    analysisCtx: QAnalysisContext,
) {
    // << Root of the CallChain >>
    init {
        if (analysisCtx.chainFrom.containsKey(targetNode.key)) {
            QE.Unreachable.throwIt(
                msg = targetNode.key
            )
        }
        if (targetNode.key == fromNode.key) {
            QE.Unreachable.throwIt(
                msg = targetNode.key
            )
        }

        analysisCtx.chainFrom[targetNode.key] = this
    }

    // << Root of the CallChain >>
    val key: QKtElementKey = targetNode.key

    // << Root of the CallChain >>
    val isPropagated: Boolean = when (type) {
        QChainType.PropagatedFromSibling, QChainType.PropagatedFromTopLevel ->
            true
        QChainType.ByCall, QChainType.ByTypeReference ->
            false
    }
}

// << Root of the CallChain >>
@JvmInline
value class QKtElementKey(val value: String)