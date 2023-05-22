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

// CallChain[size=2] = QDateVersion <-[Call]- QNextVersionScope.default()[Root]
internal data class QDateVersion(val prefix: String, val suffix: String, val date: Long, val buildCount: Int) {
    // CallChain[size=2] = QDateVersion.text() <-[Call]- QNextVersionScope.default()[Root]
    fun text(): String {
        var version = prefix + QLocalDateTimeFormat.Date.format(date, QZone.DEFAULT)

        version = if (buildCount >= 2) {
            version + "-" + String.format("bc%02d", buildCount)
        } else {
            version
        }

        return version + suffix
    }

    // CallChain[size=3] = QDateVersion.toString() <-[Propag]- QDateVersion.text() <-[Call]- QNextVersionScope.default()[Root]
    /**
     * v2023-01-01-123456-beta
     */
    override fun toString(): String {
        return text()
    }

    companion object {
        // CallChain[size=2] = QDateVersion.parse() <-[Call]- QNextVersionScope.default()[Root]
        fun parse(version: String): QDateVersion? {
            try {
                val ptn = Regex("""^(.*?)(\d\d\d\d-\d\d-\d\d)(-bc(\d\d))?(.*?)$""")
                val m = ptn.find(version)!!

                val prefix = m.groups[1]!!.value
                val date = m.groups[2]!!.value.qParseDate()
                val buildCount = try {
                    m.groups[4]!!.value.toInt()
                } catch (e: Exception) {
                    1
                }
                val suffix = m.groups[5]!!.value

                return QDateVersion(prefix, suffix, date, buildCount)
            } catch (e: Exception) {
                return null
            }
        }

        
    }
}