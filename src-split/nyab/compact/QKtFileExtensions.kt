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

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import nyab.util.qCacheIt
import nyab.util.qLineAt
import nyab.util.qReadFile
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
fun KtFile.qSrcLineAt(lineNum: Int, charset: Charset = Charsets.UTF_8): String =
    qCacheIt("${this.qPath}:$lineNum#FileLine") {
        val content = this.qPath.qReadFile(charset, useCache = true)
        val lineContent = content.qLineAt(lineNum - 1)
        """${this.qPath}:$lineNum >>>
        |$lineContent""".trimMargin()
    }

// << Root of the CallChain >>
val KtFile.qElementsAll: List<KtElement>
    get() = collectDescendantsOfType<KtElement>().distinct()

// << Root of the CallChain >>
val KtFile.qElementsTopLevel: List<KtElement>
    get() {
        return children.filterIsInstance<KtElement>()
//        collectDescendantsOfType<KtElement>().filter {
//            it.isTopLevelKtOrJavaMember()
//        }
    }

// << Root of the CallChain >>
val KtFile.qPsiElementsCount: List<Pair<String, Int>>
    get() = collectDescendantsOfType<PsiElement>().groupingBy { it::class.simpleName!! }.eachCount().toList()
        .sortedBy { (_, v) -> v }

// << Root of the CallChain >>
val KtFile.qElementsCount: List<Pair<String, Int>>
    get() = collectDescendantsOfType<KtElement>().groupingBy { it::class.simpleName!! }.eachCount().toList()
        .sortedBy { (_, v) -> v }

// << Root of the CallChain >>
val KtFile.qFunctions: List<KtNamedFunction>
    get() = collectDescendantsOfType<KtNamedFunction>().distinct()

// << Root of the CallChain >>
val KtFile.qCalls: List<KtCallExpression>
    get() = collectDescendantsOfType<KtCallExpression>().distinct()

// << Root of the CallChain >>
/**
 * https://plugins.jetbrains.com/docs/intellij/virtual-file.html#what-can-i-do-with-a-psi-file
 *
 * > A VirtualFile (VF) is the IntelliJ Platform's representation of a file in a Virtual File System (VFS).
 * > Most commonly, a virtual file is a file in a local file system.
 * > However, the IntelliJ Platform supports multiple pluggable file system implementations,
 * > so virtual files can also represent classes in a JAR file,
 * > old revisions of files loaded from a version control repository, and so on.
 */
val KtFile.qPath: Path
    get() = Paths.get(virtualFilePath)

// << Root of the CallChain >>
fun KtFile.qReadContent(charset: Charset = Charsets.UTF_8): String = qCacheIt(this.qPath.name) {
    Files.readString(this.qPath, charset)
}