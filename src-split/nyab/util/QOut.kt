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

import java.nio.charset.Charset
import java.nio.file.Path

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=8] = QOut <-[Ref]- QLogStyle <-[Ref]- QLogStyle.SRC_AND_STACK <-[Call]- QException ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal interface QOut {
    // CallChain[size=10] = QOut.isAcceptColoredText <-[Propag]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Ca ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val isAcceptColoredText: Boolean

    // CallChain[size=10] = QOut.print() <-[Propag]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogSty ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun print(msg: Any? = "")

    // CallChain[size=10] = QOut.println() <-[Propag]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogS ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun println(msg: Any? = "")

    // CallChain[size=10] = QOut.close() <-[Propag]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogSty ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun close()

    companion object {
        // CallChain[size=6] = QOut.NONE <-[Call]- String.qRunInShell() <-[Call]- QGit.String.runCmd() <-[Ca ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
        val NONE: QOut = QOutIgnore

        // CallChain[size=9] = QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogStyle <-[Ref]- QLogStyle.SRC_ ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        val CONSOLE: QOut = QConsole(true)

        
    }
}

// CallChain[size=10] = QConsole <-[Call]- QOut.CONSOLE <-[Call]- QMyLog.out <-[Call]- QLogStyle <-[ ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private class QConsole(override val isAcceptColoredText: Boolean) : QOut {
    // CallChain[size=11] = QConsole.print() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QMyLo ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun print(msg: Any?) {
        if (isAcceptColoredText) {
            kotlin.io.print(msg.toString())
        } else {
            kotlin.io.print(msg.toString().noColor)
        }
    }

    // CallChain[size=11] = QConsole.println() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QMy ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun println(msg: Any?) {
        kotlin.io.println(msg.toString())
    }

    // CallChain[size=11] = QConsole.close() <-[Propag]- QConsole <-[Call]- QOut.CONSOLE <-[Call]- QMyLo ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun close() {
        // Do nothing
    }
}

// CallChain[size=7] = QOutIgnore <-[Call]- QOut.NONE <-[Call]- String.qRunInShell() <-[Call]- QGit. ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
private object QOutIgnore : QOut {
    // CallChain[size=8] = QOutIgnore.isAcceptColoredText <-[Propag]- QOutIgnore <-[Call]- QOut.NONE <-[ ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    override val isAcceptColoredText: Boolean = true

    // CallChain[size=8] = QOutIgnore.print() <-[Propag]- QOutIgnore <-[Call]- QOut.NONE <-[Call]- Strin ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    override fun print(msg: Any?) {
    }

    // CallChain[size=8] = QOutIgnore.println() <-[Propag]- QOutIgnore <-[Call]- QOut.NONE <-[Call]- Str ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    override fun println(msg: Any?) {
    }

    // CallChain[size=8] = QOutIgnore.close() <-[Propag]- QOutIgnore <-[Call]- QOut.NONE <-[Call]- Strin ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    override fun close() {
        // Do nothing
    }
}