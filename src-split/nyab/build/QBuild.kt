/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.build

import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nyab.conf.QE
import nyab.conf.QMyEditor
import nyab.conf.QMyPath
import nyab.match.QM
import nyab.util.QIfExistsCopy
import nyab.util.QShell
import nyab.util.qExec
import nyab.util.qIsWindows
import nyab.util.qRunInShell
import nyab.util.throwIt
import nyab.util.throwItFile

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = qCurClassPathJarLibraries <-[Call]- QCompactLibAnalysis.myClassPaths[Root]
internal val qCurClassPathJarLibraries: List<Path> by lazy {
    // https://stackoverflow.com/a/18627625/5570400
    qClassPaths().filter { it.name.endsWith(".jar") }
}

// CallChain[size=3] = qClassPaths() <-[Call]- qCurClassPathJarLibraries <-[Call]- QCompactLibAnalysis.myClassPaths[Root]
internal fun qClassPaths(): List<Path> {
    val cp = System.getProperty("java.class.path")
    val cps: Array<String> = cp.split(File.pathSeparator).dropLastWhile { it.isEmpty() }.toTypedArray()
    return cps.map { Paths.get(it) }.toList()
}

// CallChain[size=3] = qOpenBrowser() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal fun qOpenBrowser(uri: String) {
    try {
        Desktop.getDesktop().browse(URI(uri))
    } catch (e: Exception) {
        QE.OpenBrowserFail.throwIt(e = e)
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