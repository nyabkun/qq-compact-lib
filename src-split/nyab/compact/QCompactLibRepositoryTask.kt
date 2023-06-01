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
import kotlin.io.path.name
import kotlinx.coroutines.delay
import nyab.util.QGit
import nyab.util.QGitInitIfExists
import nyab.util.log
import nyab.util.logYellow
import nyab.util.path
import nyab.util.pathRelative
import nyab.util.qConvertContent
import nyab.util.qLineAt

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QCompactLibRepositoryTask(val projDir: Path, val libName: String = projDir.name) {
    // << Root of the CallChain >>
    private val git = QGit(projDir)

    // << Root of the CallChain >>
    val versionFile: Path = projDir.resolve("VERSION")

    // << Root of the CallChain >>
    val nextVersion: String by lazy {
        versionFile.qLineAt(1).trim()
    }

    // << Root of the CallChain >>
    val nextVersionHash: String by lazy {
        versionFile.qLineAt(2).trim()
    }

    // << Root of the CallChain >>
    val readmeFile: Path = projDir.resolve("README.md")

    companion object {
        // << Root of the CallChain >>
        const val DEFAULT_COMMIT_MESSAGE = "[auto] Reflect the changes of main repository."
        // << Root of the CallChain >>
        const val DEFAULT_COMMIT_MESSAGE_FIRST_BUILD = "[auto] First Build."
    }

    // << Root of the CallChain >>
    fun updateReadmeVersion() {
        val curVersion = readmeFile.qLineAt(1).replaceFirst(Regex("<!--- version = (.*?) --->"), "$1").trim()

        readmeFile.qConvertContent { old ->
            old.replace(curVersion, nextVersion)
        }
    }

    // << Root of the CallChain >>
    suspend fun createGitHubRepo(
        userName: String,
        repoName: String,
        description: String,
        topics: List<String>,
        firstCommitMessage: String = DEFAULT_COMMIT_MESSAGE_FIRST_BUILD
    ) {
        git.init(QGitInitIfExists.DoNothing)

        val isNewRepo = git.isNoCommitsYet()

        if (isNewRepo) {
            git.add("LICENSE".path)
            git.commit(firstCommitMessage)
        }

        git.createGitHubRepo(
            userName = userName,
            repoName = repoName,
            description = description,
            topics = topics
        )

        delay(1000)

        git.push_u_origin_main()
    }

    // << Root of the CallChain >>
    suspend fun addAndCommit(message: String) {
        git.add_commit(message)
    }

    // << Root of the CallChain >>
    suspend fun push() {
        git.push()
    }

    // << Root of the CallChain >>
    suspend fun release(
        commitMessage: String = DEFAULT_COMMIT_MESSAGE
    ) {
        val artifacts = versionFile.qLineAt(3).split(";").map { projDir.resolve(it.trim().pathRelative).log }.filter { it.exists() }

        val latestVersion = git.getGitHubLatestRelease()

        if( latestVersion == nextVersion ) {
            "It is already the latest version.".logYellow
        } else {
            git.add_A()

            if (!git.isNothingToCommit()) {
                git.commit(commitMessage)
            }

            git.push()

            if (artifacts.isNotEmpty()) {
                git.gh_release_create(tag = nextVersion, title = "$libName $nextVersion", files = artifacts, draft = true)
            } else {
                "artifacts is empty".logYellow
            }

            git.openJitPackPage()

            git.openRepository()

            updateReadmeVersion()
        }
    }
}