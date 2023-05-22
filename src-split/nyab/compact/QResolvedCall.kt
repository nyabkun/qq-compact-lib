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

import nyab.util.blue
import nyab.util.green
import nyab.util.qBrackets
import nyab.util.qCacheIt
import nyab.util.qFirstLine
import org.jetbrains.kotlin.builtins.functions.FunctionInvokeDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.getTopLevelContainingClassifier
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets
import org.jetbrains.kotlin.resolve.calls.util.getCall
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.resolve.source.toSourceElement
import org.jetbrains.kotlin.util.containingNonLocalDeclaration

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QResolvedCall(
    val key: QKtElementKey,
    val from: KtElement,
    val to: KtElement?,
    val resolvedDescriptor: DeclarationDescriptor?,
    val resolvedLineHint: String,
    val type: QResolvedType,
    val ctx: QAnalysisContext,
    importDirectiveCandidates: List<String>,
) {

    // << Root of the CallChain >>
    val isResolvedElementInUserCodeBase: Boolean = to?.containingKtFile?.virtualFilePath?.isNotEmpty() == true

    // << Root of the CallChain >>
    val importCandidates: List<String>

    // << Root of the CallChain >>
    init {
        val candidates = importDirectiveCandidates.filter { !it.contains("<") }.toMutableList()
        candidates += candidates.map { "$it.*" } // wildcard-import

        importCandidates = candidates
    }

    // << Root of the CallChain >>
    override fun toString(): String {
        return "${from.text.qFirstLine().blue} [${from.javaClass.simpleName}] -> ${to?.text?.qFirstLine().green}\n" +
                qBrackets(
                    "Type",
                    type,
                    "ResolvedLineHint",
                    resolvedLineHint,
                    "ResolvedDescriptor",
                    resolvedDescriptor.toString(),
                    "ImportCandidates",
                    importCandidates
                )
    }

    // << Root of the CallChain >>
    fun isConstructorCall(): Boolean {
        return to is KtConstructor<*>
    }

    // << Root of the CallChain >>
    fun importCandidatesPlusAlias(analysisCtx: QAnalysisContext): List<String> = qCacheIt(key.value) {
        val plusAlias = mutableListOf<String>()
        plusAlias += importCandidates

        for (imp in importCandidates) {
            var alias = analysisCtx.realNameToAliasName[imp]
            if (alias == null) {
                // get alias by simple name
                alias = analysisCtx.realNameToAliasName[imp.substringAfterLast(".")]
            }

            if (alias != null) {
                plusAlias += alias
            }
        }

        plusAlias
    }

    // << Root of the CallChain >>
    fun isPrimaryConstructorCall(): Boolean {
        to?.qDebug()
        return to is KtPrimaryConstructor
    }
}

// << Root of the CallChain >>
class QRefTarget(val srcElement: SourceElement, val descriptor: DeclarationDescriptor)

// << Root of the CallChain >>
private fun QResolvedCall.qToChain(fromNode: QChainNode, analysisCtx: QAnalysisContext): QChain? {
//    if (!this.from.qIsInChainNode()) {
//        // in Import Directive
//        return null
//    }

    val visitor = analysisCtx.lib.nodeVisitor

    val resolvedEle = this.to ?: return null

    val targetNode = resolvedEle.qContainingCallChainNode(analysisCtx, true)

    if (targetNode.key == fromNode.key)
        return null

    if (analysisCtx.chainFrom.containsKey(targetNode.key) && analysisCtx.chainFrom[targetNode.key] != null) {
        // already resolved
        return null
    }

    val topLevel = targetNode.topLevel

    // for example :
    // already resolved in non-test src, and you can see this element from test code.
    // to avoid duplicate declaration.
    if (analysisCtx.chainStoppers.contains(topLevel.qElementKey)) {
        return null
    }

//    val fromNode = this.from.qContainingCallChainNode(analysisCtx, true)

    if (visitor.isMustDiscard(targetNode)) {
        return null
    }

    if (visitor.isMustMark(targetNode)) {
        return QChain(
            targetNode, fromNode,
            if (this.type == QResolvedType.ByTypeReference) {
                QChainType.ByTypeReference
            } else {
                QChainType.ByCall
            },
            false,
            analysisCtx
        )
    }

    val result = visitor.resolved(QNodeResolvedScope(targetNode, fromNode, this.type))

    return if (result == QNodeVisited.MarkThisNodeAndContinueChain) {
        QChain(
            targetNode, fromNode,
            if (this.type == QResolvedType.ByTypeReference) {
                QChainType.ByTypeReference
            } else {
                QChainType.ByCall
            },
            false,
            analysisCtx
        )
    } else {
        null
    }
}

// << Root of the CallChain >>
fun KtElement.qResolveToChain(fromNode: QChainNode, analysisCtx: QAnalysisContext): QChain? {
    val resolvedCall = this.qResolveCall(analysisCtx)

    if (resolvedCall?.isResolvedElementInUserCodeBase == false) {
        return null
    }

    return resolvedCall?.qToChain(fromNode, analysisCtx)
}

// << Root of the CallChain >>
fun KtElement.qResolveCall(
    analysisCtx: QAnalysisContext,
): QResolvedCall? {
    val topLevel = this.qOuterSrcLevelTarget(true)
    if (topLevel is KtImportList || topLevel is KtPackageDirective)
        return null

    val key = this.qElementKey

    if (analysisCtx.resolvedCalls.containsKey(key)) {
        return analysisCtx.resolvedCalls[key]
    }

    if (analysisCtx.nonResolvedCalls.containsKey(key)) {
        return null
    }

    val ana = analysisCtx.analysis

    val binCtx = ana.bindingContext

    val call = this.getCall(binCtx)

    val resolvedCall = call?.getResolvedCall(binCtx)

    var resolvedDesc: DeclarationDescriptor?
    var srcEle: SourceElement?
    var resolvedType: QResolvedType

    if (resolvedCall != null) {
        resolvedDesc = resolvedCall.candidateDescriptor
        srcEle = resolvedCall.candidateDescriptor.source

        resolvedType = QResolvedType.ByFunctionCall

        if( srcEle !is KotlinSourceElement ) {
            if( resolvedDesc is FunctionInvokeDescriptor  ) {
                // ex.
                // reference to -->
                // var myFunc: (String) -> Boolean = { it.endsWith(".kt") }
                //
                // In this case, we need to a chain to myFunc, not Function1.invoke

                val resolvedRef = this.qResolveReference(analysisCtx)
                resolvedDesc = resolvedRef?.descriptor
                srcEle = resolvedRef?.srcElement
                resolvedType = QResolvedType.ByTypeReference
            }

        }
    } else {
        val resolvedRef = this.qResolveReference(analysisCtx)
        resolvedDesc = resolvedRef?.descriptor
        srcEle = resolvedRef?.srcElement
        resolvedType = QResolvedType.ByTypeReference

        val alias = analysisCtx.allAliases[this.text]

        if (alias != null && resolvedDesc != null && srcEle != null) {
            val isReallyAlias = analysisCtx.realNameToAliasName.entries.find {
                it.value == alias.name
            }?.key == (srcEle.getPsi() as KtElement).name.toString()

            if( isReallyAlias ) {
                srcEle = alias.toSourceElement()

                val importCandidates = mutableListOf<String>()
                importCandidates += alias.fqName.toString()

                if (!analysisCtx.lib.strictImportCheck) {
                    if (!alias.name!!.startsWith("<")) {
                        importCandidates += alias.name!!
                    }

                    val qResolvedCall = QResolvedCall(
                        key, this, alias,
                        null, "Alias",
                        QResolvedType.ByTypeAlias,
                        analysisCtx, importCandidates
                    )

                    analysisCtx.resolvedCalls[key] = qResolvedCall

                    return qResolvedCall
                }
            }
        }
    }

    if (srcEle != null && resolvedDesc != null) {
        val importDirectiveCandidates = mutableListOf<String>()

        val fqName = resolvedDesc.fqNameUnsafe.toString()

        // ex. kotlin.String.plus
        importDirectiveCandidates += fqName

        val top = resolvedDesc.getTopLevelContainingClassifier()
        if (top != null) {
            val fqNameTop = top.fqNameUnsafe.toString()
            // ex. kotlin.String
            importDirectiveCandidates += fqNameTop
        }

//        if (!analysisCtx.lib.strictImportCheck) {
//            val name = resolvedDesc.name.toString()
//            if (!name.startsWith("<")) {
//                // plus
//                // String
//                importDirectiveCandidates += name
//            }
//        }

        return if (srcEle is KotlinSourceElement) {
            val resolvedKtEle = srcEle.psi

            val qResolvedCall = QResolvedCall(
                key, this, resolvedKtEle, resolvedDesc,
                "Resolved easily, srcEle is KotlinSourceElement",
                resolvedType, analysisCtx, importDirectiveCandidates
            )
            analysisCtx.resolvedCalls[key] = qResolvedCall

            qResolvedCall
        } else {
            val qResolvedCall = QResolvedCall(
                key, this, null, resolvedDesc,
                "Resolved easily, to is null, srcEle is not KotlinSourceElement",
                resolvedType, analysisCtx, importDirectiveCandidates
            )
            analysisCtx.resolvedCalls[key] = qResolvedCall

            qResolvedCall
        }
    }

    val declaration = this.containingNonLocalDeclaration()
    if (this is KtUserType && declaration != null) {
        val resolvedDescriptor =
            ana.typeResolver.resolveClass(qLexicalScope(ana, declaration), this, ana.bindingTrace, true)

        val srcEleDecl = resolvedDescriptor?.source

        if (srcEleDecl != null && srcEleDecl is KotlinSourceElement) {
            val resolvedKtEle = srcEleDecl.psi

            val qResolvedCall = QResolvedCall(
                key,
                this,
                resolvedKtEle,
                resolvedDescriptor,
                resolvedLineHint = "typeResolver.resolveClass",
                QResolvedType.ByTypeReference,
                analysisCtx,
                listOf(resolvedDescriptor.fqNameUnsafe.toString())
            )
            analysisCtx.resolvedCalls[key] = qResolvedCall

            return qResolvedCall
        }
    }

    if (this is KtTypeReference || this is KtUserType || this is KtNameReferenceExpression) {
        val importDirective: String? = this.containingKtFile.qImportPathList().find {
            this.text == it.substringAfterLast('.')
        }

        if (importDirective != null) {
            val qResolvedCall =
                QResolvedCall(
                    key, this, null, null,
                    "Last resolve, to is null, KtTypeReference && KtUserType && KtNamedReferenceExpression",
                    QResolvedType.ByTypeReference, analysisCtx, listOf(importDirective)
                )
            analysisCtx.resolvedCalls[key] = qResolvedCall

            return qResolvedCall
        }
    }

    analysisCtx.nonResolvedCalls[key] = QNonResolvedCall(key, this)

    return null
}

// << Root of the CallChain >>
private fun KtElement.qResolveReference(analysisCtx: QAnalysisContext): QRefTarget? {
    if (this !is KtNameReferenceExpression)
        return null

    val descriptor = this.getReferenceTargets(analysisCtx.analysis.bindingContext).firstOrNull()?.original

    val refTarget = descriptor?.toSourceElement

    return if (refTarget != null) {
        QRefTarget(refTarget, descriptor)
    } else {
        null
    }
}

// << Root of the CallChain >>
private fun qLexicalScope(ana: QCompactLibAnalysis, declaration: KtElement): LexicalScope =
    ana.resolveSession.declarationScopeProvider.getResolutionScopeForDeclaration(declaration)