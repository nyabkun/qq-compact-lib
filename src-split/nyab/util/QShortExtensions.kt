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
import java.nio.file.Paths
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import nyab.conf.QMyPath

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=11] = base <-[Call]- Path.qAppendBaseName() <-[Call]- Path.qWithDateTime() <-[Call ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal val Path.base: String
    get() = nameWithoutExtension

// CallChain[size=8] = slash <-[Call]- QBackupFile.createBackup() <-[Call]- QBackupHelper.fillSlots( ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal val Path.slash: String
    get() = this.toString().replace('\\', '/')

// CallChain[size=8] = path <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- QExcept ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal val String.path: Path
    get() = Paths.get(this.trim()).toAbsolutePath().normalize()

// CallChain[size=2] = file <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
internal val String.file: File
    get() = File(this.trim()).normalize().absoluteFile

// CallChain[size=9] = norm <-[Call]- Path.qCopyFileTo() <-[Call]- QBackupFile.createBackup() <-[Cal ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal val Path.norm: Path
    get() = toAbsolutePath().normalize()