/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("FunctionName")

package nyab.util

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass
import nyab.conf.QE
import nyab.conf.QMyPath
import nyab.match.QM

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=6] = KClass<*>.qSrcFile <-[Call]- KClass<*>.qFqNameOfContainingFile() <-[Call]- KC ... nameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal val KClass<*>.qSrcFile: Path?
    get() = QMyPath.src_root.qFind(QM.exact(this.simpleName + ".kt"), maxDepth = 100)

// CallChain[size=2] = Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
internal fun Path.qConvertContent(
    outFile: Path = this,
    charset: Charset = Charsets.UTF_8,
    convert: (oldContent: String) -> String
): String {
    val oldContent: String = Files.readString(this, charset)

    val newContent = oldContent.let(convert)

    outFile.qWrite(newContent, ifExists = QIfExistsWrite.OverwriteAtomic, charset = charset)

    return newContent
}