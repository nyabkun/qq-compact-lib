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

import java.util.*

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=4] = QOSType <-[Ref]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
internal enum class QOSType {
    // CallChain[size=4] = QOSType.WINDOWS <-[Call]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    WINDOWS,
    // CallChain[size=5] = QOSType.LINUX <-[Propag]- QOSType.WINDOWS <-[Call]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    LINUX,
    // CallChain[size=5] = QOSType.MAC <-[Propag]- QOSType.WINDOWS <-[Call]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    MAC,
    // CallChain[size=5] = QOSType.SOLARIS <-[Propag]- QOSType.WINDOWS <-[Call]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    SOLARIS,
    // CallChain[size=5] = QOSType.OTHER <-[Propag]- QOSType.WINDOWS <-[Call]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    OTHER
}

// CallChain[size=4] = os <-[Call]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
internal val os: QOSType by lazy { os() }

// CallChain[size=6] = osName <-[Call]- os() <-[Call]- os <-[Call]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
internal val osName: String by lazy { System.getProperty("os.name") }

// CallChain[size=5] = os() <-[Call]- os <-[Call]- qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
private fun os(): QOSType {
    val osName = osName.lowercase(Locale.getDefault())
    if (osName.contains("win")) {
        return QOSType.WINDOWS
    } else if (osName.contains("nix") || osName.contains("nux") ||
        osName.contains("aix")
    ) {
        return QOSType.LINUX
    } else if (osName.contains("mac")) {
        return QOSType.MAC
    } else if (osName.contains("sunos")) {
        return QOSType.SOLARIS
    }

    return QOSType.OTHER
}

// CallChain[size=3] = qIsWindows() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
internal fun qIsWindows() = os == QOSType.WINDOWS