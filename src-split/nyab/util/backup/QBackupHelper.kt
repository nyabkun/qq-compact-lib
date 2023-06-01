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

import java.nio.file.Path
import kotlin.math.abs
import nyab.conf.QMyPath
import nyab.util.QIfExistsCopyFile
import nyab.util.QIfExistsCreateDir
import nyab.util.qChangeParentDir
import nyab.util.qCreateDir
import nyab.util.qNow
import nyab.util.qWithBaseDir
import nyab.util.qWithDateTime

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=4] = Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qTryBackup(
        // Use the original file name as the folder name and place the backup file inside the folder.
        backupDir: Path = this.qWithBaseDir(QMyPath.backup),
        now: Long = qNow,
        vararg slots: QBackupSlot = QBackupSlot.DEFAULT_SLOTS,
): Path? {
    val bh = QBackupHelper(this, backupDir, *slots)
    return bh.tryBackup(now)
}

// CallChain[size=5] = QBackupHelper <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
// TODO use hash
private class QBackupHelper(
        val originalFile: Path,
        val backupDir: Path,
        vararg slots: QBackupSlot,
) {
    // CallChain[size=7] = QBackupHelper.filesInSlot <-[Call]- QBackupHelper.fillSlots() <-[Call]- QBack ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    val filesInSlot: Array<QFileInBackupSlot> = run {
        val slotsEx = if (slots.none {
                    it.periodMilli == Long.MAX_VALUE
                }
        ) {
            // slots should contain the oldest slot
            val slotsWithOldest = slots.toMutableList()
            slotsWithOldest.add(0, QBackupSlot.oldest())
            slotsWithOldest.toTypedArray()
        } else {
            slots
        }

        slotsEx.map { QFileInBackupSlot(it) }.toTypedArray()
    }

    // CallChain[size=5] = QBackupHelper.tryBackup() <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    fun tryBackup(now: Long = qNow): Path? {
        clearSlots()

        val bFiles = listBackupFilesWithNewBackupCandidate(now)

        return fillSlots(bFiles, now)
    }

    // CallChain[size=6] = QBackupHelper.clearSlots() <-[Call]- QBackupHelper.tryBackup() <-[Call]- Path ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    private fun clearSlots() {
        for (slot in filesInSlot) {
            slot.backupFile = null
        }
    }

    // CallChain[size=8] = QBackupHelper.backupPath() <-[Call]- QBackupHelper.newBackupFile() <-[Call]-  ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    private fun backupPath(now: Long): Path {
        return originalFile.qWithDateTime(qKWD_BACKUP, now).qChangeParentDir(backupDir)
    }

    // CallChain[size=7] = QBackupHelper.listBackupFilesExisting() <-[Call]- QBackupHelper.listBackupFil ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    private fun listBackupFilesExisting(): MutableList<QBackupFile> {
        backupDir.qCreateDir(QIfExistsCreateDir.DoNothing)

        val bFilePList = backupDir.qListBackupFiles(originalFile)

        val bFileList: MutableList<QBackupFile> = mutableListOf()
        for (bFileP in bFilePList) {
            val bFile = QBackupFile(bFileP, false)
            if (bFile.backupDate > 0) {
                bFileList.add(bFile)
            }
        }

        return bFileList
    }

    // CallChain[size=7] = QBackupHelper.newBackupFile() <-[Call]- QBackupHelper.listBackupFilesWithNewB ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    private fun newBackupFile(now: Long): QBackupFile {
        return QBackupFile(backupPath(now), true)
    }

    // CallChain[size=6] = QBackupHelper.listBackupFilesWithNewBackupCandidate() <-[Call]- QBackupHelper ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    private fun listBackupFilesWithNewBackupCandidate(now: Long): List<QBackupFile> {
        val bFileList = listBackupFilesExisting()

        bFileList.add(newBackupFile(now))

        return bFileList
    }

    // CallChain[size=7] = QBackupHelper.findBestFileInSlots() <-[Call]- QBackupHelper.fillSlots() <-[Ca ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    private fun findBestFileInSlots(slot: QBackupSlot, backupFiles: List<QBackupFile>, oldest: QBackupFile?, now: Long): QBackupFile? {
        var minDistance = Long.MAX_VALUE
        var bestFileInSlot: QBackupFile? = null

        for (bFile in backupFiles) {
            val elapsedPeriod = now - bFile.backupDate

            // ex. 1 year slot & oldest backup file is not older than 1 year
            if (slot.availableOnlyIfPeriodElapsed && oldest != null &&
                    now - oldest.backupDate < slot.periodMilli
            ) {
                continue
            }
            val distance = abs(
                    elapsedPeriod - slot.periodMilli
            )
            if (distance < minDistance) {
                minDistance = distance
                bestFileInSlot = bFile
            }
        }

        return bestFileInSlot
    }

    // CallChain[size=6] = QBackupHelper.fillSlots() <-[Call]- QBackupHelper.tryBackup() <-[Call]- Path. ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    /**
     * Creates new backup if necessary.
     *
     * Removes old backups if necessary.
     */
    private fun fillSlots(backupFiles: List<QBackupFile>, now: Long): Path? {
        var createdBackupFile: Path? = null

        val remainingBackupFiles = backupFiles.toMutableList()

        val oldest = remainingBackupFiles.minByOrNull {
            it.backupDate
        }

        for (slot in filesInSlot) {
            if (!slot.isEmpty) continue

            val bestFileInSlot = findBestFileInSlots(slot.slot, remainingBackupFiles, oldest, now)

            if (bestFileInSlot != null) {
                slot.backupFile = bestFileInSlot

                if (bestFileInSlot.notYetCreated) {
                    bestFileInSlot.createBackup(originalFile)
                    createdBackupFile = bestFileInSlot.backupFile
                }

                remainingBackupFiles.remove(bestFileInSlot)
            }
        }

        for (bFile in remainingBackupFiles) {
            bFile.delete()
        }

        return createdBackupFile
    }
}