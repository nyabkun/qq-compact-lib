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

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = QMapCount <-[Ref]- QAnalysisContext.nodeHitCount[Root]
internal class QMapCount<K>(private val map: MutableMap<K, Int> = mutableMapOf()) : MutableMap<K, Int> by map {
    // CallChain[size=3] = QMapCount.get() <-[Propag]- QMapCount.plusAssign() <-[Call]- QAnalysisContext.finishCallChainAnalysis()[Root]
    override operator fun get(key: K): Int {
        return map.getOrDefault(key, 0)
    }

    // CallChain[size=2] = QMapCount.plusAssign() <-[Call]- QAnalysisContext.finishCallChainAnalysis()[Root]
    operator fun plusAssign(key: K) {
        incrementCount(key, 1)
    }

    // CallChain[size=3] = QMapCount.incrementCount() <-[Call]- QMapCount.plusAssign() <-[Call]- QAnalysisContext.finishCallChainAnalysis()[Root]
    fun incrementCount(key: K, value: Int = 1): Int {
        val count = map.getOrDefault(key, 0) + value
        map[key] = count
        return count
    }

    // CallChain[size=2] = QMapCount.ranking() <-[Call]- QAnalysisContext.nodeHitCount()[Root]
    fun ranking(limit: Int = 10, keyToString: (K) -> String = { it.toString() }): String {
        if( this.map.values.isEmpty() ) {
            return ""
        }

        val nValMaxLen = this.map.values.maxOf { it.toString().length }
        val valueFormat = "%${nValMaxLen}d"

        val nKeyMaxLen = this.map.keys.maxOf { keyToString(it).length }
        val keyFormat = "%${nKeyMaxLen + 8}s"

        return this.map.entries.sortedBy { -it.value }.take(limit).joinToString("\n") {
            String.format("$keyFormat : $valueFormat", keyToString(it.key).blue, it.value)
        }.trimIndent()
    }
}