/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("NOTHING_TO_INLINE")

package nyab.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=7] = qHOUR <-[Call]- QBackupSlot.hour() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[Cal ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal const val qHOUR = 3_600_000L

// CallChain[size=8] = qDAY <-[Call]- qYEAR <-[Call]- QBackupSlot.year() <-[Call]- QBackupSlot.DEFAU ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal const val qDAY = 86_400_000L

// CallChain[size=7] = qWEEK <-[Call]- QBackupSlot.week() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[Cal ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal const val qWEEK = qDAY * 7

// CallChain[size=7] = qMONTH <-[Call]- QBackupSlot.month() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[C ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal const val qMONTH = qDAY * 30

// CallChain[size=7] = qYEAR <-[Call]- QBackupSlot.year() <-[Call]- QBackupSlot.DEFAULT_SLOTS <-[Cal ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal const val qYEAR = qDAY * 365

// CallChain[size=9] = qNow <-[Call]- qCacheItTimedThreadLocal() <-[Call]- qCacheItOneSecThreadLocal ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal val qNow: Long
    get() = System.currentTimeMillis()

// CallChain[size=2] = qToday() <-[Call]- QNextVersionScope.default()[Root]
/**
 * Unix timestamp (Long Value).
 */
internal fun qToday(): Long {
    return qDayStart(System.currentTimeMillis(), zoneId = ZoneId.systemDefault())
}

// CallChain[size=3] = qDayStart() <-[Call]- qToday() <-[Call]- QNextVersionScope.default()[Root]
/**
 * Unix timestamp (Long Value).
 */
internal fun qDayStart(time: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
    val ld: LocalDate = Instant.ofEpochMilli(time).atZone(zoneId).toLocalDate()
    return ld.atStartOfDay(zoneId).toInstant().toEpochMilli()
}