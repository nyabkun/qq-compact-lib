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
import nyab.conf.QE

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=7] = String.qEscapeString() <-[Call]- String.qQuoteArg() <-[Call]- List<String>.qJ ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun String.qEscapeString(vararg withEscapeToWithoutEscape: Pair<String, String>): String {
    return QStringEscape(*withEscapeToWithoutEscape).escape(this)
}

// CallChain[size=8] = String.qUnescapeString() <-[Call]- String.qUnquoteArg() <-[Call]- String.qIsS ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun String.qUnescapeString(vararg withEscapeToWithoutEscape: Pair<String, String>): String {
    return QStringEscape(*withEscapeToWithoutEscape).unescape(this)
}

// CallChain[size=8] = QStringEscape <-[Call]- String.qEscapeString() <-[Call]- String.qQuoteArg() < ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal class QStringEscape(vararg withEscapeToWithoutEscape: Pair<String, String>) {
    // CallChain[size=10] = QStringEscape.commonEscapeChar <-[Call]- QStringEscape.init { <-[Propag]- QS ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
    private val commonEscapeChar: Char?
    // CallChain[size=9] = QStringEscape.mapWithEscapeToWithoutEscape <-[Call]- QStringEscape.escape() < ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
    private val mapWithEscapeToWithoutEscape: MutableMap<CharArray, CharArray> = mutableMapOf()

    // CallChain[size=9] = QStringEscape.init { <-[Propag]- QStringEscape.escape() <-[Call]- String.qEsc ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
    init {
        for ((withEscape, withoutEscape) in withEscapeToWithoutEscape) {
            mapWithEscapeToWithoutEscape += withEscape.toCharArray() to withoutEscape.toCharArray()
        }

        val firstChars = mapWithEscapeToWithoutEscape.mapKeys { (seq, _) -> seq[0] }.keys.distinct()
        commonEscapeChar = if (firstChars.size == 1) {
            firstChars[0]
        } else {
            null
        }
    }

    // CallChain[size=8] = QStringEscape.escape() <-[Call]- String.qEscapeString() <-[Call]- String.qQuo ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
    fun escape(text: String): String {
        val reader = QSequenceReader(text)
        val sb = StringBuilder()

        while (reader.hasNextChar()) {
            var interpretDetected = false

            for ((seq, interpret) in mapWithEscapeToWithoutEscape) {
                if (reader.detectSequence(interpret)) {
                    sb.append(seq)

                    interpretDetected = true
                    break
                }
            }

            if (!interpretDetected) {
                sb.append(reader.nextChar())
            }
        }

        return sb.toString()
    }

    // CallChain[size=9] = QStringEscape.unescape() <-[Call]- String.qUnescapeString() <-[Call]- String. ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
    fun unescape(text: String): String {
        val reader = QSequenceReader(text)
        val sb = StringBuilder()

        while (reader.hasNextChar()) {
            var sequenceDetected = false

            for ((seq, interpret) in mapWithEscapeToWithoutEscape) {

                // Short Path : if all sequences have the same first char = escapeChar.
                if (commonEscapeChar != null && reader.peekNextChar() != commonEscapeChar) {
                    break
                }

                if (reader.detectSequence(seq)) {
                    sb.append(interpret)
                    sequenceDetected = true
                    break
                }
            }

            if (sequenceDetected) {
                continue
            }

            sb.append(reader.nextChar())
        }

        return sb.toString()
    }
}