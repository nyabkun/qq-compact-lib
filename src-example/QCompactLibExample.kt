/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package compact

import kotlinx.coroutines.runBlocking
import nyab.compact.QCompactLibResult
import nyab.compact.QKtVisibilityChange
import nyab.compact.QTopLevelCompactElement
import nyab.compact.QVersionUpdateStrategy
import nyab.compact.qCompactLib
import nyab.util.QIfExistsWrite
import java.nio.file.Paths

fun main(): Unit = runBlocking {
    val result = build()

    // git task includes
    // - create local git repository
    // - commit
    // - create github repository
    // - push
    // - release
//    result.doGitTask()
}

private suspend fun build(): QCompactLibResult {
    val result = qCompactLib(
        libName = "hello-world-compact",
        baseFileName = "CompactHelloWorld",
        author = "nyabkun",
    ) {
        versionUpdateStrategy = QVersionUpdateStrategy.UpdateOnlyIfSplitSrcHashChanged

        repoDescription = """Kotlin library that can say Hello."""

        readmeDescription = """Kotlin library that can say Hello."""

        versionSuffix = "-alpha"

        references = ""

        srcDirs {
            main("rsc-test/src")
            test("rsc-test/src-test")
        }

        repoTopics = listOf("kotlin-library", "hello-world", "compact-library")

        createSingleFileSrc = true

        createLibJarFromSplitFileSrc = true

        exampleSrcFile = Paths.get("rsc-test/src-example/${baseFileName}Example.kt")

        destMainSrcFileName = "$baseFileName.kt"

        destTestSrcFileName = "${baseFileName}Test.kt"

        destExampleSrcFileName = "${baseFileName}Example.kt"

        ifFileExists {
            ifExistsReadeMeFile = QIfExistsWrite.BackupAndOverwriteAtomic
//                        ifExistsReadeMeFile = QIfExistsWrite.DoNothing
        }

        readme {
            default()
        }

        rootOfChain {
            mainTopLevel {
                fileName == "${lib.baseFileName}.kt"
            }
            testTopLevel {
                fileName == "${lib.baseFileName}Test.kt"
            }
        }

        splitSrcFileImportFilter filter@{
            if (fileName == "QDep.kt" && (importPath == "nyab.util.children" || importPath == "nyab.util.parent")) {
                return@filter false
            }

            default()
        }

        singleFileAnnotation {
            """@file:Suppress("UNCHECKED_CAST")"""
        }

        topLevelElementsSorter = Comparator<QTopLevelCompactElement> { a, b ->
            a.analysisCtx.isTest.compareTo(b.analysisCtx.isTest)
        }.thenComparator { a, b ->
            a.filePath.compareTo(b.filePath)
        }.thenComparator { a, b ->
            a.lineNumber.compareTo(b.lineNumber)
        }

        nonRootNodeVisitor {
            isMustDiscard {
                if (pkgFqName.contains("q_ignore")) {
                    return@isMustDiscard true
                }

                false
            }

            isMustMark {
                isEnumEntry() || isClassInitializer() || isOverridden() || isInOpenClass() || isInAbstractClass() || hasOpenKeyword() || isCompanionObject() || isInInterface()
            }
        }

        srcCode {
            visibilityChange {

                if (compactEle.isChainRoot()) {
                    QKtVisibilityChange.NoChange
                } else if (!srcSetType.isSingleFileSelfContained) {
                    QKtVisibilityChange.ToInternal
                } else {
                    QKtVisibilityChange.ToPrivate
                }
            }
        }
    }

    return result
}

