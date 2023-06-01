/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package nyab.util

import com.sun.nio.file.ExtendedCopyOption
import com.sun.nio.file.ExtendedOpenOption
import java.awt.Desktop
import java.awt.Dimension
import java.io.BufferedInputStream
import java.io.IOException
import java.io.LineNumberReader
import java.io.OutputStream
import java.io.PrintStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.file.CopyOption
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.io.path.absolutePathString
import kotlin.io.path.appendText
import kotlin.io.path.bufferedReader
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.pathString
import kotlin.io.path.readText
import kotlin.io.path.reader
import kotlin.io.path.useLines
import kotlin.io.path.writeText
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.streams.asSequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nyab.conf.QE
import nyab.conf.QMyEditor
import nyab.conf.QMyPath
import nyab.match.QM
import nyab.match.qMatches
import nyab.util.backup.qTryBackup

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=11] = qBUFFER_SIZE <-[Call]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[ ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal const val qBUFFER_SIZE = DEFAULT_BUFFER_SIZE

// CallChain[size=10] = qFILE_LIST_RECURSIVE_MAX_DEPTH <-[Call]- Path.qListRecursive() <-[Call]- Pat ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
private const val qFILE_LIST_RECURSIVE_MAX_DEPTH: Int = 1000

// CallChain[size=11] = QOpenOpt <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call] ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
// @formatter:off
internal enum class QOpenOpt(val opt: OpenOption) : QFlagEnum<QOpenOpt> {
    // CallChain[size=13] = QOpenOpt.TRUNCATE_EXISTING <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    TRUNCATE_EXISTING(StandardOpenOption.TRUNCATE_EXISTING),
    // CallChain[size=13] = QOpenOpt.CREATE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    CREATE(StandardOpenOption.CREATE),
    // CallChain[size=13] = QOpenOpt.CREATE_NEW <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    CREATE_NEW(StandardOpenOption.CREATE_NEW),
    // CallChain[size=13] = QOpenOpt.WRITE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toO ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    WRITE(StandardOpenOption.WRITE),
    // CallChain[size=13] = QOpenOpt.READ <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toOp ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    READ(StandardOpenOption.READ),
    // CallChain[size=13] = QOpenOpt.APPEND <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    APPEND(StandardOpenOption.APPEND),
    // CallChain[size=13] = QOpenOpt.DELETE_ON_CLOSE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOp ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    DELETE_ON_CLOSE(StandardOpenOption.DELETE_ON_CLOSE),
    // CallChain[size=13] = QOpenOpt.DSYNC <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toO ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    DSYNC(StandardOpenOption.DSYNC),
    // CallChain[size=13] = QOpenOpt.SYNC <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.toOp ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    SYNC(StandardOpenOption.SYNC),
    // CallChain[size=13] = QOpenOpt.SPARSE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt>.to ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    SPARSE(StandardOpenOption.SPARSE),
    // CallChain[size=13] = QOpenOpt.EX_DIRECT <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOpenOpt> ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    EX_DIRECT(ExtendedOpenOption.DIRECT),
    // CallChain[size=13] = QOpenOpt.EX_NOSHARE_DELETE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    EX_NOSHARE_DELETE(ExtendedOpenOption.NOSHARE_DELETE),
    // CallChain[size=13] = QOpenOpt.EX_NOSHARE_READ <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QOp ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    EX_NOSHARE_READ(ExtendedOpenOption.NOSHARE_READ),
    // CallChain[size=13] = QOpenOpt.EX_NOSHARE_WRITE <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<QO ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    EX_NOSHARE_WRITE(ExtendedOpenOption.NOSHARE_WRITE),
    // CallChain[size=13] = QOpenOpt.LN_NOFOLLOW_LINKS <-[Propag]- QOpenOpt.QOpenOpt() <-[Call]- QFlag<Q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    LN_NOFOLLOW_LINKS(LinkOption.NOFOLLOW_LINKS);

    companion object {
        // CallChain[size=10] = QOpenOpt.DEFAULT <-[Call]- Path.qOutputStream() <-[Call]- Path.qCreateZip()  ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
        val DEFAULT = CREATE + TRUNCATE_EXISTING + WRITE
    }
}

// CallChain[size=6] = Path.sep() <-[Call]- QMyPath.backup <-[Call]- Path.qTryBackup() <-[Call]- Pat ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal infix fun Path.sep(childPath: String): Path = Paths.get(toString(), childPath)

// CallChain[size=11] = QFlag<QOpenOpt>.toOptEnums() <-[Call]- Path.qReader() <-[Call]- Path.qFetchL ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun QFlag<QOpenOpt>.toOptEnums(): Array<OpenOption> {
    return toEnumValues().map { it.opt }.toTypedArray()
}

// CallChain[size=9] = QCopyOpt <-[Ref]- Path.qCopyFileTo() <-[Call]- QBackupFile.createBackup() <-[ ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal enum class QCopyOpt(val opt: CopyOption) : QFlagEnum<QCopyOpt> {
    // CallChain[size=9] = QCopyOpt.REPLACE_EXISTING <-[Call]- Path.qCopyFileTo() <-[Call]- QBackupFile. ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    REPLACE_EXISTING(StandardCopyOption.REPLACE_EXISTING),
    // CallChain[size=10] = QCopyOpt.COPY_ATTRIBUTES <-[Propag]- QCopyOpt.REPLACE_EXISTING <-[Call]- Pat ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    COPY_ATTRIBUTES(StandardCopyOption.COPY_ATTRIBUTES),
    // CallChain[size=10] = QCopyOpt.ATOMIC_MOVE <-[Propag]- QCopyOpt.REPLACE_EXISTING <-[Call]- Path.qC ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    ATOMIC_MOVE(
        StandardCopyOption.ATOMIC_MOVE
    ),
    // CallChain[size=10] = QCopyOpt.EX_INTERRUPTIBLE <-[Propag]- QCopyOpt.REPLACE_EXISTING <-[Call]- Pa ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    EX_INTERRUPTIBLE(ExtendedCopyOption.INTERRUPTIBLE),
    // CallChain[size=10] = QCopyOpt.LN_NOFOLLOW_LINKS <-[Propag]- QCopyOpt.REPLACE_EXISTING <-[Call]- P ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    LN_NOFOLLOW_LINKS(LinkOption.NOFOLLOW_LINKS);

    
}

// CallChain[size=9] = QFlag<QCopyOpt>.toOptEnums() <-[Call]- Path.qCopyFileTo() <-[Call]- QBackupFi ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun QFlag<QCopyOpt>.toOptEnums(): Array<CopyOption> {
    return toEnumValues().map { it.opt }.toTypedArray()
}

// CallChain[size=8] = Path.qDateTime() <-[Call]- QBackupFile.backupDate <-[Call]- QBackupHelper.fil ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qDateTime(): Long = qCacheItOneSec(name) {
    name.qParseDateTime()
}

// CallChain[size=2] = Path.qReadFile() <-[Call]- KtElement.qSrcLine()[Root]
internal fun Path.qReadFile(charset: Charset = Charsets.UTF_8, useCache: Boolean = false): String {
    return if (useCache) {
        qCacheIt(this) {
            this.readText(charset)
        }
    } else {
        this.readText(charset)
    }
}

// CallChain[size=7] = String.path() <-[Call]- String.pathTmp <-[Call]- String.qRunShellScript() <-[ ... nameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
/**
 * Create [Path] with a [pathString] relative to [baseDir]
 */
internal fun String.path(baseDir: Path = QMyPath.root): Path {
    val path = Paths.get(this)

    if (path.isAbsolute) QE.ShouldBeRelativePath.throwItFile(path)

    // if path is absolute, resolve method just returns path itself
    return baseDir.resolve(path).norm
}

// CallChain[size=9] = Path.qOutputStream() <-[Call]- Path.qCreateZip() <-[Call]- QBackupFile.create ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qOutputStream(opts: QFlag<QOpenOpt> = QOpenOpt.DEFAULT): OutputStream {
    return Files.newOutputStream(this, *opts.toOptEnums())
}

// CallChain[size=5] = Path.qDeleteDirContents() <-[Call]- Path.qForceDelete() <-[Call]- QGit.gitHub ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qDeleteDirContents(maxDepth: Int = Int.MAX_VALUE, throwException: Boolean = false) {
    if (this.exists() && this.isDirectory()) {
        this.qSeq(QFType.Any, maxDepth = maxDepth).forEach {
            try {
                it.qForceDelete() // TODO remove recursive call
            } catch (e: Exception) {
                if (throwException)
                    throw e
            }
        }
    } else if (this.exists()) {
        QE.FileAlreadyExists.throwItFile(this)
    }
}

// CallChain[size=4] = Path.qForceDelete() <-[Call]- QGit.gitHubDownloadSingleDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qForceDelete() {
    if (this.isDirectory()) {
        this.qDeleteDirContents()
        this.qDeleteDirIfEmpty()
    } else {
        this.deleteIfExists()
    }
}

// CallChain[size=6] = Sequence<*>.qIsEmpty() <-[Call]- Path.qDeleteDirIfEmpty() <-[Call]- Path.qFor ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Sequence<*>.qIsEmpty(): Boolean =
    count() == 0

// CallChain[size=5] = Path.qDeleteDirIfEmpty() <-[Call]- Path.qForceDelete() <-[Call]- QGit.gitHubD ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qDeleteDirIfEmpty(): Boolean {
    if (!this.isDirectory()) {
        return false
    }

    return if (this.qSeq(QFType.Any).qIsEmpty()) {
        try {
            this.deleteIfExists()
            true
        } catch (e: Exception) {
            false
        }
    } else {
        false
    }
}

// CallChain[size=4] = Path.qMoveDirTo() <-[Call]- QGit.gitHubDownloadSingleDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
// TODO with progress
internal fun Path.qMoveDirTo(
    destDir: Path,
    ifExistsContentFile: QIfExistsCopyFile = QIfExistsCopyFile.Overwrite,
    ifExistsDestDir: QIfExistsCopyDir = QIfExistsCopyDir.DoNothing,
    atomic: Boolean = true,
): Path {
    if (this.qIsSubDirOrFileOf(destDir) || destDir.qIsSubDirOrFileOf(this))
        QE.ShouldNotBeSubDirectory.throwItBrackets("Src", this, "Dest", destDir)

    if (!this.exists()) QE.DirectoryNotFound.throwItFile(this)

    var finalDestDir = destDir

    if (destDir.exists()) {
        when (ifExistsDestDir) {
            QIfExistsCopyDir.DoNothing -> {
                return destDir
            }

            QIfExistsCopyDir.Merge -> {
            }

            QIfExistsCopyDir.ChangeFileNameAndRetry -> {
                finalDestDir = this.qIfExistsRetryPath()
            }

            QIfExistsCopyDir.Overwrite -> {
                destDir.qForceDelete()
                destDir.createDirectories()
            }

            QIfExistsCopyDir.OverwriteIfDifferentHash -> {
                if (this.qHash() != destDir.qHash()) {
                    destDir.qForceDelete()
                    destDir.createDirectories()
                } else {
                    return destDir
                }
            }

            QIfExistsCopyDir.RaiseException -> {
                QE.DirectoryAlreadyExists.throwItDir(this)
            }
        }

        val opt = EnumSet.noneOf(FileVisitOption::class.java)

        Files.walkFileTree(
            this, opt, Int.MAX_VALUE,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.createDirectories(finalDestDir.resolve(this@qMoveDirTo.relativize(dir)))

                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.isDirectory())
                        return FileVisitResult.CONTINUE

                    val destFile = finalDestDir.resolve(this@qMoveDirTo.relativize(file))

                    file.qMoveFileTo(destFile, ifExistsContentFile, atomic)

                    return FileVisitResult.CONTINUE
                }
            }
        )

        this.qForceDelete()

        return finalDestDir
    }

    return this.qMoveFileTo(finalDestDir, ifExistsContentFile, atomic)
}

// CallChain[size=9] = Path.qIfExistsRetryPath() <-[Call]- Path.qCopyFileTo() <-[Call]- QBackupFile. ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qIfExistsRetryPath(retryNum: Int = 3): Path {
    val now = System.currentTimeMillis()

    for (i in 0 until retryNum) {
        val retryPath =
            this.qWithNewBaseName("$nameWithoutExtension.{${now + i}}$extension")

        if (retryPath.notExists()) {
            return retryPath
        }
    }

    QE.FileAlreadyExists.throwItFile(this)
}

// CallChain[size=8] = Path.qCopyFileTo() <-[Call]- QBackupFile.createBackup() <-[Call]- QBackupHelp ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qCopyFileTo(
    destFile: Path = Paths.get("$name.copy"),
    ifExists: QIfExistsCopyFile = QIfExistsCopyFile.ChangeFileNameAndRetry,
): Path {
    if (!this.exists()) QE.FileNotFound.throwItFile(this)

    destFile.qCreateParentDirs()

    var options: QFlag<QCopyOpt> = QFlag.none()

    if (destFile.exists()) {
        when (ifExists) {
            QIfExistsCopyFile.ChangeFileNameAndRetry -> {
                val retryPath = this.qIfExistsRetryPath()

                return try {
                    Files.copy(this, retryPath, *options.toOptEnums()).norm
                } catch (e: FileAlreadyExistsException) {
                    QE.FileAlreadyExists.throwItFile(this)
                }
            }

            QIfExistsCopyFile.Overwrite -> {
                options += QCopyOpt.REPLACE_EXISTING
                return Files.copy(this, destFile, *options.toOptEnums()).norm
            }

            QIfExistsCopyFile.OverwriteIfDifferentHash -> {
                return if (this.qHash() != destFile.qHash()) {
                    options += QCopyOpt.REPLACE_EXISTING
                    Files.copy(this, destFile, *options.toOptEnums()).norm
                } else {
                    destFile
                }
            }

            QIfExistsCopyFile.RaiseException -> {
                QE.FileAlreadyExists.throwItFile(this)
            }

            QIfExistsCopyFile.DoNothing -> {
                return destFile
            }
        }
    }

    return Files.copy(this, destFile, *options.toOptEnums()).norm
}

// CallChain[size=10] = Path.qAppendBaseName() <-[Call]- Path.qWithDateTime() <-[Call]- QBackupHelpe ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qAppendBaseName(nameSuffix: String): Path {
    return qWithNewBaseName(base + nameSuffix)
}

// CallChain[size=9] = Path.qWithDateTime() <-[Call]- QBackupHelper.backupPath() <-[Call]- QBackupHe ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qWithDateTime(prefix: String = "TIME", time: Long = System.currentTimeMillis()): Path {
    return qAppendBaseName(".$prefix=${time.qFormatDateTime()}")
}

// CallChain[size=10] = Path.qWithNewBaseName() <-[Call]- Path.qIfExistsRetryPath() <-[Call]- Path.q ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qWithNewBaseName(baseName: String): Path {
    return resolveSibling(Paths.get(qNewBaseNameStr(baseName)))
}

// CallChain[size=11] = Path.qNewBaseNameStr() <-[Call]- Path.qWithNewBaseName() <-[Call]- Path.qIfE ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qNewBaseNameStr(nameWithoutExtension: String): String {
    return if (this.extension.isEmpty()) {
        nameWithoutExtension
    } else {
        nameWithoutExtension + "." + this.extension
    }
}

// CallChain[size=6] = Path.qBaseDir() <-[Call]- Path.qWithBaseDir() <-[Call]- Path.qTryBackup() <-[ ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qBaseDir(
    baseDirsToSearch: List<Path> = QMyPath.base,
): Path? {
    val found = baseDirsToSearch.filter {
        this.qIsSubDirOrFileOf(it)
    }

    return if (found.isEmpty()) {
        null
    } else if (found.size == 1) {
        found[0]
    } else {
        qFindNearestBaseDir(found)
    }
}

// CallChain[size=7] = qFindNearestBaseDir() <-[Call]- Path.qBaseDir() <-[Call]- Path.qWithBaseDir() ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
private fun qFindNearestBaseDir(dirs: List<Path>): Path = qCacheItOneSec(dirs.toString()) {
    val sorted = dirs.sortedWith { aDir, bDir ->
        when {
            aDir.qIsSubDirOrFileOf(bDir) -> -1
            bDir.qIsSubDirOrFileOf(aDir) -> 1
            else -> 0
        }
    }

    sorted[0]
}

// CallChain[size=7] = Path.qIsSubDirOrFileOf() <-[Call]- Path.qBaseDir() <-[Call]- Path.qWithBaseDi ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qIsSubDirOrFileOf(baseDir: Path): Boolean {
    var pa: Path? = this.toAbsolutePath().normalize().parent
    val base = baseDir.toAbsolutePath().normalize()
    while (pa != null) {
        if (pa == base) {
            return true
        }
        pa = pa.parent
    }

    return false
}

// CallChain[size=9] = Path.qChangeParentDir() <-[Call]- QBackupHelper.backupPath() <-[Call]- QBacku ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qChangeParentDir(newParentDir: Path): Path {
    return newParentDir.resolve(Paths.get(this.name)).norm
}

// CallChain[size=6] = Path.qToRelativePath() <-[Call]- Path.qWithBaseDir() <-[Call]- Path.qTryBacku ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
private fun Path.qToRelativePath(): Path {
    var newPathString = if (this.pathString.contains(":")) {
        this.pathString.replace(':', '_')
    } else {
        this.pathString
    }

    newPathString = newPathString.replace("//", "/").removePrefix("/")

    val newPath = Paths.get(newPathString)

    (newPath.isAbsolute).qaFalse(QE.ShouldBeRelativePath)

    return newPath
}

// CallChain[size=5] = Path.qWithBaseDir() <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qWithBaseDir(newBaseDir: Path? = QMyPath.temp): Path {
    if (newBaseDir == null) {
        return this
    }

    val base = this.qBaseDir()

    return if (base == null) {
        newBaseDir.resolve(this.qToRelativePath()).norm
    } else {
        val rel = this.qRelativeFrom(base)
        newBaseDir.resolve(rel).norm
    }
}

// CallChain[size=9] = QIfExistsCreate <-[Ref]- Path.qCreateZip() <-[Call]- QBackupFile.createBackup ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal enum class QIfExistsCreate {
    // CallChain[size=9] = QIfExistsCreate.DoNothing <-[Call]- Path.qCreateZip() <-[Call]- QBackupFile.c ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    DoNothing,
    // CallChain[size=9] = QIfExistsCreate.ChangeFileNameAndRetry <-[Call]- Path.qCreateZip() <-[Call]-  ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    ChangeFileNameAndRetry,
    // CallChain[size=9] = QIfExistsCreate.DeleteAndCreateFile <-[Call]- Path.qCreateZip() <-[Call]- QBa ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    DeleteAndCreateFile,
    // CallChain[size=9] = QIfExistsCreate.RaiseException <-[Call]- Path.qCreateZip() <-[Call]- QBackupF ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    RaiseException
}

// CallChain[size=8] = QIfExistsCreateDir <-[Ref]- QBackupHelper.listBackupFilesExisting() <-[Call]- ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal enum class QIfExistsCreateDir {
    // CallChain[size=8] = QIfExistsCreateDir.DoNothing <-[Call]- QBackupHelper.listBackupFilesExisting( ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    DoNothing,
    // CallChain[size=9] = QIfExistsCreateDir.ChangeDirNameAndRetry <-[Propag]- QIfExistsCreateDir.DoNot ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    ChangeDirNameAndRetry,
    // CallChain[size=9] = QIfExistsCreateDir.DeleteContents <-[Propag]- QIfExistsCreateDir.DoNothing <- ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    DeleteContents,
    // CallChain[size=9] = QIfExistsCreateDir.RaiseException <-[Propag]- QIfExistsCreateDir.DoNothing <- ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    RaiseException
}

// CallChain[size=4] = QIfExistsCopyFile <-[Ref]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal enum class QIfExistsCopyFile {
    // CallChain[size=9] = QIfExistsCopyFile.DoNothing <-[Call]- Path.qCopyFileTo() <-[Call]- QBackupFil ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    DoNothing,
    // CallChain[size=9] = QIfExistsCopyFile.ChangeFileNameAndRetry <-[Call]- Path.qCopyFileTo() <-[Call ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    ChangeFileNameAndRetry,
    // CallChain[size=4] = QIfExistsCopyFile.Overwrite <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    Overwrite,
    // CallChain[size=9] = QIfExistsCopyFile.OverwriteIfDifferentHash <-[Call]- Path.qCopyFileTo() <-[Ca ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    OverwriteIfDifferentHash,
    // CallChain[size=9] = QIfExistsCopyFile.RaiseException <-[Call]- Path.qCopyFileTo() <-[Call]- QBack ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    RaiseException
}

// CallChain[size=4] = QIfExistsCopyDir <-[Ref]- QGit.gitHubDownloadSingleDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal enum class QIfExistsCopyDir {
    // CallChain[size=5] = QIfExistsCopyDir.DoNothing <-[Call]- Path.qMoveDirTo() <-[Call]- QGit.gitHubD ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    DoNothing,
    // CallChain[size=5] = QIfExistsCopyDir.ChangeFileNameAndRetry <-[Call]- Path.qMoveDirTo() <-[Call]- ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    ChangeFileNameAndRetry,
    // CallChain[size=4] = QIfExistsCopyDir.Overwrite <-[Call]- QGit.gitHubDownloadSingleDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Overwrite,
    // CallChain[size=5] = QIfExistsCopyDir.OverwriteIfDifferentHash <-[Call]- Path.qMoveDirTo() <-[Call ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    OverwriteIfDifferentHash,
    // CallChain[size=5] = QIfExistsCopyDir.RaiseException <-[Call]- Path.qMoveDirTo() <-[Call]- QGit.gi ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    RaiseException,
    // CallChain[size=5] = QIfExistsCopyDir.Merge <-[Call]- Path.qMoveDirTo() <-[Call]- QGit.gitHubDownl ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Merge
}

// CallChain[size=2] = QIfExistsWrite <-[Ref]- QCompactLibIfFileExists[Root]
enum class QIfExistsWrite {
    // CallChain[size=3] = QIfExistsWrite.DoNothing <-[Propag]- QIfExistsWrite <-[Ref]- QCompactLibIfFileExists[Root]
    DoNothing,
    // CallChain[size=3] = QIfExistsWrite.OverwriteAtomic <-[Propag]- QIfExistsWrite <-[Ref]- QCompactLibIfFileExists[Root]
    OverwriteAtomic,
    // CallChain[size=3] = QIfExistsWrite.OverwriteDirect <-[Propag]- QIfExistsWrite <-[Ref]- QCompactLibIfFileExists[Root]
    OverwriteDirect,
    // CallChain[size=3] = QIfExistsWrite.BackupAndOverwriteAtomic <-[Propag]- QIfExistsWrite <-[Ref]- QCompactLibIfFileExists[Root]
    BackupAndOverwriteAtomic,
    // CallChain[size=3] = QIfExistsWrite.AppendAtomicHighCost <-[Propag]- QIfExistsWrite <-[Ref]- QCompactLibIfFileExists[Root]
    AppendAtomicHighCost,
    // CallChain[size=3] = QIfExistsWrite.AppendDirect <-[Propag]- QIfExistsWrite <-[Ref]- QCompactLibIfFileExists[Root]
    AppendDirect,
    // CallChain[size=3] = QIfExistsWrite.RaiseException <-[Propag]- QIfExistsWrite <-[Ref]- QCompactLibIfFileExists[Root]
    RaiseException
}

// CallChain[size=4] = Path.qCreateTempFile() <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qCreateTempFile(ifExists: QIfExistsCreate = QIfExistsCreate.DeleteAndCreateFile): Path {
    val tempPath = this.qWithBaseDir(QMyPath.temp)
    return tempPath.qCreateFile(ifExists)
}

// CallChain[size=4] = Path.qCreateTempFileAndCopyContent() <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qCreateTempFileAndCopyContent(ifExists: QIfExistsCopyFile = QIfExistsCopyFile.Overwrite): Path {
    val tempPath = this.qWithBaseDir(QMyPath.temp)
    return this.qCopyFileTo(tempPath, ifExists)
}

// CallChain[size=3] = Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qWrite(
    text: String,
    ifExists: QIfExistsWrite = QIfExistsWrite.OverwriteAtomic,
    createParentDirs: Boolean = true,
    charset: Charset = Charsets.UTF_8,
): Path {
    if (createParentDirs) this.qCreateParentDirs()

    if (this.notExists()) {
        this.writeText(text, charset)
    } else {
        when (ifExists) {
            QIfExistsWrite.OverwriteAtomic -> {
                val tmp = this.qCreateTempFile()
                tmp.writeText(text, charset)
                tmp.qMoveFileTo(this, QIfExistsCopyFile.Overwrite, atomic = true)
            }

            QIfExistsWrite.OverwriteDirect -> {
                this.writeText(text, charset)
            }

            QIfExistsWrite.AppendDirect -> {
                this.appendText(text, charset)
            }

            QIfExistsWrite.AppendAtomicHighCost -> {
                val tmp = this.qCreateTempFileAndCopyContent()
                tmp.appendText(text, charset)
                tmp.qMoveFileTo(this, QIfExistsCopyFile.Overwrite)
            }

            QIfExistsWrite.BackupAndOverwriteAtomic -> {
                this.qTryBackup()

                val tmp = this.qCreateTempFileAndCopyContent()
                tmp.writeText(text, charset)
                tmp.qMoveFileTo(this, QIfExistsCopyFile.Overwrite)
            }

            QIfExistsWrite.RaiseException -> {
                QE.FileAlreadyExists.throwItFile(this)
            }

            QIfExistsWrite.DoNothing -> {
                return this
            }
        }
    }

    return this
}

// CallChain[size=8] = Path.qCreateDir() <-[Call]- QBackupHelper.listBackupFilesExisting() <-[Call]- ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qCreateDir(
    ifExists: QIfExistsCreateDir = QIfExistsCreateDir.DoNothing,
    createParentDirs: Boolean = true,
): Path {
    if (createParentDirs) this.qCreateParentDirs()

    return try {
        this.createDirectory()
    } catch (e: FileAlreadyExistsException) {
        when (ifExists) {
            QIfExistsCreateDir.DeleteContents -> {
                forEachDirectoryEntry {
                    it.deleteIfExists()
                }

                this
            }

            QIfExistsCreateDir.ChangeDirNameAndRetry -> {
                val retryPath = this.qIfExistsRetryPath()

                try {
                    retryPath.createDirectory()
                } catch (e: FileAlreadyExistsException) {
                    QE.FileAlreadyExists.throwItFile(this)
                }
            }

            QIfExistsCreateDir.RaiseException -> {
                QE.FileAlreadyExists.throwItFile(this)
            }

            QIfExistsCreateDir.DoNothing -> {
                this
            }
        }
    }
}

// CallChain[size=9] = Path.qCreateFile() <-[Call]- Path.qCreateZip() <-[Call]- QBackupFile.createBa ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qCreateFile(
    ifExists: QIfExistsCreate = QIfExistsCreate.DoNothing,
    createParentDirs: Boolean = true,
): Path {
    if (createParentDirs) this.qCreateParentDirs()

    return if (this.exists()) {
        when (ifExists) {
            QIfExistsCreate.DeleteAndCreateFile -> {
                Files.deleteIfExists(this)
                Files.createFile(this)
            }

            QIfExistsCreate.ChangeFileNameAndRetry -> {
                val retryPath = this.qIfExistsRetryPath()

                return try {
                    retryPath.createFile()
                    Files.createFile(retryPath)
                } catch (e: FileAlreadyExistsException) {
                    QE.FileAlreadyExists.throwItFile(this)
                }
            }

            QIfExistsCreate.RaiseException -> {
                QE.FileAlreadyExists.throwItFile(this)
            }

            QIfExistsCreate.DoNothing -> {
                this
            }
        }
    } else {
        Files.createFile(this)
    }
}

// CallChain[size=6] = Path.qRelativeFrom() <-[Call]- Path.qWithBaseDir() <-[Call]- Path.qTryBackup( ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qRelativeFrom(baseDir: Path = QMyPath.root): Path {
    return baseDir.relativize(this)
}

// CallChain[size=9] = Path.qRelativeTo() <-[Call]- Path.qCreateZip() <-[Call]- QBackupFile.createBa ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qRelativeTo(childFile: Path = QMyPath.root): Path {
    return this.relativize(childFile)
}

// CallChain[size=10] = Path.qHashFile() <-[Call]- Path.qHash() <-[Call]- Path.qCopyFileTo() <-[Call ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
@Throws(IOException::class, NoSuchAlgorithmException::class)
private fun Path.qHashFile(
    algorithm: QHash = QHash.DEFAULT,
    buffSize: Int = qBUFFER_SIZE,
    additionalData: String? = null,
): String {
    val md = MessageDigest.getInstance(algorithm.str)
    BufferedInputStream(Files.newInputStream(this, StandardOpenOption.READ), buffSize).use { input ->
        DigestOutputStream(OutputStream.nullOutputStream(), md).use { out ->
            input.transferTo(out)

            additionalData?.byteInputStream()?.transferTo(out)
        }
    }
    val fx = "%0${md.digestLength * 2}x"
    return String.format(fx, BigInteger(1, md.digest()))
}

// CallChain[size=10] = Path.qLineSeparator() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileL ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Path.qLineSeparator(charset: Charset = Charsets.UTF_8): QLineSeparator {
    this.bufferedReader(charset).use { reader ->
        var ch: Char

        while (true) {
            ch = reader.read().toChar()

            if (ch == '\u0000') return QLineSeparator.DEFAULT

            if (ch == '\r') {
                val nextCh = reader.read().toChar()

                if (nextCh == '\u0000') return QLineSeparator.CR

                return if (nextCh == '\n') QLineSeparator.CRLF
                else QLineSeparator.CR
            } else if (ch == '\n') {
                return QLineSeparator.LF
            }
        }
    }
}

// CallChain[size=9] = QFetchEnd <-[Ref]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut() <-[Call ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal enum class QFetchEnd {
    // CallChain[size=10] = QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE <-[Propag]- QFetchEnd.END_WITH ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE,
    // CallChain[size=9] = QFetchEnd.END_WITH_THIS_LINE <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrcC ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    END_WITH_THIS_LINE,
    // CallChain[size=10] = QFetchEnd.END_WITH_NEXT_LINE <-[Propag]- QFetchEnd.END_WITH_THIS_LINE <-[Cal ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    END_WITH_NEXT_LINE,
    // CallChain[size=10] = QFetchEnd.END_WITH_PREVIOUS_LINE <-[Propag]- QFetchEnd.END_WITH_THIS_LINE <- ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    END_WITH_PREVIOUS_LINE
}

// CallChain[size=9] = QFetchStart <-[Ref]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut() <-[Ca ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal enum class QFetchStart {
    // CallChain[size=9] = QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE <-[Call]- QFetchRule.SING ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE,
    // CallChain[size=9] = QFetchStart.START_FROM_THIS_LINE <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- Q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    START_FROM_THIS_LINE,
    // CallChain[size=10] = QFetchStart.START_FROM_NEXT_LINE <-[Propag]- QFetchStart.FETCH_THIS_LINE_AND ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    START_FROM_NEXT_LINE,
    // CallChain[size=10] = QFetchStart.START_FROM_PREVIOUS_LINE <-[Propag]- QFetchStart.FETCH_THIS_LINE ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    START_FROM_PREVIOUS_LINE
}

// CallChain[size=9] = QFetchRuleA <-[Call]- QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut() <-[C ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal abstract class QFetchRuleA(
    override val numLinesBeforeTargetLine: Int = 10,
    override val numLinesAfterTargetLine: Int = 10,
) : QFetchRule

// CallChain[size=8] = QFetchRule <-[Ref]- QSrcCut.QSrcCut() <-[Call]- qLogStackFrames() <-[Call]- Q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal interface QFetchRule {
    // CallChain[size=9] = QFetchRule.numLinesBeforeTargetLine <-[Propag]- QFetchRule.SINGLE_LINE <-[Cal ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val numLinesBeforeTargetLine: Int
    // CallChain[size=9] = QFetchRule.numLinesAfterTargetLine <-[Propag]- QFetchRule.SINGLE_LINE <-[Call ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val numLinesAfterTargetLine: Int

    // CallChain[size=9] = QFetchRule.fetchStartCheck() <-[Propag]- QFetchRule.SINGLE_LINE <-[Call]- QSr ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun fetchStartCheck(
        line: String,
        currentLineNumber: Int,
        targetLine: String,
        targetLineNumber: Int,
        context: MutableSet<String>,
    ): QFetchStart

    // CallChain[size=9] = QFetchRule.fetchEndCheck() <-[Propag]- QFetchRule.SINGLE_LINE <-[Call]- QSrcC ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun fetchEndCheck(
        line: String,
        currentLineNumber: Int,
        targetLine: String,
        targetLineNumber: Int,
        context: MutableSet<String>,
    ): QFetchEnd

    companion object {
        // CallChain[size=8] = QFetchRule.SINGLE_LINE <-[Call]- QSrcCut.QSrcCut() <-[Call]- qLogStackFrames( ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        val SINGLE_LINE = object : QFetchRuleA(0, 0) {
            override fun fetchStartCheck(
                line: String,
                currentLineNumber: Int,
                targetLine: String,
                targetLineNumber: Int,
                context: MutableSet<String>,
            ): QFetchStart = if (currentLineNumber == targetLineNumber) {
                QFetchStart.START_FROM_THIS_LINE
            } else {
                QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE
            }

            override fun fetchEndCheck(
                line: String,
                currentLineNumber: Int,
                targetLine: String,
                targetLineNumber: Int,
                context: MutableSet<String>,
            ): QFetchEnd = if (currentLineNumber == targetLineNumber) {
                QFetchEnd.END_WITH_THIS_LINE
            } else {
                qUnreachable()
            }
        }

        // CallChain[size=5] = QFetchRule.SMART_FETCH_INFIX <-[Call]- QSrcCut.MULTILINE_INFIX_NOCUT <-[Call]- qThrowIt() <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
        val SMART_FETCH_INFIX = object : QFetchRuleA(10, 10) {
            // """               <<< targetLine
            // some text
            // """ shouldBe """
            // some text
            // """

            override fun fetchStartCheck(
                line: String,
                currentLineNumber: Int,
                targetLine: String,
                targetLineNumber: Int,
                context: MutableSet<String>,
            ): QFetchStart {
                return QFetchStart.START_FROM_THIS_LINE
            }

            override fun fetchEndCheck(
                line: String,
                currentLineNumber: Int,
                targetLine: String,
                targetLineNumber: Int,
                context: MutableSet<String>,
            ): QFetchEnd = if (currentLineNumber >= targetLineNumber) {
                val nIndentThis = line.qCountLeftSpace()
                val nIndentTarget = targetLine.qCountLeftSpace()

                if (currentLineNumber == targetLineNumber && line.trimStart()
                        .startsWith("\"\"\"") && line.qCountOccurrence("\"\"\"") == 1
                ) {
                    // """
                    // some text
                    // """.log           <<< targetLine
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (line.qEndsWith(""".* should[a-zA-Z]+ ""${'"'}""".re)) { // TODO More accurately
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (line.endsWith("{") || line.endsWith("(") || line.endsWith(".")) {
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (nIndentThis == nIndentTarget) {
                    QFetchEnd.END_WITH_THIS_LINE
                } else {
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                }
            } else {
                QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
            }
        }

        // CallChain[size=7] = QFetchRule.SMART_FETCH <-[Call]- qLogStackFrames() <-[Call]- QException.mySrc ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        val SMART_FETCH = object : QFetchRuleA(10, 10) {
            override fun fetchStartCheck(
                line: String,
                currentLineNumber: Int,
                targetLine: String,
                targetLineNumber: Int,
                context: MutableSet<String>,
            ): QFetchStart {
                val nIndentThis = line.qCountLeftSpace()
                val nIndentTarget = targetLine.qCountLeftSpace()
                val trimmed = line.trimStart()

                return if (arrayOf(
                        "\"\"\".",
                        "}",
                        ")",
                        ".",
                        ",",
                        "?",
                        "//",
                        "/*",
                        "*"
                    ).any { trimmed.startsWith(it) }
                ) {
                    QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE
                } else if (nIndentThis <= nIndentTarget) {
                    QFetchStart.START_FROM_THIS_LINE
                } else {
                    QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE
                }
            }

            override fun fetchEndCheck(
                line: String,
                currentLineNumber: Int,
                targetLine: String,
                targetLineNumber: Int,
                context: MutableSet<String>,
            ): QFetchEnd = if (currentLineNumber >= targetLineNumber) {
                val nIndentThis = line.qCountLeftSpace()
                val nIndentTarget = targetLine.qCountLeftSpace()

                if (currentLineNumber == targetLineNumber && line.trimStart()
                        .startsWith("\"\"\"") && line.qCountOccurrence("\"\"\"") == 1
                ) {
                    // """               <<< targetLine
                    // some text
                    // """ shouldBe """
                    // some text
                    // """

                    // """
                    // some text
                    // """.log           <<< targetLine
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (line.qEndsWith(""".* should[a-zA-Z]+ ""${'"'}""".re)) {
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (line.endsWith("{") || line.endsWith("(") || line.endsWith(".")) {
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                } else if (nIndentThis == nIndentTarget) {
                    QFetchEnd.END_WITH_THIS_LINE
                } else {
                    QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
                }
            } else {
                QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE
            }
        }
    }
}

// CallChain[size=12] = LineNumberReader.qFetchLinesBetween() <-[Call]- LineNumberReader.qFetchTarge ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private fun LineNumberReader.qFetchLinesBetween(
    lineNumberStartInclusive: Int,
    lineNumberEndInclusive: Int,
): List<String> {
    var fetching = false
    val lines = mutableListOf<String>()

    while (true) {
        val n = this.lineNumber + 1
        val line = this.readLine() ?: break

        if (n == lineNumberStartInclusive) {
            fetching = true
            lines += line
        } else if (fetching) {
            lines += line

            if (n == lineNumberEndInclusive) {
                break
            }
        }
    }

    return lines
}

// CallChain[size=12] = TargetSurroundingLines <-[Ref]- LineNumberReader.qFetchTargetSurroundingLine ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal class TargetSurroundingLines(
    val targetLineNumber: Int,
    val startLineNumber: Int,
    val endLineNumber: Int,
    val targetLine: String,
    val linesBeforeTargetLine: List<String>,
    val linesAfterTargetLine: List<String>,
) {
    // CallChain[size=11] = TargetSurroundingLines.linesBetween() <-[Call]- LineNumberReader.qFetchLines ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun linesBetween(lineNumberStartInclusive: Int, lineNumberEndInclusive: Int): List<String> {
        val lines = mutableListOf<String>()

        lines += linesBeforeTargetLine
        lines += targetLine
        lines += linesAfterTargetLine

        val startIdx = lineNumberStartInclusive - startLineNumber
        val endIdx = lineNumberEndInclusive - startLineNumber

        return lines.subList(startIdx, endIdx + 1)
    }
}

// CallChain[size=11] = LineNumberReader.qFetchTargetSurroundingLines() <-[Call]- LineNumberReader.q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private fun LineNumberReader.qFetchTargetSurroundingLines(
    targetLineNumber: Int,
    numLinesBeforeTargetLine: Int = 10,
    numLinesAfterTargetLine: Int = 10,
): TargetSurroundingLines {
    val start = max(1, targetLineNumber - numLinesBeforeTargetLine)
    val end = targetLineNumber + numLinesAfterTargetLine

    val lines = qFetchLinesBetween(start, end)

    return TargetSurroundingLines(
        targetLineNumber = targetLineNumber,
        startLineNumber = start,
        endLineNumber = end,
        targetLine = lines[targetLineNumber - start],
        linesBeforeTargetLine = lines.subList(0, targetLineNumber - start),
        linesAfterTargetLine = lines.subList(targetLineNumber - start + 1, lines.size)
    )
}

// CallChain[size=10] = LineNumberReader.qFetchLinesAround() <-[Call]- Path.qFetchLinesAround() <-[C ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private fun LineNumberReader.qFetchLinesAround(
    file: Path,
    targetLineNumber: Int,
    targetLine: String,
    fetchRule: QFetchRule = QFetchRule.SMART_FETCH,
    lineSeparator: QLineSeparator = QLineSeparator.LF,
): String {
    val surroundingLines = qFetchTargetSurroundingLines(
        targetLineNumber,
        fetchRule.numLinesBeforeTargetLine,
        fetchRule.numLinesAfterTargetLine
    )
    val context: MutableSet<String> = mutableSetOf()

    var start: Int = -1
    var end: Int = -1

    val checkStartLines = mutableListOf<String>()
    checkStartLines += surroundingLines.linesBeforeTargetLine
    checkStartLines += targetLine

    val checkEndLines = mutableListOf<String>()
    checkEndLines += targetLine
    checkEndLines += surroundingLines.linesAfterTargetLine

    for ((i, line) in checkStartLines.asReversed().withIndex()) {
        val curLineNumber = targetLineNumber - i

        val check = fetchRule.fetchStartCheck(
            line,
            curLineNumber,
            targetLine,
            targetLineNumber,
            context
        )

        when (check) {
            QFetchStart.START_FROM_PREVIOUS_LINE -> {
                start = curLineNumber - 1
                break
            }

            QFetchStart.START_FROM_THIS_LINE -> {
                start = curLineNumber
                break
            }

            QFetchStart.START_FROM_NEXT_LINE -> {
                start = curLineNumber + 1
                break
            }

            QFetchStart.FETCH_THIS_LINE_AND_GO_TO_PREVIOUS_LINE -> {
            }
        }
    }

    if (start == -1) {
        start = max(0, targetLineNumber - fetchRule.numLinesBeforeTargetLine)
    }

    for ((i, line) in checkEndLines.withIndex()) {
        val curLineNumber = targetLineNumber + i

        val check = fetchRule.fetchEndCheck(
            line,
            curLineNumber,
            targetLine,
            targetLineNumber,
            context
        )

        when (check) {
            QFetchEnd.END_WITH_PREVIOUS_LINE -> {
                end = curLineNumber - 1
                break
            }

            QFetchEnd.END_WITH_THIS_LINE -> {
                end = curLineNumber
                break
            }

            QFetchEnd.END_WITH_NEXT_LINE -> {
                end = curLineNumber + 1
                break
            }

            QFetchEnd.FETCH_THIS_LINE_AND_GO_TO_NEXT_LINE -> {
            }
        }
    }

    if (end == -1) {
        end = targetLineNumber + fetchRule.numLinesAfterTargetLine
    }

    return try {
        surroundingLines.linesBetween(start, end).joinToString(lineSeparator.value)
    } catch (e: Exception) {
        QE.FetchLinesFail.throwItFile(file)
    }
}

// CallChain[size=10] = Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtF ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Path.qReader(
    charset: Charset = Charsets.UTF_8,
    buffSize: Int = qBUFFER_SIZE,
    opts: QFlag<QOpenOpt> = QFlag.none(),
): LineNumberReader {
    return LineNumberReader(reader(charset, *opts.toOptEnums()), buffSize)
}

// CallChain[size=9] = Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLin ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Path.qFetchLinesAround(
    lineNumber: Int,
    fetchRule: QFetchRule = QFetchRule.SMART_FETCH,
    charset: Charset = Charsets.UTF_8,
    lineSeparator: QLineSeparator = this.qLineSeparator(charset),
): String {
    val reader = qReader(charset)

    try {
        // TODO optimization
        val targetLine = qLineAt(lineNumber, charset)

        if (fetchRule == QFetchRule.SINGLE_LINE) return targetLine

        val fetchedLines = reader.use {
            it.qFetchLinesAround(this, lineNumber, targetLine, fetchRule, lineSeparator)
        }

        return fetchedLines
    } catch (e: Exception) {
        QE.FetchLinesFail.throwItBrackets("File", this, "LineNumber", lineNumber, e = e)
    }
}

// CallChain[size=2] = Path.qNumberOfLines() <-[Call]- QCompactLib.createStat()[Root]
internal fun Path.qNumberOfLines(): Int {
    return this.useLines { lines ->
        lines.count()
    }
}

// CallChain[size=10] = Path.qLineAt() <-[Call]- Path.qFetchLinesAround() <-[Call]- qSrcFileLinesAtF ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Path.qLineAt(
    lineNumber: Int,
    charset: Charset = Charsets.UTF_8,
): String {
    bufferedReader(charset).use { reader ->
        var n = 0
        var line: String? = reader.readLine()

        while (line != null) {
            n++

            if (n == lineNumber) {
                return line
            }

            line = reader.readLine()
        }

        QE.LineNumberExceedsMaximum.throwItBrackets("File", this.absolutePathString(), "TargetLineNumber", lineNumber)
    }
}

// CallChain[size=11] = Path.qListPrintRecursive() <-[Call]- Path.qHashDir() <-[Call]- Path.qHash()  ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
/**
 * List files recursively with filter
 */
internal fun Path.qListPrintRecursive(
    fileFilter: (Path) -> Boolean = { true },
    dirFilter: (Path) -> Boolean = { true },
    maxDepth: Int = 20,
    followLinks: Boolean = false,
): List<Path> {
//    this.listDirectoryEntries(glob)
    return if (!Files.exists(this)) {
        emptyList()
    } else {
        val files = mutableListOf<Path>()

        val opts =
            if (followLinks) EnumSet.of(FileVisitOption.FOLLOW_LINKS) else EnumSet.noneOf(FileVisitOption::class.java)

        Files.walkFileTree(
            this, opts, maxDepth,
            object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    return if (!dirFilter.invoke(dir)) {
                        FileVisitResult.SKIP_SUBTREE
                    } else FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (!Files.isRegularFile(file) || !fileFilter.invoke(file)) {
                        return FileVisitResult.CONTINUE
                    }

                    files.add(file)

                    return FileVisitResult.CONTINUE
                }
            }
        )

        files
    }
}

// CallChain[size=10] = Path.qHashDir() <-[Call]- Path.qHash() <-[Call]- Path.qCopyFileTo() <-[Call] ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
private fun Path.qHashDir(
    algorithm: QHash = QHash.DEFAULT,
    buffSize: Int = qBUFFER_SIZE,
    modifiedDateHash: Boolean = true,
    additionalData: String? = null,
): String {
    val allRegularFiles = this.qListPrintRecursive()

    return allRegularFiles.qHash(algorithm, buffSize, modifiedDateHash, additionalData)
}

// CallChain[size=11] = List<Path>.qHash() <-[Call]- Path.qHashDir() <-[Call]- Path.qHash() <-[Call] ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun List<Path>.qHash(
    algorithm: QHash = QHash.DEFAULT,
    buffSize: Int = qBUFFER_SIZE,
    modifiedDateHash: Boolean = true,
    additionalData: String? = null,
): String {
    val md = MessageDigest.getInstance(algorithm.str)
    val out = DigestOutputStream(OutputStream.nullOutputStream(), md)

    this.forEach { file ->
        if (!file.isRegularFile()) return@forEach

        if (!modifiedDateHash) {
            BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ), buffSize).use { input ->
                input.transferTo(out)
                additionalData?.byteInputStream()?.transferTo(out)
            }
        } else {
            out.write(Files.getLastModifiedTime(file).hashCode())
        }
    }

    val fx = "%0${md.digestLength * 2}x"
    return String.format(fx, BigInteger(1, md.digest()))
}

// CallChain[size=9] = Path.qHash() <-[Call]- Path.qCopyFileTo() <-[Call]- QBackupFile.createBackup( ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qHash(
    algorithm: QHash = QHash.DEFAULT,
    buffSize: Int = qBUFFER_SIZE,
    useModifiedDateForDir: Boolean = true,
    additionalData: String? = null,
): String {
    if (!this.exists())
        return ""

    return if (Files.isDirectory(this)) {
        this.qHashDir(algorithm, buffSize, useModifiedDateForDir, additionalData)
    } else {
        this.qHashFile(algorithm, buffSize, additionalData)
    }
}

// CallChain[size=11] = QFType <-[Ref]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() <-[Call ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal enum class QFType {
    // CallChain[size=15] = QFType.Any <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    Any,
    // CallChain[size=11] = QFType.File <-[Call]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() < ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    File,
    // CallChain[size=15] = QFType.Dir <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    Dir,
    // CallChain[size=15] = QFType.SymLink <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Pa ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    SymLink,
    // CallChain[size=15] = QFType.FileOrDir <-[Call]- QFType.matches() <-[Call]- Path.qSeq() <-[Call]-  ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    FileOrDir;

    // CallChain[size=14] = QFType.matches() <-[Call]- Path.qSeq() <-[Call]- Path.qList() <-[Call]- Path ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun matches(path: Path?, followSymLink: Boolean = true): Boolean {
        if (path == null) return false

        return when (this) {
            Any -> true
            File -> if (followSymLink) path.isRegularFile() else path.isRegularFile(LinkOption.NOFOLLOW_LINKS)
            Dir -> if (followSymLink) path.isDirectory() else path.isDirectory(LinkOption.NOFOLLOW_LINKS)
            FileOrDir -> return if (followSymLink) {
                path.isRegularFile() || path.isDirectory()
            } else {
                path.isRegularFile(LinkOption.NOFOLLOW_LINKS) || path.isDirectory(LinkOption.NOFOLLOW_LINKS)
            }

            SymLink -> return path.isSymbolicLink()
        }
    }
}

// CallChain[size=4] = Path.qQuoteArg() <-[Call]- QGit.clone_sparse_checkout() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qQuoteArg(shell: QShell = QShell.DEFAULT): String =
    pathString.qQuoteArg(shell)

// CallChain[size=5] = Path.qIsEmptyDir() <-[Call]- Path.qIsNotEmpty() <-[Call]- QGit.clone_sparse_checkout() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qIsEmptyDir(): Boolean {
    return qList(QFType.Any).isEmpty()
}

// CallChain[size=4] = Path.qIsNotEmpty() <-[Call]- QGit.clone_sparse_checkout() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qIsNotEmpty(): Boolean {
    return !qIsEmptyDir()
}

// CallChain[size=4] = Path.qDirOrFileSizeStr() <-[Call]- QGit.gitHubDownloadSingleDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qDirOrFileSizeStr(followSymLink: Boolean = false, maxDepth: Int = Int.MAX_VALUE): String {
    return if (this.isDirectory()) {
        this.qDirSizeStr(followSymLink = followSymLink, maxDepth = maxDepth)
    } else {
        this.qFileSizeStr()
    }
}

// CallChain[size=3] = Path.qImgSize() <-[Call]- QMyCompactLib.exampleSection <-[Ref]- QReadmeScope.defaultExampleSection()[Root]
internal fun Path.qImgSize(): Dimension {
    val img = ImageIO.read(this.toFile())
    return Dimension(img.width, img.height)
}

// CallChain[size=5] = Path.qFileSizeStr() <-[Call]- Path.qDirOrFileSizeStr() <-[Call]- QGit.gitHubD ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qFileSizeStr() = fileSize().qToSizeString()

// CallChain[size=5] = Path.qDirSizeStr() <-[Call]- Path.qDirOrFileSizeStr() <-[Call]- QGit.gitHubDo ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Path.qDirSizeStr(followSymLink: Boolean = false, maxDepth: Int = Int.MAX_VALUE): String {
    return this.qList(QFType.Any, maxDepth = maxDepth, followSymLink = followSymLink).sumOf { it.fileSize() }
        .qToSizeString()
}

// CallChain[size=2] = Path.qCopyFileIntoDir() <-[Call]- QCompactLib.createLibrary()[Root]
internal fun Path.qCopyFileIntoDir(
    destDir: Path,
    ifExists: QIfExistsCopyFile = QIfExistsCopyFile.Overwrite,
    createParentDirs: Boolean = true
): Path {
    if (!destDir.exists()) {
        if (createParentDirs)
            destDir.qCreateDir()
        else
            QE.DirectoryNotFound.throwItDir(destDir)
    }

    if (!destDir.isDirectory())
        QE.ShouldBeDirectory.throwItFile(this)

    return qCopyFileTo(destDir sep name, ifExists)
}

// CallChain[size=4] = Path.qMoveFileTo() <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qMoveFileTo(
    destPath: Path,
    ifExistsDestFile: QIfExistsCopyFile = QIfExistsCopyFile.Overwrite,
    atomic: Boolean = true,
    createParentDirs: Boolean = true
): Path {
    val parentDir = destPath.parent

    if (!parentDir.exists()) {
        if (createParentDirs)
            parentDir.qCreateDir()
        else
            QE.DirectoryNotFound.throwItDir(parentDir)
    }

    if (!this.exists()) QE.FileNotFound.throwItFile(this)

    var options: QFlag<QCopyOpt> = QFlag.none()
    if (atomic) options += QCopyOpt.ATOMIC_MOVE
    if (ifExistsDestFile == QIfExistsCopyFile.Overwrite || ifExistsDestFile == QIfExistsCopyFile.OverwriteIfDifferentHash) options += QCopyOpt.REPLACE_EXISTING

    return try {
        Files.move(this, destPath, *options.toOptEnums())
    } catch (e: FileAlreadyExistsException) {
        when (ifExistsDestFile) {
            QIfExistsCopyFile.Overwrite -> {
                try {
                    Files.copy(this, destPath, *options.toOptEnums())
                } catch (e: kotlin.io.FileAlreadyExistsException) {
                    QE.FileAlreadyExists.throwItFile(this)
                }
            }

            QIfExistsCopyFile.ChangeFileNameAndRetry -> {
                val retryPath = this.qIfExistsRetryPath()

                return try {
                    Files.copy(this, retryPath, *options.toOptEnums())
                } catch (e: kotlin.io.FileAlreadyExistsException) {
                    QE.FileAlreadyExists.throwItFile(this)
                }
            }

            QIfExistsCopyFile.OverwriteIfDifferentHash -> {
                if (this.qHash() == destPath.qHash()) {
                    return destPath
                } else {
                    try {
                        Files.copy(this, destPath, *options.toOptEnums())
                    } catch (e: kotlin.io.FileAlreadyExistsException) {
                        QE.FileAlreadyExists.throwItFile(this)
                    }
                }
            }

            QIfExistsCopyFile.RaiseException -> {
                QE.FileAlreadyExists.throwItFile(this)
            }

            QIfExistsCopyFile.DoNothing -> {
                return destPath
            }
        }
    }
}

// CallChain[size=9] = Path.qWithNewExtension() <-[Call]- Path.qCreateZip() <-[Call]- QBackupFile.cr ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qWithNewExtension(ext: String): Path {
    val fileNameWithExtension = this.name

    val dotIdx = fileNameWithExtension.lastIndexOf('.')

    return if (dotIdx < 0) {
        this.resolveSibling(fileNameWithExtension + ext.qWithDotPrefix())
    } else {
        this.resolveSibling(fileNameWithExtension.substring(0, dotIdx) + ext.qWithDotPrefix())
    }
}

// CallChain[size=7] = qILLEGAL_FILENAMES_WINDOWS <-[Call]- String.qEscapeFileName() <-[Call]- Strin ... nameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
// https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file
internal const val qILLEGAL_FILENAMES_WINDOWS =
    "CON|PRN|AUX|NUL|COM1|COM2|COM3|COM4|COM5|COM6|COM7|COM8|COM9|LPT1|LPT2|LPT3|LPT4|LPT5|LPT6|LPT7|LPT8|LPT9"

// CallChain[size=6] = String.qEscapeFileName() <-[Call]- String.qRunShellScript() <-[Call]- qRunMet ... nameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun String.qEscapeFileName(): String {
    var fileName = this.replace("^\\.+", "_")
        .replace("[\\\\/:*?\"<>|]".re, "_")
        .replace("\\.+$".re, "_")
        .trim { it <= ' ' }
    fileName =
        fileName.replace("(?i)(^($qILLEGAL_FILENAMES_WINDOWS)$|^($qILLEGAL_FILENAMES_WINDOWS)(?=\\.))".re, "$1_")
//        fileName = fileName.replace("(^($qILLEGAL_FILENAMES_WINDOWS)$|^($qILLEGAL_FILENAMES_WINDOWS)(?=\\.)$)", "_", true)

    return fileName
}

// CallChain[size=6] = Long.qToSizeString() <-[Call]- Path.qFileSizeStr() <-[Call]- Path.qDirOrFileS ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Long.qToSizeString(): String {
    val size = this
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}

// CallChain[size=7] = Int.qToSizeString() <-[Call]- QGitObj.diskSizeStr <-[Call]- QGitObj.toString( ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Int.qToSizeString(): String {
    return this.toLong().qToSizeString()
}

// CallChain[size=10] = Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLines ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Collection<Path>.qFind(nameMatcher: QM, type: QFType = QFType.File, maxDepth: Int = 1): Path? {
    for (path in this) {
        val found = path.qFind(nameMatcher, type, maxDepth)
        if (found != null) return found
    }

    return null
}

// CallChain[size=11] = Path.qFind() <-[Call]- Collection<Path>.qFind() <-[Call]- qSrcFileAtFrame()  ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Path.qFind(nameMatcher: QM, type: QFType = QFType.File, maxDepth: Int = 1): Path? {
    return try {
        qList(type, maxDepth = maxDepth) {
            it.name.qMatches(nameMatcher)
        }.firstOrNull()
    } catch (e: NoSuchElementException) {
        null
    }
}

// CallChain[size=9] = Path.qListRecursive() <-[Call]- Path.qListBackupFiles() <-[Call]- QBackupHelp ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qListRecursive(
    type: QFType = QFType.File,
    followSymLink: Boolean = false,
    filter: (Path) -> Boolean = { true },
): List<Path> {
    return qList(
        type = type, followSymLink = followSymLink, maxDepth = qFILE_LIST_RECURSIVE_MAX_DEPTH, filter = filter
    )
}

// CallChain[size=8] = Path.qListByMatch() <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <- ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Path.qListByMatch(
    nameMatch: QM,
    type: QFType = QFType.File,
    maxDepth: Int = 1,
    followSymLink: Boolean = false,
): List<Path> {
    return qList(
        type, maxDepth = maxDepth, followSymLink = followSymLink
    ) {
        it.name.qMatches(nameMatch)
    }
}

// CallChain[size=12] = Path.qList() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind() <-[Ca ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Path.qList(
    type: QFType = QFType.File,
    maxDepth: Int = 1,
    followSymLink: Boolean = false,
    sortWith: ((Path, Path) -> Int)? = Path::compareTo,
    filter: (Path) -> Boolean = { true },
    // TODO https://stackoverflow.com/a/66996768/5570400
    // errorContinue: Boolean = true
): List<Path> {
    return qSeq(
        type = type,
        maxDepth = maxDepth,
        followSymLink = followSymLink,
        sortWith = sortWith,
        filter = filter
    ).toList()
}

// CallChain[size=13] = Path.qSeq() <-[Call]- Path.qList() <-[Call]- Path.qFind() <-[Call]- Collecti ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun Path.qSeq(
    type: QFType = QFType.File,
    maxDepth: Int = 1,
    followSymLink: Boolean = false,
    sortWith: ((Path, Path) -> Int)? = Path::compareTo,
    filter: (Path) -> Boolean = { true },
    // TODO https://stackoverflow.com/a/66996768/5570400
    // errorContinue: Boolean = true
): Sequence<Path> {
    if (!this.isDirectory())
        return emptySequence()

    val fvOpt = if (followSymLink) arrayOf(FileVisitOption.FOLLOW_LINKS) else arrayOf()

    val seq = Files.walk(this, maxDepth, *fvOpt).asSequence().filter {
        if (it == this) return@filter false

        type.matches(it, followSymLink) && filter(it)
    }

    return if (sortWith != null) {
        seq.sortedWith(sortWith)
    } else {
        seq
    }
}

// CallChain[size=4] = Path.qCreateParentDirs() <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qCreateParentDirs() {
    if (this.parent != null) {
        Files.createDirectories(this.parent).qaNotNull()
    }
}

// CallChain[size=2] = Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
internal suspend fun Path.qOpenEditor(editor: QMyEditor = QMyEditor.Idea, vararg options: String) =
    withContext(Dispatchers.IO) {
        try {
            if (editor == QMyEditor.PlatformDefault) {
                try {
                    // https://stackoverflow.com/questions/6273221/open-a-text-file-in-the-default-text-editor-via-java
                    Desktop.getDesktop().edit(this@qOpenEditor.toFile())
                } catch (e: Exception) {
                    val cmd = if (qIsWindows()) "notepad.exe" else "vi"

                    (listOf(cmd) + options + pathString).qExec()
                }
            } else {
                (listOf(editor.cmd) + options + pathString).qRunInShell(
                    shell = QShell.CMD
                )
            }
        } catch (e: Exception) {
            QE.FileOpenFail.throwItFile(this@qOpenEditor, e = e)
        }
    }