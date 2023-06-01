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
import nyab.compact.QCompactLibRepositoryTask
import nyab.compact.QCompactLibResult
import nyab.compact.QKtVisibilityChange
import nyab.compact.QTopLevelCompactElement
import nyab.compact.QVersionUpdateStrategy
import nyab.compact.qCompactLib
import nyab.util.QIfExistsWrite
import java.nio.file.Paths

fun main(): Unit = runBlocking {
    // build includes
    // - analyse src dirs
    // - create compact src code
    //   - single-file version
    //   - split-file version
    // - create test src code ( if exists )
    //   - single-file version
    //   - split-file version
    // - create example src code
    // - create jars
    // - create build.gradle.kts
    // - create README.md, LICENSE, .gitignore, etc
    // - run test ( if exists )
    // - run example
    val result = build()

//    if( result.buildSuccess ) {
//        val repo = QCompactLibRepositoryTask(result.lib.destProjDir)
//
//        // - create GitHub repository
//        repo.createGitHubRepo(
//            result.lib.author,
//            result.lib.libName,
//            result.lib.readmeDescription,
//            listOf("kotlin-library", "hello-world", "compact-library")
//        )
//
//        // - add / commit / push
//        // - release jars
//        repo.release("[auto] Reflect the changes of main repository.")
//    }
}

private suspend fun build(): QCompactLibResult {
    val result = qCompactLib(
        libName = "hello-world-compact",
        author = "nyabkun",
    ) {
        baseFileName("CompactHelloWorld")
        exampleSrcFile = Paths.get("rsc-test/src-example/CompactHelloWorldExample.kt")

        versionUpdateStrategy = QVersionUpdateStrategy.UpdateOnlyIfSplitSrcHashChanged

        readmeDescription = """Kotlin library that can say Hello."""

        versionSuffix = "-alpha"

        references = ""

        srcDirs {
            main("rsc-test/src")
            test("rsc-test/src-test")
        }

        createSingleFileSrc = true

        createLibJarFromSplitFileSrc = true

        ifFileExists {
            ifExistsReadMeFile = QIfExistsWrite.BackupAndOverwriteAtomic
//                        ifExistsReadeMeFile = QIfExistsWrite.DoNothing
        }

        readme {
            default()
        }

        splitSrcFileImportFilter filter@{
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

