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
import nyab.conf.QMyMark
import nyab.util.QOut
import nyab.util.QShColor
import nyab.util.QUnit
import nyab.util.light_gray
import nyab.util.qColor
import nyab.util.qEnumValues
import nyab.util.qFormatDuration
import nyab.util.throwItBrackets

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QPhase<E : Enum<E>>(cls: KClass<E>,
                          val label: String = cls.simpleName!!,
                          val out: QOut = QOut.CONSOLE,
                          val fg: QShColor = QShColor.Blue,
                          val bg: QShColor? = null,
                          val longTaskBorderMillis: Long = 1000) {
    // << Root of the CallChain >>
    val phases = cls.qEnumValues()
    // << Root of the CallChain >>
    var current = phases[0]
    // << Root of the CallChain >>
    var currentPhaseStartTime = System.currentTimeMillis()
    // << Root of the CallChain >>
    val time: MutableMap<E, Long> = mutableMapOf()

    // << Root of the CallChain >>
    init {
        printPhaseTime()
    }

    // << Root of the CallChain >>
    private fun printPhaseTime() {
        if( current.ordinal == 0 )
            return

        val time = System.currentTimeMillis() - currentPhaseStartTime
        this.time[current] = time

        val isLongTask = time > longTaskBorderMillis

        val timeStr = time.qFormatDuration(QUnit.Milli).trim()

        val mark = if( isLongTask ) QMyMark.phase_start_long_task else QMyMark.phase_start

        out.println("$mark ${label}: ${current.name.qColor(fg = fg, bg = bg)}\n${"...".light_gray}  $timeStr")
    }

    // << Root of the CallChain >>
    fun nextPhase(next: E) {
        if (current.ordinal != next.ordinal - 1) {
            QE.InvalidPhaseTransition.throwItBrackets(
                "Current", current.name, "CurrentOrdinal", current.ordinal,
                "Next", next.name, "NextOrdinal", next.ordinal
            )
        }

        printPhaseTime()

        current = next
        currentPhaseStartTime = System.currentTimeMillis()
    }

    // << Root of the CallChain >>
    fun isFinished(phase: E): Boolean {
        return phase.ordinal < current.ordinal
    }

    // << Root of the CallChain >>
    fun isNotFinished(phase: E): Boolean {
        return !isFinished(phase)
    }

    // << Root of the CallChain >>
    fun isFinishedOrWorkingOn(phase: E): Boolean {
        return phase.ordinal <= current.ordinal
    }

    // << Root of the CallChain >>
    fun checkFinished(phase: E) {
        check(isFinished(phase))
    }

    // << Root of the CallChain >>
    fun checkFinishedOrWorkingOn(phase: E) {
        check(isFinishedOrWorkingOn(phase))
    }

    // << Root of the CallChain >>
    fun checkNotFinished(phase: E) {
        check(!isFinished(phase))
    }
}