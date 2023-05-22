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
import kotlin.io.path.exists
import kotlinx.coroutines.delay
import nyab.build.qOpenEditor
import nyab.util.QFType
import nyab.util.QGit
import nyab.util.QGitInitIfExists
import nyab.util.log
import nyab.util.path
import nyab.util.qList

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QCompactLibResult(
        val lib: QCompactLib,
        val version: String,
        val oldHash: String,
        val newHash: String,
        val isVersionUpdated: Boolean,
        val mainContext: QAnalysisContext,
        val testContext: QAnalysisContext?,
        val singleMainSrcFile: Path?,
        val singleTestSrcFile: Path?,
        val readMeFile: Path,
        val buildGradleKtsFile: Path,
        val settingsGradleKtsFile: Path,
        val gitIgnoreFile: Path,
        val chainNodeCount: String,
        val buildSuccess: Boolean
) {
    // << Root of the CallChain >>
    private val git = QGit(lib.destProjDir)

    // << Root of the CallChain >>
    suspend fun openEditorAll() {
        singleMainSrcFile?.qOpenEditor()

        singleTestSrcFile?.qOpenEditor()

        readMeFile.qOpenEditor()

        buildGradleKtsFile.qOpenEditor()

        settingsGradleKtsFile.qOpenEditor()

        gitIgnoreFile.qOpenEditor()
    }

    // << Root of the CallChain >>
    suspend fun gitHubRelease(draft: Boolean = true, targetBranch: String = "main"): Boolean {
        return git.gh_release_create(
                tag = version,
                title = lib.libName + " " + version,
                draft = draft,
                targetBranch = targetBranch,
                files = lib.destProjDir.relativize("build/libs".path).qList(QFType.File) { it.fileName.startsWith("${lib.mavenArtifactId}-${version}") && it.fileName.endsWith(".jar") }
        ).success
    }

    // << Root of the CallChain >>
    suspend fun doGitTask() {
        if (!buildSuccess)
            return

        git.init(QGitInitIfExists.DoNothing)

        val isNewRepo = git.isNoCommitsYet()

        if (isNewRepo) {
            git.add("LICENSE".path)
            git.commit("First Commit.")
        }

        git.add()

        val committed = if (!git.isNothingToCommit()) {
            val msg = lib.commitMessage(QCommitMessageScope(lib, this, isNewRepo))
            git.commit(msg).success
        } else {
            false
        }

        if (committed) {
            if (!git.hasRemote()) {
                git.createGitHubRepo(userName = lib.gitHubUserName, repoName = lib.gitHubRepoName, description = lib.repoDescription, topics = lib.repoTopics)
                delay(1000)
                git.push_u_origin_main()
            } else {
                git.push()
            }

            val jar = lib.destArtifactsDir.resolve(lib.destJarFileName(version))
            val srcJar = lib.destArtifactsDir.resolve(lib.destSourcesJarFileName(version))
            val artifacts = listOf(jar, srcJar).filter { it.exists() }

            oldHash.log
            newHash.log

            isVersionUpdated.log

            artifacts.size.log

            if (isVersionUpdated && artifacts.isNotEmpty()) {
                git.gh_release_create(tag = version, title = "${lib.libName} $version", files = artifacts, draft = true)
            }

            git.openRepository()

            println(chainNodeCount)
        }
    }
}