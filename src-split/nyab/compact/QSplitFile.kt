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
import java.nio.file.Paths
import java.util.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFileAnnotationList
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPackageDirective

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QSplitFile(val ktFile: KtFile, val analysisCtx: QAnalysisContext) {
    // << Root of the CallChain >>
    private val containingCompactElements = mutableListOf<QTopLevelCompactElement>()

    // << Root of the CallChain >>
    private val srcSetType = if (analysisCtx.isTest) QSrcSetType.TestSplit else QSrcSetType.MainSplit

    // << Root of the CallChain >>
    fun addElement(compactEle: QTopLevelCompactElement) {
        if (!containingCompactElements.contains(compactEle)) {
            containingCompactElements.add(compactEle)
        }
    }

    // << Root of the CallChain >>
    fun toSrcCode(): String {
        val anno = lib.splitFileAnnotation(QSplitFileAnnotationCreationScope(analysisCtx, analysisCtx.isTest, ktFile))

        val pkg = ktFile.packageDirective?.text ?: ""

        return lib.srcHeader(QLicenseScope(lib, Calendar.getInstance().get(Calendar.YEAR), lib.author)) + "\n\n" +
                if (anno.isNotEmpty()) {
                    anno + "\n\n"
                } else {
                    ""
                } +
                if (pkg.isNotEmpty()) {
                    pkg + "\n\n"
                } else {
                    ""
                } +
                if (imports.isNotEmpty()) {
                    imports.joinToString("\n") {
                        "import $it"
                    } + "\n\n"
                } else {
                    ""
                } +
                lib.betweenImportsAndFirstElementSplit + "\n\n" +
                containingCompactElements.filter {
                    it.topLevelElement !is KtImportList &&
                            it.topLevelElement !is KtFileAnnotationList &&
                            it.topLevelElement !is KtPackageDirective
                }.sortedBy {
                    it.rawSrcTextOffset
                }.joinToString("\n\n") {
                    it.toSrcCode(srcSetType = srcSetType)
                }
    }

    // << Root of the CallChain >>
    val imports: List<String>
        get() {
            val importPathList = ktFile.qImportPathList()
            return importPathList.filter { importPath ->
                analysisCtx.lib.splitSrcFileImportFilter(
                    QSplitImportFilterScope(
                        importPath,
                        ktFile,
                        ktFile.name,
                        lib,
                        analysisCtx,
                        srcSetType
                    )
                )
            }.distinct().sorted()
        }

    // << Root of the CallChain >>
    val lib: QCompactLib
        get() = analysisCtx.lib

    // << Root of the CallChain >>
    val destPath: Path
        get() {
            val rootDir = if (!analysisCtx.isTest) lib.destSplitMainDir else lib.destSplitTestDir
            return if (ktFile.packageFqName.toString() == "<root>") {
                rootDir.resolve(
                    Paths.get(ktFile.name)
                )
            } else {
                rootDir.resolve(
                    Paths.get(ktFile.packageFqName.toString().replace(".", "/")).resolve(ktFile.name)
                )
            }
        }
}