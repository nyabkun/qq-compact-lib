/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.util

import java.io.File
import java.nio.file.Path
import kotlin.io.path.isDirectory
import nyab.match.QM

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=5] = QPathSearchDsl <-[Call]- QPathSearchCondition <-[Ref]- QSearchPath.searchInDir() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
@DslMarker
internal annotation class QPathSearchDsl

// CallChain[size=4] = QSearchPath <-[Ref]- qPathSearch() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
@QPathSearchDsl
internal class QSearchPath(val type: QFType = QFType.File) {
    // CallChain[size=4] = QSearchPath.alreadyFound <-[Call]- QSearchPath.searchEnv() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    private var alreadyFound: Path? = null

    // CallChain[size=4] = QSearchPath.finders <-[Call]- QSearchPath.searchInDir() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    private val finders = mutableListOf<QPathFinder>()

    // CallChain[size=3] = QSearchPath.searchEnv() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    fun searchEnv(envName: String) {
        val env = System.getenv(envName)

        try {
            val found = env.path.toRealPath()

            if (alreadyFound == null)
                alreadyFound = found
        } catch (_: Exception) {
            // ignore
        }
    }

    // CallChain[size=3] = QSearchPath.searchInDir() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    fun searchInDir(targetDir: Path?, type: QFType = this.type, action: QPathSearchCondition.() -> Unit) {
        if (targetDir == null || !targetDir.isDirectory())
            return

        val cond = QPathSearchCondition(targetDir, type)
        cond.action()
        finders += cond
    }

    // CallChain[size=5] = QSearchPath.doSearch() <-[Call]- QSearchPath.qPathSearch() <-[Call]- qPathSearch() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    private fun doSearch(): Path? {
        if (alreadyFound != null)
            return alreadyFound

        for (cond in finders) {
            val found = cond.find()
            if (found != null)
                return found
        }

        return null
    }

    companion object {
        // CallChain[size=4] = QSearchPath.qPathSearch() <-[Call]- qPathSearch() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
        internal fun qPathSearch(type: QFType = QFType.File, action: QSearchPath.() -> Unit): Path? {
            val scope = QSearchPath(type)

            scope.action()

            return scope.doSearch()
        }
    }
}

// CallChain[size=3] = QFirstOrLast <-[Ref]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
internal enum class QFirstOrLast {
    // CallChain[size=4] = QFirstOrLast.FIRST <-[Propag]- QFirstOrLast.LAST <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    FIRST,
    // CallChain[size=3] = QFirstOrLast.LAST <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    LAST
}

// CallChain[size=5] = QPathFinder <-[Ref]- QSearchPath.finders <-[Call]- QSearchPath.searchInDir() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
internal interface QPathFinder {
    // CallChain[size=6] = QPathFinder.find() <-[Propag]- QPathFinder <-[Ref]- QSearchPath.finders <-[Ca ... hPath.searchInDir() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    fun find(): Path?
}

// CallChain[size=4] = QPathSearchCondition <-[Ref]- QSearchPath.searchInDir() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
@QPathSearchDsl
internal class QPathSearchCondition(val targetDir: Path, val type: QFType) : QPathFinder {
    // CallChain[size=3] = QPathSearchCondition.nameMatcher <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    var nameMatcher: QM = QM.any()
    // CallChain[size=5] = QPathSearchCondition.maxDepth <-[Call]- QPathSearchCondition.find() <-[Propag ... ndition.sortAndFind <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    var maxDepth: Int = 1
    // CallChain[size=3] = QPathSearchCondition.sortAndFind <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    var sortAndFind: QFirstOrLast = QFirstOrLast.FIRST

    // CallChain[size=4] = QPathSearchCondition.find() <-[Propag]- QPathSearchCondition.sortAndFind <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    override fun find(): Path? {
        return try {
            val pathList = targetDir.qListByMatch(nameMatcher, type, maxDepth = maxDepth).sorted()

            when (sortAndFind) {
                QFirstOrLast.FIRST ->
                    pathList.first().toRealPath()
                QFirstOrLast.LAST ->
                    pathList.last().toRealPath()
            }
        } catch (e: Exception) {
            null
        }
    }
}

// CallChain[size=3] = qPathSearch() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
internal fun qPathSearch(type: QFType = QFType.File, action: QSearchPath.() -> Unit): Path? {
    return QSearchPath.qPathSearch(type, action)
}