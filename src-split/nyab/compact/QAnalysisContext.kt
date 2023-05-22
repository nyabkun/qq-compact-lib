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
import nyab.util.QMapCount
import nyab.util.qCacheIt
import nyab.util.qUnreachable
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFileAnnotationList
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QAnalysisContext(
    val lib: QCompactLib,
    val isTest: Boolean,
    val chainStoppers: Set<QKtElementKey>,
) {
    // << Root of the CallChain >>
    val phase = QPhase(QAnalysisPhase::class)

    // << Root of the CallChain >>
    val analysis = QCompactLibAnalysis(this, lib, isTest)

    // << Root of the CallChain >>
    val ktFiles = analysis.ktFiles

    // << Root of the CallChain >>
    internal val nodeHitCount: QMapCount<QChainNode> = QMapCount()

    // << Root of the CallChain >>
    fun nodeHitCount(limit: Int = 10): String {
        return nodeHitCount.ranking(limit)
    }

    // << Root of the CallChain >>
    val allAliases: Map<String, KtTypeAlias> = ktFiles.flatMap {
        it.collectDescendantsOfType<KtTypeAlias>()
    }.associateBy { it.name!! }

    // << Root of the CallChain >>
    val realNameToAliasName: MutableMap<String, String> = mutableMapOf()

    // << Root of the CallChain >>
    lateinit var singleSrcBase: QSingleSrcBase

    // << Root of the CallChain >>
    var dependencyLines = mutableListOf<String>()

    // << Root of the CallChain >>
    val callChainNodes: MutableMap<QKtElementKey, QChainNode> = mutableMapOf()

    // << Root of the CallChain >>
    val destSrcFile: Path = lib.destProjDir.resolve(
        if (isTest) lib.destMainSrcFileName else lib.destTestSrcFileName
    )

    // << Root of the CallChain >>
    val resolvedCalls: MutableMap<QKtElementKey, QResolvedCall> = mutableMapOf()

    // << Root of the CallChain >>
    val nonResolvedCalls: MutableMap<QKtElementKey, QNonResolvedCall> = mutableMapOf()

    // << Root of the CallChain >>
    val chainFrom: MutableMap<QKtElementKey, QChain?> = mutableMapOf()

    // << Root of the CallChain >>
    val resolvedChains: MutableMap<QKtElementKey, List<QChain>> = mutableMapOf()

    // << Root of the CallChain >>
    val rootOfChainNode: MutableMap<QKtElementKey, QChainNode> = mutableMapOf()

    // << Root of the CallChain >>
    val markedNodes: MutableMap<QKtElementKey, QChainNode> = mutableMapOf()

    // << Root of the CallChain >>
    val containingCompactElements: MutableMap<QKtElementKey, QTopLevelCompactElement> = mutableMapOf()

//    lateinit var allImportDirectiveCandidates: List<String>

    // << Root of the CallChain >>
    lateinit var rootKtFiles: List<KtFile>

    // << Root of the CallChain >>
    lateinit var fileAnnotationLists: List<KtFileAnnotationList>

    // << Root of the CallChain >>
    lateinit var markedTopLevelCompactElements: List<QTopLevelCompactElement>

    // << Root of the CallChain >>
    lateinit var splitFiles: Map<String, QSplitFile>

    // << Root of the CallChain >>
    init {
        val regex = Regex("typealias\\s+(\\S+?)\\s*=\\s*(\\S+)")

        allAliases.values.forEach {
            val found = regex.find(it.text)

            if (found != null) {
//                val pkg = it.containingKtFile.packageFqName.toString()
                val alias = found.groups[1]!!.value
                val realType = found.groups[2]!!.value

                realNameToAliasName[realType] = alias.substringAfterLast('.')
            } else {
                qUnreachable(it.text)
            }
        }

        for(ktFile in ktFiles) {
            for(topLevel in ktFile.children) {

            }
        }
    }

    // << Root of the CallChain >>
    fun splitFileImportCandidates(ktFile: KtFile): List<String> =
        qCacheIt("splitFileImportCandidates:" + ktFile.virtualFilePath + ":" + isTest + ":" + hashCode()) {
            phase.checkFinished(QAnalysisPhase.CallChainAnalysisFinalWork)

            resolvedCalls.values.filter {
                if (it.isResolvedElementInUserCodeBase) {
                    it.from.containingKtFile.virtualFilePath == ktFile.virtualFilePath && it.to?.containingKtFile?.virtualFilePath != ktFile.virtualFilePath
                } else {
                    true
                }
            }.flatMap {
                it.importCandidatesPlusAlias(this)
            }.distinct()
        }

    // << Root of the CallChain >>
    fun singleFileImportCandidates(): List<String> =
        qCacheIt("singleFileImportCandidates:" + isTest + ":" + hashCode()) {
            phase.checkFinished(QAnalysisPhase.CallChainAnalysisFinalWork)

            resolvedCalls.values.filter {
                // The user code is all included in the same file, so import statements are not necessary.
                !it.isResolvedElementInUserCodeBase
            }.flatMap {
                it.importCandidatesPlusAlias(this)
            }.distinct()
        }

    // << Root of the CallChain >>
    private fun initSplitFiles() {
        val files = mutableMapOf<String, QSplitFile>()

        for (compactEle in containingCompactElements.values) {
            var splitFile = files[compactEle.ktFile.virtualFilePath]

            if (splitFile == null) {
                splitFile = QSplitFile(compactEle.ktFile, analysisCtx = this)
                files[compactEle.ktFile.virtualFilePath] = splitFile
            }

            splitFile.addElement(compactEle)
        }

        splitFiles = files
    }

    // << Root of the CallChain >>
    fun doCompilerAnalysis() {
        analysis.doAnalyse()
    }

    // << Root of the CallChain >>
    fun finishCallChainAnalysis() {

//        allImportDirectiveCandidates = resolvedCalls.values.filter { !it.isInUserCodeBase }.flatMap {
//            it.importCandidates
//        }

        fileAnnotationLists = markedNodes.mapNotNull { node ->
            if (node is KtFileAnnotationList) {
                node
            } else {
                null
            }
        }

        markedTopLevelCompactElements = markedNodes.map { node ->
            node.value.topLevel.qContainingTopLevelCompactElement(this)
        }.distinctBy { it.topLevelElement.qElementKey }.filter {
            it.topLevelElement !is KtImportList &&
                    it.topLevelElement !is KtImportDirective && it.topLevelElement !is KtPackageDirective && it.topLevelElement !is KtFileAnnotationList
        }.sortedBy {
            it.isChainRoot()
        }.sortedBy {
            it.topLevelElement.textOffset
        }.sortedBy {
            it.filePath
        }

        for(node in markedNodes.values) {
            for( chainedNode in node.chainNodesFromRoot() ) {
                if( chainedNode == node )
                    continue

                nodeHitCount += chainedNode
            }
        }

        initSplitFiles()
    }

//    val visitedCompactElements: MutableList<QTopLevelCompactElement> = mutableListOf()
}