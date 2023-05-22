/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.conf

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import nyab.match.QM
import nyab.util.QFType
import nyab.util.QFirstOrLast
import nyab.util.path
import nyab.util.qListByMatch
import nyab.util.qPathSearch
import nyab.util.qaNotNull
import nyab.util.re
import nyab.util.sep

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=7] = QMyPath <-[Ref]- qLogStackFrames() <-[Call]- QException.mySrcAndStack <-[Call ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal object QMyPath {
    // -- dirs

    // CallChain[size=4] = QMyPath.root <-[Call]- Path.qRelativeFrom() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val root = "".path
    // CallChain[size=8] = QMyPath.src <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]-  ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val src = "src".path
    // CallChain[size=8] = QMyPath.src_java <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Ca ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val src_java = "src-java".path
    // CallChain[size=8] = QMyPath.src_build <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[C ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val src_build = "src-build".path
    // CallChain[size=8] = QMyPath.src_experiment <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val src_experiment = "src-experiment".path
    // CallChain[size=8] = QMyPath.src_plugin <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[ ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val src_plugin = "src-plugin".path
    // CallChain[size=8] = QMyPath.src_config <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[ ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val src_config = "src-config".path
    // CallChain[size=8] = QMyPath.src_test <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Ca ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val src_test = "src-test".path
    // CallChain[size=8] = QMyPath.rsc <-[Call]- QMyPath.base <-[Call]- Path.qBaseDir() <-[Call]- Path.q ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val rsc = "rsc".path
    // CallChain[size=6] = QMyPath.temp <-[Call]- Path.qWithBaseDir() <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val temp = ".q_temp".path
    // CallChain[size=8] = QMyPath.desktop <-[Call]- QMyPath.base <-[Call]- Path.qBaseDir() <-[Call]- Pa ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val desktop = (System.getProperty("user.home") + "/Desktop").path
    // CallChain[size=6] = QMyPath.user <-[Call]- QMyPath.backup <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val user = "user".path
    // CallChain[size=8] = QMyPath.app <-[Call]- QMyPath.base <-[Call]- Path.qBaseDir() <-[Call]- Path.q ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val app = "app".path
    // CallChain[size=2] = QMyPath.compact <-[Call]- QCompactLibScope.destProjDir[Root]
    val compact = "compact".path
    // CallChain[size=5] = QMyPath.backup <-[Call]- Path.qTryBackup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val backup = user sep ".q_backup"

    // --- dir list

    // CallChain[size=7] = QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAndSta ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val src_root: List<Path> by lazy {
        val base = listOf(
            src,
            src_test,
            src_experiment,
            src_config,
            src_plugin,
            src_java,
            src_build,
            "src".path,
            "test".path,
            "src/main/kotlin".path,
            "src/test/kotlin".path,
            "src/main/java".path,
            "src/test/java".path,
//            ".".path,
        ).filter { it.exists() }

        val search = Paths.get(".").qListByMatch(type = QFType.Dir, nameMatch = QM.startsWith("src-"), maxDepth = 1)

        (base + search).distinct()
    }

    // CallChain[size=7] = QMyPath.base <-[Call]- Path.qBaseDir() <-[Call]- Path.qWithBaseDir() <-[Call] ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val base: List<Path> by lazy {
        val list: MutableList<Path> = mutableListOf()
        list.add(root)
        list += src_root
        list += listOf(desktop, rsc, user, app)
        list.distinct()
    }

    // -- files

    
}

// CallChain[size=2] = qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
internal val qJAVA_HOME: Path by lazy {
    qPathSearch(QFType.Dir) {
        searchEnv("JAVA_HOME")

        searchInDir("C:/Scoop/apps/".path) {
            nameMatcher = QM.matches("""(?i)openjdk.*""".re)
            sortAndFind = QFirstOrLast.LAST
        }

        searchInDir("C:/Program Files/Java/".path) {
            nameMatcher = QM.matches("""(?i).*jdk.*""".re)
            sortAndFind = QFirstOrLast.LAST
        }

        searchInDir("C:/Program Files/AdoptOpenJDK".path) {
            nameMatcher = QM.matches("""(?i).*jdk.*""".re)
            sortAndFind = QFirstOrLast.LAST
        }
    }.qaNotNull(QMyException.DirectoryNotFound)
}