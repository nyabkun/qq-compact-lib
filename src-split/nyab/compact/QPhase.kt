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

import kotlin.reflect.KClass
import nyab.conf.QE
import nyab.util.qEnumValues
import nyab.util.throwItBrackets

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QPhase<E : Enum<E>>(cls: KClass<E>) {
    // << Root of the CallChain >>
    val phases = cls.qEnumValues()
    // << Root of the CallChain >>
    var current = phases[0]

    // << Root of the CallChain >>
    fun nextPhase(next: E) {
        if (current.ordinal != next.ordinal - 1) {
            QE.InvalidPhaseTransition.throwItBrackets(
                "Current", current.name, "CurrentOrdinal", current.ordinal,
                "Next", next.name, "NextOrdinal", next.ordinal
            )
        }

        current = next
    }

    // << Root of the CallChain >>
    fun isFinished(phase: E): Boolean {
        return phase.ordinal < current.ordinal
    }

    // << Root of the CallChain >>
    fun isFinishedOrWorking(phase: E): Boolean {
        return phase.ordinal < current.ordinal
    }

    // << Root of the CallChain >>
    fun checkFinished(phase: E) {
        check(isFinished(phase))
    }

    // << Root of the CallChain >>
    fun checkFinishedOrWorking(phase: E) {
        check(isFinishedOrWorking(phase))
    }

    // << Root of the CallChain >>
    fun checkNotFinished(phase: E) {
        check(!isFinished(phase))
    }

    // << Root of the CallChain >>
    fun isNotFinished(phase: E): Boolean {
        return !isFinished(phase)
    }
}