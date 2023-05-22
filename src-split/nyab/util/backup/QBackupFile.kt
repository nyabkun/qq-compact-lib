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

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import nyab.conf.QMyPath
import nyab.util.QFType
import nyab.util.path
import nyab.util.qCopyFileTo
import nyab.util.qCreateZip
import nyab.util.qDateTime
import nyab.util.qListRecursive
import nyab.util.qMoveFileTo
import nyab.util.slash

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=9] = qKWD_BACKUP <-[Call]- QBackupHelper.backupPath() <-[Call]- QBackupHelper.newB ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal const val qKWD_BACKUP = "BACKUP"

// CallChain[size=8] = Path.qListBackupFiles() <-[Call]- QBackupHelper.listBackupFilesExisting() <-[ ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun Path.qListBackupFiles(orgFileOrDir: Path): List<Path> {
    return qListRecursive(QFType.FileOrDir) { file ->
        file.qIsBackupFileOf(orgFileOrDir)
    }
}

// CallChain[size=9] = Path.qIsBackupFileOf() <-[Call]- Path.qListBackupFiles() <-[Call]- QBackupHel ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun Path.qIsBackupFileOf(org: Path): Boolean {
    if (!name.startsWith("${org.nameWithoutExtension}.$qKWD_BACKUP="))
        return false

    if (!this.isDirectory() && extension.isNotEmpty()) {
        if (!name.endsWith(org.extension)) {
            return false
        }
    }

    return true
}

// CallChain[size=7] = QBackupFile <-[Ref]- QBackupHelper.fillSlots() <-[Call]- QBackupHelper.tryBac ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal class QBackupFile(val backupFile: Path, var notYetCreated: Boolean) {

    // CallChain[size=8] = QBackupFile.deleted <-[Call]- QBackupFile.delete() <-[Call]- QBackupHelper.fi ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    private var deleted: Boolean = false

    // CallChain[size=7] = QBackupFile.backupDate <-[Call]- QBackupHelper.fillSlots() <-[Call]- QBackupH ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val backupDate: Long
        get() = backupFile.qDateTime()

    // CallChain[size=7] = QBackupFile.createBackup() <-[Call]- QBackupHelper.fillSlots() <-[Call]- QBac ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    fun createBackup(orgFile: Path): Boolean {
        if (!notYetCreated) return false
        if (backupFile.exists()) return false
        if (deleted) return false

        if (orgFile.isDirectory()) {
            val destZipFile = if (!backupFile.endsWith(".zip")) {
                (backupFile.slash + ".zip").path
            } else {
                backupFile
            }

            val zipFile = orgFile.qCreateZip(destZipFile)
            zipFile.qMoveFileTo(destZipFile)
        } else {
            orgFile.qCopyFileTo(backupFile)
        }

        return true
    }

    // CallChain[size=7] = QBackupFile.delete() <-[Call]- QBackupHelper.fillSlots() <-[Call]- QBackupHel ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    fun delete() {
        if (deleted) return

        Files.deleteIfExists(backupFile)

        deleted = true
    }
}