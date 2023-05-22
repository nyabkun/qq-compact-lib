/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("FunctionName")

package nyab.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=10] = QZone <-[Ref]- String.qParseDateTime() <-[Call]- Path.qDateTime() <-[Call]-  ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal enum class QZone(val zoneId: ZoneId) {
    // CallChain[size=10] = QZone.DEFAULT <-[Call]- String.qParseDateTime() <-[Call]- Path.qDateTime() < ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    DEFAULT(ZoneOffset.systemDefault()),
    // CallChain[size=11] = QZone.UTC <-[Propag]- QZone.DEFAULT <-[Call]- String.qParseDateTime() <-[Cal ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    UTC(ZoneOffset.UTC);

    // CallChain[size=12] = QZone.offset() <-[Call]- QLocalDateTimeFormat.parseToEpochMilli() <-[Call]-  ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    fun offset(epochTimeMilli: Long = qNow): ZoneOffset {
        return zoneId.rules.getOffset(Instant.ofEpochMilli(epochTimeMilli))
    }
}

// CallChain[size=11] = QLocalDateTimeFormat <-[Ref]- _qParseDateTime() <-[Call]- String.qParseDateT ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal enum class QLocalDateTimeFormat(val format: String) {
    // CallChain[size=11] = QLocalDateTimeFormat.DateTime <-[Ref]- _qParseDateTime() <-[Call]- String.qP ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    DateTime("yyyy-MM-dd'T'HH-mm-ss"),
    // CallChain[size=12] = QLocalDateTimeFormat.Date <-[Call]- QLocalDateTimeFormat.parseToEpochMilli() ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    Date("yyyy-MM-dd"),
    // CallChain[size=12] = QLocalDateTimeFormat.Time <-[Call]- QLocalDateTimeFormat.parseToEpochMilli() ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    Time("HH:mm");

    // CallChain[size=13] = QLocalDateTimeFormat.dtfUTC <-[Call]- QLocalDateTimeFormat.dtf() <-[Call]- Q ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    private val dtfUTC by lazy { DateTimeFormatter.ofPattern(format).withZone(ZoneOffset.UTC) }
    // CallChain[size=13] = QLocalDateTimeFormat.dtfDefault <-[Call]- QLocalDateTimeFormat.dtf() <-[Call ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    private val dtfDefault by lazy { DateTimeFormatter.ofPattern(format).withZone(ZoneOffset.systemDefault()) }

    // CallChain[size=12] = QLocalDateTimeFormat.dtf() <-[Call]- QLocalDateTimeFormat.parseToEpochMilli( ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    private fun dtf(zone: QZone): DateTimeFormatter {
        return when (zone) {
            QZone.DEFAULT -> dtfDefault
            QZone.UTC -> dtfUTC
        }
    }

    // CallChain[size=11] = QLocalDateTimeFormat.format() <-[Call]- Long.qFormatDateTime() <-[Call]- Pat ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    fun format(epochTimeMillis: Long, zone: QZone = QZone.DEFAULT): String {
        return dtf(zone).format(Instant.ofEpochMilli(epochTimeMillis))
    }

    // CallChain[size=11] = QLocalDateTimeFormat.parseToEpochMilli() <-[Call]- _qParseDateTime() <-[Call ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    fun parseToEpochMilli(text: String, zone: QZone = QZone.DEFAULT): Long {
        val dtf = dtf(zone)
        return when (this) {
            DateTime ->
                LocalDateTime.parse(text, dtf).toInstant(zone.offset()).toEpochMilli()
            Date ->
                LocalDate.parse(text, dtf).atStartOfDay(zone.zoneId).toInstant().toEpochMilli()
            Time ->
                LocalTime.parse(text, dtf).toEpochSecond(LocalDate.now(), zone.offset()) * 1000L
        }
    }
}

// CallChain[size=9] = String.qParseDateTime() <-[Call]- Path.qDateTime() <-[Call]- QBackupFile.back ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun String.qParseDateTime(): Long {
    return _qParseDateTime(this, QZone.DEFAULT)
}

// CallChain[size=10] = _qParseDateTime() <-[Call]- String.qParseDateTime() <-[Call]- Path.qDateTime ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
private fun _qParseDateTime(text: String, zone: QZone = QZone.DEFAULT): Long {
    val dateStr = if (text.length == "0000-00-00T00-00-00".length) {
        text
    } else {
        """.*(?<!\d)(\d{4}-\d{2}-\d{2}T\d{2}-\d{2}-\d{2})(?!\d).*""".re
            .replaceFirst(text, "$1")
    }

    return QLocalDateTimeFormat.DateTime.parseToEpochMilli(dateStr, zone)
}

// CallChain[size=3] = String.qParseDate() <-[Call]- QDateVersion.parse() <-[Call]- QNextVersionScope.default()[Root]
internal fun String.qParseDate(): Long {
    return _qParseDate(this, QZone.DEFAULT)
}

// CallChain[size=4] = _qParseDate() <-[Call]- String.qParseDate() <-[Call]- QDateVersion.parse() <-[Call]- QNextVersionScope.default()[Root]
private fun _qParseDate(text: String, zone: QZone = QZone.DEFAULT): Long {
    val timeStr = """.*(?<!\d)(\d{4}-\d{2}-\d{2})(?!\d).*""".re.replaceFirst(text, "$1")
    return QLocalDateTimeFormat.Date.parseToEpochMilli(timeStr, zone)
}

// CallChain[size=10] = Long.qFormatDateTime() <-[Call]- Path.qWithDateTime() <-[Call]- QBackupHelpe ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun Long.qFormatDateTime(): String {
    return QLocalDateTimeFormat.DateTime.format(this, QZone.DEFAULT)
}

// CallChain[size=5] = QUnit <-[Ref]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
internal enum class QUnit {
    // CallChain[size=5] = QUnit.Nano <-[Call]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
    Nano,
    // CallChain[size=5] = QUnit.Micro <-[Call]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
    Micro,
    // CallChain[size=5] = QUnit.Milli <-[Call]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
    Milli,
    // CallChain[size=5] = QUnit.Second <-[Call]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
    Second,
    // CallChain[size=5] = QUnit.Minute <-[Call]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
    Minute,
    // CallChain[size=5] = QUnit.Hour <-[Call]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
    Hour,
    // CallChain[size=5] = QUnit.Day <-[Call]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
    Day
}

// CallChain[size=4] = Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
internal fun Long.qFormatDuration(unit: QUnit = QUnit.Nano): String {
    return when (unit) {
        QUnit.Milli ->
            Duration.ofMillis(this).qFormat()
        QUnit.Micro ->
            Duration.ofNanos(this).qFormat()
        QUnit.Nano ->
            Duration.ofNanos(this).qFormat()
        QUnit.Second ->
            Duration.ofSeconds(this).qFormat()
        QUnit.Minute ->
            Duration.ofMinutes(this).qFormat()
        QUnit.Hour ->
            Duration.ofHours(this).qFormat()
        QUnit.Day ->
            Duration.ofDays(this).qFormat()
    }
}

// CallChain[size=6] = Duration.qToMicrosOnlyPart() <-[Call]- Duration.qFormat() <-[Call]- Long.qFor ... ]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
internal fun Duration.qToMicrosOnlyPart(): Int {
    return (toNanosPart() % 1_000_000) / 1_000
}

// CallChain[size=6] = Duration.qToNanoOnlyPart() <-[Call]- Duration.qFormat() <-[Call]- Long.qForma ... ]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
internal fun Duration.qToNanoOnlyPart(): Int {
    return toNanosPart() % 1_000
}

// CallChain[size=5] = Duration.qFormat() <-[Call]- Long.qFormatDuration() <-[Call]- QTimeItResult.toString() <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
internal fun Duration.qFormat(detail: Boolean = false): String {
    val du = abs()

    val maxUnit: QUnit = du.let {
        when {
            it < Duration.ofNanos(1000) -> QUnit.Nano
            it < Duration.ofMillis(1) -> QUnit.Micro
            it < Duration.ofSeconds(1) -> QUnit.Milli
            it < Duration.ofMinutes(1) -> QUnit.Second
            it < Duration.ofHours(1) -> QUnit.Minute
            it < Duration.ofDays(1) -> QUnit.Hour
            else -> QUnit.Day
        }
    }

    val parts = mutableListOf<String>()
    when (maxUnit) {
        QUnit.Nano -> {
            parts.add(String.format("%3d ns", du.toNanosPart()))
        }
        QUnit.Micro -> {
            parts.add(String.format("%3d μs", du.qToMicrosOnlyPart()))

            if (du.qToMicrosOnlyPart() <= 3 || detail)
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
        }
        QUnit.Milli -> {
            parts.add(String.format("%3d ms", du.toMillisPart()))

            if (du.toMillisPart() <= 3 || detail)
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
        }
        QUnit.Second -> {
            parts.add(String.format("%2d sec", du.toSecondsPart()))
            parts.add(String.format("%03d ms", du.toMillisPart()))

            if (detail) {
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
            }
        }
        QUnit.Minute -> {
            parts.add(String.format("%2d min", du.toMinutesPart()))
            parts.add(String.format("%02d sec", du.toSecondsPart()))
            if (detail) {
                parts.add(String.format("%03d ms", du.toMillisPart()))
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
            }
        }
        QUnit.Hour -> {
            parts.add(String.format("%2d hour", du.toHoursPart()))
            parts.add(String.format("%02d min", du.toMinutesPart()))
            if (detail) {
                parts.add(String.format("%02d sec", du.toSecondsPart()))
                parts.add(String.format("%03d ms", du.toMillisPart()))
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
            }
        }
        QUnit.Day -> {
            parts.add(String.format("%2d day", du.toDaysPart()))
            parts.add(String.format("%02d hour", du.toHoursPart()))
            if (detail) {
                parts.add(String.format("%02d min", du.toMinutesPart()))
                parts.add(String.format("%02d sec", du.toSecondsPart()))
                parts.add(String.format("%03d ms", du.toMillisPart()))
                parts.add(String.format("%03d μs", du.qToMicrosOnlyPart()))
                parts.add(String.format("%03d ns", du.qToNanoOnlyPart()))
            }
        }
    }

    return parts.joinToString(" ")
}