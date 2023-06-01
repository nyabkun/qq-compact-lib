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

import java.nio.file.Path
import nyab.util.qOpenEditor

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QCompactLibResult(
    val lib: QCompactLib,
    val stat: QCompactLibStat,
    val latestReleaseVersion: String,
    val version: String,
    val oldHash: String,
    val newHash: String,
    val isVersionUpdated: Boolean,
    val mainContext: QAnalysisContext,
    val testContext: QAnalysisContext?,
    val singleMainSrcFile: Path?,
    val singleTestSrcFile: Path?,
    val readMeFile: Path,
    val buildGradleKtsFile: Path,
    val settingsGradleKtsFile: Path,
    val gitIgnoreFile: Path,
    val buildSuccess: Boolean
) {
    // << Root of the CallChain >>
    suspend fun openEditorAll() {
        singleMainSrcFile?.qOpenEditor()

        singleTestSrcFile?.qOpenEditor()

        readMeFile.qOpenEditor()

        buildGradleKtsFile.qOpenEditor()

        settingsGradleKtsFile.qOpenEditor()

        gitIgnoreFile.qOpenEditor()

        stat.statFile.qOpenEditor()
    }
}