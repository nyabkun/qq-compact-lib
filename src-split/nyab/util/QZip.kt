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

import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.outputStream
import nyab.conf.QE
import nyab.conf.QMyPath
import nyab.match.QM

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=8] = Path.qCreateZip() <-[Call]- QBackupFile.createBackup() <-[Call]- QBackupHelpe ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qCreateZip(
    destZipFile: Path = this.qWithNewExtension("zip"),
    ifExistsCreate: QIfExistsCreate = QIfExistsCreate.RaiseException,
    fileNameCharset: Charset = Charsets.UTF_8,
    followSymLink: Boolean = false,
    maxDepth: Int = Int.MAX_VALUE,
    createTopLevelDirInZip: Boolean = false,
    fileFilter: (Path) -> Boolean = { true },
    dirFilter: (Path) -> Boolean = { true },
): Path {
    var finalDest = destZipFile
    if (destZipFile.exists()) {
        when (ifExistsCreate) {
            QIfExistsCreate.DoNothing -> return this
            QIfExistsCreate.ChangeFileNameAndRetry -> {
                finalDest = destZipFile.qIfExistsRetryPath()
            }
            QIfExistsCreate.DeleteAndCreateFile -> {
                destZipFile.deleteIfExists()
            }
            QIfExistsCreate.RaiseException -> {
                QE.FileAlreadyExists.throwItFile(this)
            }
        }
    }

    finalDest.qCreateFile(ifExistsCreate)

    val opt =
        if (followSymLink) EnumSet.of(FileVisitOption.FOLLOW_LINKS) else EnumSet.noneOf(FileVisitOption::class.java)

    ZipOutputStream(finalDest.qOutputStream(QOpenOpt.CREATE), fileNameCharset).use { zs ->
        Files.walkFileTree(
            this, opt, maxDepth,
            object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    return if (!dirFilter(dir)) {
                        FileVisitResult.SKIP_SUBTREE
                    } else FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (!Files.isRegularFile(file) || !fileFilter(file)) {
                        return FileVisitResult.CONTINUE
                    }

                    try {
                        val inZipPath = if (createTopLevelDirInZip) {
                            this@qCreateZip.name + "/" + this@qCreateZip.qRelativeTo(file).slash
                        } else {
                            this@qCreateZip.qRelativeTo(file).slash
                        }

                        val zipEntry = ZipEntry(inZipPath)

                        zs.putNextEntry(zipEntry)

                        Files.copy(file, zs)

                        zs.closeEntry()
                    } catch (e: Exception) {
                        QE.CreateZipFileFail.throwItFile(file, e)
                    }

                    return FileVisitResult.CONTINUE
                }
            }
        )
    }

    return finalDest
}