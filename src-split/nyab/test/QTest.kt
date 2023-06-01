/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package nyab.test

import java.lang.reflect.Method
import kotlin.reflect.KFunction
import nyab.conf.QE
import nyab.conf.QMyMark
import nyab.conf.QMyTest
import nyab.util.QException
import nyab.util.QLogStyle
import nyab.util.QOnlyIfStr
import nyab.util.QOut
import nyab.util.QShColor
import nyab.util.QSrcCut
import nyab.util.blue
import nyab.util.light_gray
import nyab.util.light_green
import nyab.util.light_yellow
import nyab.util.noStyle
import nyab.util.qBrackets
import nyab.util.qIsDebugging
import nyab.util.qIsTesting
import nyab.util.qToHex
import nyab.util.qToLogString
import nyab.util.qWithNewLinePrefix
import nyab.util.qWithNewLineSurround

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=3] = qFailMsg() <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
private fun qFailMsg(msg: String = "it is null"): String {
    val cMsg = "[$msg]".colorIt
    return "${QMyMark.warn} $cMsg"
}

// CallChain[size=3] = qFailMsg() <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
private fun qFailMsg(actual: Any?, msg: String = "is not equals to", expected: Any?): String {
    val cMsg = "[$msg]".colorIt
    val actualStr = actual.qToLogString() + " " + "(actual)".light_green
    val expectedStr = expected.qToLogString() + " " + "(expected)".blue
    return "${QMyMark.warn} ${actualStr.qWithNewLineSurround(onlyIf = QOnlyIfStr.Always)}$cMsg${
        expectedStr.qWithNewLinePrefix(onlyIf = QOnlyIfStr.Always)
    }"
}

// CallChain[size=4] = String.colorIt <-[Call]- qFailMsg() <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
private val String.colorIt: String
    get() = this.light_yellow

// CallChain[size=3] = qThrowIt() <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
private fun qThrowIt(msg: String, exception: QE) {
    throw QException(exception, msg, null, stackDepth = 2, srcCut = QSrcCut.MULTILINE_INFIX_NOCUT)
}

// CallChain[size=2] = Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
internal infix fun Any?.shouldBe(expected: Any?) {
    if (!qOkToTest()) return

    if (expected is Boolean && this is Boolean && this != expected) {
        if (expected) {
            val msg = qFailMsg("it is false")

            qThrowIt(msg, QE.ShouldBeTrue)
        } else {
            val msg = qFailMsg("it is true")

            qThrowIt(msg, QE.ShouldBeFalse)
        }
    }

    val thisStr = this.qToLogString()
    val expectedStr = expected.qToLogString()

    if (thisStr.trim().noStyle != expectedStr.trim().noStyle) {
        val msg = qFailMsg(thisStr, "is not equals to", expectedStr)

        val diffIdx =
            if (expectedStr.length < thisStr.length) {
                expectedStr.mapIndexedNotNull { idx, ch ->
                    if (thisStr.length > idx && thisStr[idx] != ch) {
                        idx
                    } else {
                        null
                    }
                }.firstOrNull()
            } else {
                thisStr.mapIndexedNotNull { idx, ch ->
                    if (expectedStr.length > idx && expectedStr[idx] != ch) {
                        idx
                    } else {
                        null
                    }
                }.firstOrNull()
            }

        if (diffIdx != null) {
            val expectedChar = expectedStr[diffIdx]
            val actualChar = thisStr[diffIdx]

            qThrowIt(
                msg + "\n" + qBrackets(
                    "expected char at idx=$diffIdx".blue,
                    expectedChar.toString().blue + (" ( CharCodeHex = " + expectedChar.qToHex() + " )").light_gray,
                    "actual char at idx=$diffIdx".light_green,
                    actualChar.toString().light_green + (" ( CharCodeHex = " + actualChar.qToHex() + " )").light_gray
                ),
                QE.ShouldBeEqual
            )
        } else {
            qThrowIt(msg, QE.ShouldBeEqual)
        }
    }
}

// CallChain[size=3] = qOkToTest() <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
private inline fun qOkToTest(): Boolean {
    return QMyTest.forceTestMode || qIsTesting || qIsDebugging
}