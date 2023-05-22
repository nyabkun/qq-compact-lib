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

// CallChain[size=2] = QFuncSrc <-[Ref]- QReadmeScope[Root]
data class QFuncSrc(val name: String, val entireSrc: String, val typeParam: String?, val params: String, val returnType: String?, val body: String)

// CallChain[size=2] = String.qGetTopLevelFunctionsQuickAndDirty() <-[Call]- Path?.qReadCodeExamples()[Root]
internal fun String.qGetTopLevelFunctionsQuickAndDirty(): List<QFuncSrc> {
    val maskResult = QMask.PARAMS_AND_CONTENT_OF_FUNC.apply(this)

    val matches = Regex("""(?s)(?m)^fun\s*(<.*?>)?\s*`?([^\s.]+?)`?\((.*?)\)\s*(:\s*(\S+\??)\s*)?\{(.*?)}""").findAll(maskResult.maskedStr)

    val funcs = mutableListOf<QFuncSrc>()

    for(m in matches) {
        val entireFuncSrc = maskResult.orgText.substring(m.groups[0]!!.range)
        val typeParam = if( m.groups[1] != null ) {
            val withBracket = maskResult.orgText.substring(m.groups[1]!!.range).trimIndent()
            withBracket.substring(1, withBracket.length - 1)
        } else {
            null
        }
        val name = maskResult.orgText.substring(m.groups[2]!!.range).trim()
        val params = maskResult.orgText.substring(m.groups[3]!!.range)
        val returnType = if( m.groups[5] != null ) {
            maskResult.orgText.substring(m.groups[5]!!.range).trim()
        } else {
            null
        }
        val body = maskResult.orgText.substring(m.groups[6]!!.range).trimIndent()

        funcs += QFuncSrc(name = name, entireSrc = entireFuncSrc, typeParam = typeParam, params = params, returnType = returnType, body = body)
    }

    return funcs
}