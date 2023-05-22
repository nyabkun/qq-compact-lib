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

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.coroutines.resume
import kotlin.io.path.absolutePathString
import kotlin.reflect.KFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import nyab.conf.QE

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=9] = QRunResultType <-[Ref]- QRunResult.type <-[Propag]- QRunResult.QRunResult() < ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal enum class QRunResultType {
    // CallChain[size=9] = QRunResultType.Success <-[Call]- QRunResult.type <-[Propag]- QRunResult.QRunR ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    Success,
    // CallChain[size=9] = QRunResultType.Error <-[Call]- QRunResult.type <-[Propag]- QRunResult.QRunRes ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    Error,
    // CallChain[size=9] = QRunResultType.CannotExecute <-[Call]- QRunResult.type <-[Propag]- QRunResult ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    CannotExecute
}

// CallChain[size=5] = QRunResult <-[Ref]- QGit.String.runCmd() <-[Call]- QGit.config_get_remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal open class QRunResult(private val procResult: QProcResult, val exitCode: Int, val cmds: List<String>) :
    QProcResultI by procResult {
    // CallChain[size=2] = QRunResult.success <-[Call]- QCompactLibResult.doGitTask()[Root]
    val success: Boolean
        get() = exitCode == 0

    // CallChain[size=8] = QRunResult.type <-[Propag]- QRunResult.QRunResult() <-[Call]- List<String>.qE ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val type: QRunResultType = when (exitCode) {
        0 -> {
            QRunResultType.Success
        }

        126 -> {
            QRunResultType.CannotExecute
        }

        else -> {
            QRunResultType.Error
        }
    }

    // CallChain[size=8] = QRunResult.isExecuted() <-[Propag]- QRunResult.QRunResult() <-[Call]- List<St ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    fun isExecuted() = this !== NotExecuted

    companion object {
        // CallChain[size=8] = QRunResult.NotExecuted <-[Propag]- QRunResult.QRunResult() <-[Call]- List<Str ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
        val NotExecuted: QRunResult = QRunResultNotExecuted()
    }
}

// CallChain[size=9] = QRunResultNotExecuted <-[Call]- QRunResult.NotExecuted <-[Propag]- QRunResult ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
private class QRunResultNotExecuted(exitCode: Int = -1, cmds: List<String> = emptyList()) :
    QRunResult(procResult = QProcResult("", "", null), exitCode, cmds)

// CallChain[size=9] = QProcResultI <-[Ref]- QProcResult <-[Ref]- QProcStreamHandlerI.handleStream() ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal interface QProcResultI {
    // CallChain[size=10] = QProcResultI.output <-[Propag]- QProcResultI <-[Ref]- QProcResult <-[Ref]- Q ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val output: String
    // CallChain[size=10] = QProcResultI.error <-[Propag]- QProcResultI <-[Ref]- QProcResult <-[Ref]- QP ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val error: String
    // CallChain[size=10] = QProcResultI.anything <-[Propag]- QProcResultI <-[Ref]- QProcResult <-[Ref]- ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val anything: Any?
}

// CallChain[size=8] = QProcResult <-[Ref]- QProcStreamHandlerI.handleStream() <-[Propag]- QProcStre ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal data class QProcResult(
    override val output: String,
    override val error: String,
    override val anything: Any? = null,
) : QProcResultI

// CallChain[size=7] = QProcStreamHandler <-[Call]- QProcStreamHandlerI.CONSOLE <-[Call]- String.qRu ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal open class QProcStreamHandler(
    override val redirectErrorOutStream: Boolean = false,
    val procInCharset: Charset = Charsets.UTF_8,
    val procOutCharset: Charset = procInCharset,
    val consoleOut: OutputStream = System.out,
    val consoleOutErr: OutputStream = System.err,
    val consoleCharset: Charset = Charset.forName(System.getProperty("file.encoding")),
) : QProcStreamHandlerI {
    // CallChain[size=8] = QProcStreamHandler.handleStream() <-[Propag]- QProcStreamHandler <-[Call]- QP ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    @Suppress("UNUSED_PARAMETER")
    private suspend fun handleStream(
        procOut: InputStreamReader,
        procErr: InputStreamReader,
        procIn: OutputStreamWriter,
    ): QProcResult = withContext(Dispatchers.IO) {
        val procOutDeferred = async {
            procOut.qRedirectAndGet(
                OutputStreamWriter(consoleOut, consoleCharset)
            )
        }

        val procErrDeferred = if (!redirectErrorOutStream) {
            async {
                procErr.qRedirectAndGet(
                    OutputStreamWriter(consoleOutErr, consoleCharset)
                )
            }
        } else {
            async { "" }
        }

        val out = procOutDeferred.await()
        val err = procErrDeferred.await()

        QProcResult(out, err)
    }

    // CallChain[size=8] = QProcStreamHandler.handleStream() <-[Propag]- QProcStreamHandler <-[Call]- QP ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    override suspend fun handleStream(procOut: InputStream, procErr: InputStream, procIn: OutputStream): QProcResult =
        handleStream(
            InputStreamReader(procOut, procOutCharset),
            InputStreamReader(procErr, procOutCharset),
            OutputStreamWriter(procIn, procInCharset)
        )
}

// CallChain[size=7] = QProcStreamHandlerSilent <-[Call]- QProcStreamHandlerI.SILENT <-[Call]- Strin ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal open class QProcStreamHandlerSilent(
    override val redirectErrorOutStream: Boolean = false,
    val procInCharset: Charset = Charsets.UTF_8,
    val procOutCharset: Charset = procInCharset,
) : QProcStreamHandlerI {

    // CallChain[size=8] = QProcStreamHandlerSilent.handleStream() <-[Propag]- QProcStreamHandlerSilent. ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    @Suppress("UNUSED_PARAMETER")
    private suspend fun handleStream(
        procOut: InputStreamReader,
        procErr: InputStreamReader,
        procIn: OutputStreamWriter,
    ): QProcResult = withContext(Dispatchers.IO) {
        val procOutDeferred = async {
            procOut.qGet()
        }

        val procErrDeferred = if (!redirectErrorOutStream) {
            async {
                procErr.qGet()
            }
        } else {
            async { "" }
        }

        val out = procOutDeferred.await()
        val err = procErrDeferred.await()

        QProcResult(out, err)
    }

    // CallChain[size=8] = QProcStreamHandlerSilent.handleStream() <-[Propag]- QProcStreamHandlerSilent. ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    override suspend fun handleStream(procOut: InputStream, procErr: InputStream, procIn: OutputStream): QProcResult =
        handleStream(
            InputStreamReader(procOut, procOutCharset),
            InputStreamReader(procErr, procOutCharset),
            OutputStreamWriter(procIn, procInCharset)
        )
}

// CallChain[size=6] = QProcStreamHandlerI <-[Ref]- String.qRunInShell() <-[Call]- QGit.String.runCm ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal interface QProcStreamHandlerI {
    // CallChain[size=7] = QProcStreamHandlerI.redirectErrorOutStream <-[Propag]- QProcStreamHandlerI.SI ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val redirectErrorOutStream: Boolean

    // CallChain[size=7] = QProcStreamHandlerI.handleStream() <-[Propag]- QProcStreamHandlerI.SILENT <-[ ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun handleStream(procOut: InputStream, procErr: InputStream, procIn: OutputStream): QProcResult

    companion object {
        // CallChain[size=6] = QProcStreamHandlerI.SILENT <-[Call]- String.qRunInShell() <-[Call]- QGit.Stri ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
        val SILENT = QProcStreamHandlerSilent(redirectErrorOutStream = true)
        // CallChain[size=6] = QProcStreamHandlerI.CONSOLE <-[Call]- String.qRunInShell() <-[Call]- QGit.Str ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
        val CONSOLE = QProcStreamHandler()
        
    }
}

// CallChain[size=6] = List<String>.qExec() <-[Call]- String.qRunInShell() <-[Call]- QGit.String.run ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal suspend fun List<String>.qExec(
    pwd: Path = Paths.get("."),
    environments: Map<String, String>? = null,
    streamHandler: QProcStreamHandlerI = QProcStreamHandlerI.CONSOLE,
    throwsException: Boolean = false,
): QRunResult = coroutineScope<QRunResult> {
    val result = suspendCancellableCoroutine<QRunResult> { continuation ->
        if (this@qExec.isEmpty()) {
            // 126 = "Command invoked cannot execute"
            // https://stackoverflow.com/a/1535733/5570400
            // https://tldp.org/LDP/abs/html/exitcodes.html
            continuation.resume(QRunResult(QProcResult("", ""), 126, this@qExec))
            return@suspendCancellableCoroutine
        }

        val cmds = this@qExec

        // bug?
        // powershell does

        val pb = ProcessBuilder(cmds)
        pb.directory(pwd.toAbsolutePath().toFile())

        if (environments != null)
            pb.environment().putAll(environments)

        if (streamHandler.redirectErrorOutStream) {
            pb.redirectErrorStream(true)
        }

        val process = pb.start()

        continuation.invokeOnCancellation {
            process.destroy()
        }

        launch(Dispatchers.IO) {

            val result = streamHandler.handleStream(
                process.inputStream,
                process.errorStream,
                process.outputStream,
            )

            val exitCode = process.waitFor()

            continuation.resume(QRunResult(result, exitCode, cmds))
        }
    }

    if (throwsException && !result.success) {
        QE.CommandFail.throwItBrackets(
            "Cmd",
            result.cmds,
            "ErrOut",
            result.error,
            "StdOut",
            result.output
        )
    }

    result
}

// CallChain[size=6] = String.qMakeShellCmd() <-[Call]- String.qRunInShell() <-[Call]- QGit.String.r ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun String.qMakeShellCmd(shell: QShell = QShell.DEFAULT): List<String> {
    return listOf(*shell.cmd, this)
}

// CallChain[size=5] = qNO_QUOTE_ARGS <-[Call]- List<String>.qRunInShell() <-[Call]- QGit.List<String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
private val qNO_QUOTE_ARGS = listOf("<", "|", "&", "&&", "||")

// CallChain[size=4] = List<String>.qRunInShell() <-[Call]- QGit.List<String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal suspend fun List<String>.qRunInShell(
    pwd: Path = Paths.get("."),
    shell: QShell = QShell.DEFAULT,
    environments: Map<String, String>? = null,
    streamHandler: QProcStreamHandlerI = QProcStreamHandlerI.CONSOLE,
    quiet: Boolean = false,
    printCmd: QOut = QOut.NONE,
    throwsException: Boolean = false,
    noQuoteArgs: List<String> = qNO_QUOTE_ARGS,
): QRunResult {
    return qJoinAndQuoteShellArgs(shell, noQuoteArgs).qRunInShell(
        pwd = pwd,
        shell = shell,
        environments = environments,
        streamHandler = streamHandler,
        quiet = quiet,
        printCmd = printCmd,
        throwsException = throwsException
    )
}

// CallChain[size=5] = String.qRunInShell() <-[Call]- QGit.String.runCmd() <-[Call]- QGit.config_get_remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal suspend fun String.qRunInShell(
    pwd: Path = Paths.get("."),
    shell: QShell = QShell.DEFAULT,
    environments: Map<String, String>? = null,
    streamHandler: QProcStreamHandlerI = QProcStreamHandlerI.CONSOLE,
    quiet: Boolean = false,
    printCmd: QOut = QOut.NONE,
    throwsException: Boolean = false,
): QRunResult {
    if (printCmd != QOut.NONE) {
        printCmd.println(this.blue)
    }

    val cmd = qMakeShellCmd(shell)

    return cmd.qExec(
        pwd = pwd,
        environments = environments,
        streamHandler = if (quiet) QProcStreamHandlerI.SILENT else streamHandler,
        throwsException = throwsException
    )
}

// CallChain[size=9] = InputStreamReader.qGet() <-[Call]- QProcStreamHandlerSilent.handleStream() <- ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal suspend fun InputStreamReader.qGet(
    buffSize: Int = qBUFFER_SIZE,
): String =
    withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        val chars = CharArray(buffSize)
        while (coroutineContext.isActive) {
            val read = read(chars)
            if (read == -1 || !coroutineContext.isActive) {
                break
            }

            sb.append(String(chars, 0, read))
        }

        sb.toString()
    }

// CallChain[size=9] = InputStreamReader.qRedirectAndGet() <-[Call]- QProcStreamHandler.handleStream ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal suspend fun InputStreamReader.qRedirectAndGet(
    out: OutputStreamWriter,
    buffSize: Int = qBUFFER_SIZE,
    autoFlush: Boolean = true,
): String =
    withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        val chars = CharArray(buffSize)
        while (coroutineContext.isActive) {
            val read = read(chars)
            if (read == -1 || !coroutineContext.isActive) {
                break
            }

            out.write(chars, 0, read)

            sb.append(String(chars, 0, read))

            if (autoFlush)
                out.flush()
        }

        sb.toString()
    }

// CallChain[size=5] = List<String>.qJoinAndQuoteShellArgs() <-[Call]- List<String>.qRunInShell() <- ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun List<String>.qJoinAndQuoteShellArgs(
    shell: QShell = QShell.DEFAULT,
    noQuoteArgs: List<String> = listOf("<", "|", "&", "&&", "||")
): String {
    return joinToString(" ") { arg ->
        if (arg.qIsShellArgNeedsQuote() && !noQuoteArgs.any { it == arg }) {
            arg.qQuoteArg(shell)
        } else {
            arg
        }
    }
}

// CallChain[size=7] = QShell <-[Ref]- QShell.DEFAULT_PWSH <-[Call]- QGit.shell <-[Call]- QGit.Strin ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
@Suppress("SpellCheckingInspection")
internal enum class QShell(
    val charsToBeQuoted: CharArray,
    val charsToBeEscapedInString: CharArray,
    val quote: Char,
    val cmd: Array<String>,
    val withEscapeToWithoutEscapeInQuotedString: Array<Pair<String, String>>,
) {
    // CallChain[size=7] = QShell.CMD <-[Propag]- QShell.DEFAULT_PWSH <-[Call]- QGit.shell <-[Call]- QGi ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    // TODO Check
    CMD(
        cmd = arrayOf("cmd", "/c"),
        charsToBeQuoted = charArrayOf(' ', '\t', '\"', '<', '>', '&', '|', '^', '%'),
        charsToBeEscapedInString = charArrayOf('\"', '\n'),
        quote = '"',
        withEscapeToWithoutEscapeInQuotedString = arrayOf(
            "\\\\" to "\\",
            "\\t" to "\t",
            "\\b" to "\b",
            "\\n" to "\n",
            "\\r" to "\r",
            "\\f" to "\u000c",
            "\\'" to "\'",
            "\\\"" to "\"",
        ),
    ),
    // CallChain[size=7] = QShell.PWSH <-[Call]- QShell.DEFAULT_PWSH <-[Call]- QGit.shell <-[Call]- QGit ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    PWSH(
        cmd = arrayOf("powershell", "-NoP", "-c"),
        charsToBeQuoted = charArrayOf(' ', '\t', '\"', '<', '>', '&', '|', '^'),
        charsToBeEscapedInString = charArrayOf('\'', '\n', '#'),
        quote = '\'',
        withEscapeToWithoutEscapeInQuotedString = arrayOf(
            "``" to "`",
            "`t" to "\t",
            "`b" to "\b",
            "`n" to "\n",
            "`r" to "\r",
            "`f" to "\u000c",
            "`\"" to "\"",
        ),
    ),
    // CallChain[size=7] = QShell.BASH <-[Call]- QShell.DEFAULT_PWSH <-[Call]- QGit.shell <-[Call]- QGit ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    BASH(
        cmd = arrayOf("/bin/sh", "-c"),
        charsToBeQuoted = charArrayOf(' ', '\t', '\"', '<', '>', '&', '|', '^'),
        charsToBeEscapedInString = charArrayOf('\'', '\n'),
        quote = '\'',
        withEscapeToWithoutEscapeInQuotedString = arrayOf(
            "\\\\" to "\\",
            "\\t" to "\t",
            "\\b" to "\b",
            "\\n" to "\n",
            "\\r" to "\r",
            "\\f" to "\u000c",
            "\\'" to "\'",
            "\\\"" to "\"",
        ),
    ),
    // CallChain[size=7] = QShell.WIN_BASH <-[Propag]- QShell.DEFAULT_PWSH <-[Call]- QGit.shell <-[Call] ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    WIN_BASH(
        cmd = arrayOf("bash", "-c"),
        charsToBeQuoted = charArrayOf(' ', '\t', '\"', '<', '>', '&', '|', '^'),
        charsToBeEscapedInString = charArrayOf('\'', '\n'),
        quote = '\'',
        withEscapeToWithoutEscapeInQuotedString = arrayOf(
            "\\\\" to "\\",
            "\\t" to "\t",
            "\\b" to "\b",
            "\\n" to "\n",
            "\\r" to "\r",
            "\\f" to "\u000c",
            "\\'" to "\'",
            "\\\"" to "\"",
        ),
    );

    companion object {
        // CallChain[size=6] = QShell.DEFAULT <-[Call]- String.qRunInShell() <-[Call]- QGit.String.runCmd()  ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
        val DEFAULT: QShell = if (qIsWindows()) CMD else BASH
        // CallChain[size=6] = QShell.DEFAULT_PWSH <-[Call]- QGit.shell <-[Call]- QGit.String.runCmd() <-[Ca ... _remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
        val DEFAULT_PWSH: QShell = if (qIsWindows()) PWSH else BASH
    }
}

// CallChain[size=6] = String.qIsShellArgNeedsQuote() <-[Call]- List<String>.qJoinAndQuoteShellArgs( ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
private fun String.qIsShellArgNeedsQuote(shell: QShell = QShell.DEFAULT): Boolean {
    if (isEmpty()) return false

    val unquoted: String = this.qUnquoteArg()

    if (this != unquoted) // Already quoted
        return false

    return shell.charsToBeQuoted.any { this.contains(it) }
}

// CallChain[size=7] = String.qUnquoteArg() <-[Call]- String.qIsShellArgNeedsQuote() <-[Call]- List< ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun String.qUnquoteArg(shell: QShell = QShell.DEFAULT): String {
    if (!startsWith(shell.quote) || !endsWith(shell.quote) || length < 2) return this

    // "abc" -> abc
    val unquoted = substring(1, length - 1)

    return unquoted.qUnescapeString(
        *shell.withEscapeToWithoutEscapeInQuotedString
    )
}

// CallChain[size=6] = String.qQuoteArg() <-[Call]- List<String>.qJoinAndQuoteShellArgs() <-[Call]-  ... <String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun String.qQuoteArg(shell: QShell = QShell.DEFAULT): String {
    shell.charsToBeQuoted
    return shell.quote + this.qEscapeString(*shell.withEscapeToWithoutEscapeInQuotedString) + shell.quote
}