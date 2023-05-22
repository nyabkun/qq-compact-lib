/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.util.backup

import nyab.util.qDAY
import nyab.util.qHOUR
import nyab.util.qMONTH
import nyab.util.qWEEK
import nyab.util.qYEAR

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=8] = QFileInBackupSlot <-[Ref]- QBackupHelper.filesInSlot <-[Call]- QBackupHelper. ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal class QFileInBackupSlot(val slot: QBackupSlot) {
    // CallChain[size=7] = QFileInBackupSlot.backupFile <-[Call]- QBackupHelper.fillSlots() <-[Call]- QB ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    var backupFile: QBackupFile? = null

    // CallChain[size=7] = QFileInBackupSlot.isEmpty <-[Call]- QBackupHelper.fillSlots() <-[Call]- QBack ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val isEmpty: Boolean
        get() = backupFile == null
}

// CallChain[size=5] = QBackupSlot <-[Ref]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal class QBackupSlot private constructor(val periodMilli: Long, val availableOnlyIfPeriodElapsed: Boolean) {
    companion object {
        // CallChain[size=8] = QBackupSlot.oldest() <-[Call]- QBackupHelper.filesInSlot <-[Call]- QBackupHel ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
        fun oldest(): QBackupSlot {
            return QBackupSlot(Long.MAX_VALUE, false)
        }

        // CallChain[size=6] = QBackupSlot.hour() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
        fun hour(hour: Int, availableOnlyIfPeriodElapsed: Boolean = false): QBackupSlot {
            return QBackupSlot(hour * qHOUR, availableOnlyIfPeriodElapsed)
        }

        // CallChain[size=6] = QBackupSlot.day() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
        fun day(day: Int, availableOnlyIfPeriodElapsed: Boolean = false): QBackupSlot {
            return QBackupSlot(day * qDAY, availableOnlyIfPeriodElapsed)
        }

        // CallChain[size=6] = QBackupSlot.week() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
        fun week(week: Int, availableOnlyIfPeriodElapsed: Boolean = true): QBackupSlot {
            return QBackupSlot(week * qWEEK, availableOnlyIfPeriodElapsed)
        }

        // CallChain[size=6] = QBackupSlot.month() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
        fun month(month: Int, availableOnlyIfPeriodElapsed: Boolean = true): QBackupSlot {
            return QBackupSlot(month * qMONTH, availableOnlyIfPeriodElapsed)
        }

        // CallChain[size=6] = QBackupSlot.year() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
        fun year(year: Int, availableOnlyIfPeriodElapsed: Boolean = true): QBackupSlot {
            return QBackupSlot(year * qYEAR, availableOnlyIfPeriodElapsed)
        }

        // CallChain[size=5] = QBackupSlot.DEFAULT_SLOTS <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
        val DEFAULT_SLOTS: Array<QBackupSlot> by lazy {
            arrayOf(
                    hour(1),
                    day(1),
                    day(3),
                    week(1),
                    month(1),
                    month(3),
                    month(6),
                    year(1),
                    year(2),
                    year(3),
                    year(5),
                    year(7),
                    year(10),
                    year(30),
                    year(100)
            )
        }
    }
}