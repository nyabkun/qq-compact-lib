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

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nyab.build.qOpenBrowser
import nyab.conf.QE
import nyab.conf.QMyGit
import nyab.conf.QMyLicense
import nyab.conf.QMyPath
import nyab.match.QM
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=4] = QGitHubRepo <-[Ref]- QGit.gh_repo_list() <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
@Serializable
internal data class QGitHubRepo(val name: String, val url: String, val isPrivate: Boolean)

// CallChain[size=2] = QGit <-[Call]- QCompactLibResult.git[Root]
internal class QGit(val pwd: Path = Paths.get(".")) {
    // CallChain[size=5] = QGit.shell <-[Call]- QGit.String.runCmd() <-[Call]- QGit.config_get_remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    var shell = QShell.DEFAULT_PWSH
    // CallChain[size=5] = QGit.printCmd <-[Call]- QGit.String.runCmd() <-[Call]- QGit.config_get_remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    var printCmd = QOut.CONSOLE

    // CallChain[size=2] = QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun openRepository() {
        qOpenBrowser(config_get_remote_origin_url())
    }

    // CallChain[size=2] = QGit.hasRemote() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun hasRemote(): Boolean {
        val result = "git remote".runCmd()
        return result.success && result.output.isNotEmpty()
    }

    // CallChain[size=3] = QGit.remote_add() <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun remote_add(url: String, remoteName: String = "origin"): QRunResult {
        return "git remote add $remoteName $url".runCmd()
    }

    // CallChain[size=2] = QGit.getGitHubLatestRelease() <-[Call]- QCompactLib.getGitHubLatestReleaseVersion()[Root]
    suspend fun getGitHubLatestRelease(): String {
        val latestLine = "gh release list".runCmd().output.lines().find {
            it.contains("Latest")
        } ?: return ""

        val spl = latestLine.split("\t")

        return if (spl.size == 4) {
            spl[2]
        } else {
            ""
        }
    }

    // CallChain[size=2] = QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
    /**
     * https://cli.github.com/manual/gh_release_create
     */
    suspend fun gh_release_create(
        tag: String,
        title: String = tag,
        files: List<Path> = emptyList(),
        generateNotes: Boolean = false,
        prerelease: Boolean = false,
        draft: Boolean = false,
        targetBranch: String = "main",
    ): QRunResult {
        val cmd = mutableListOf("gh", "release", "create", tag)

        if (title.isNotEmpty()) {
            cmd += "--title"
            cmd += title
        }

        if (generateNotes) {
            cmd += "--generate-notes"
        }

        if (prerelease) {
            cmd += "--prerelease"
        }

        if (draft) {
            cmd += "--draft"
        }

        if (targetBranch.isNotEmpty()) {
            cmd += "--target"
            cmd += targetBranch
        }

        if (files.isNotEmpty()) {
            files.forEach {
                cmd += it.qRelativeFrom(pwd).normalize().pathString
            }
        }

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.config_get_remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun config_get_remote_origin_url(): String {
        return "git config --get remote.origin.url".runCmd().output.trim()
    }

    // CallChain[size=2] = QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun init(ifExists: QGitInitIfExists = QGitInitIfExists.DoNothing): QRunResult {
        val dotGit = pwd.resolve(".git")
        if (dotGit.exists() && dotGit.isDirectory()) {
            when (ifExists) {
                QGitInitIfExists.DoNothing -> {
                    return QRunResult.NotExecuted
                }

                QGitInitIfExists.Continue -> { // continue
                }
            }
        }

        val result = listOf("git", "init").runCmd()

        pwd.resolve(".gitignore").qWrite(QMyGit.git_ignore, QIfExistsWrite.DoNothing)

        pwd.resolve("LICENSE").qWrite(QMyGit.mit_license, QIfExistsWrite.DoNothing)

        pwd.resolve("README.md").qCreateFile(QIfExistsCreate.DoNothing)

        return result
    }

    // CallChain[size=2] = QGit.commit() <-[Call]- QCompactLibResult.doGitTask()[Root]
    /**
     * [Manual](https://git-scm.com/docs/git-commit)
     */
    suspend fun commit(message: String = "[WIP]"): QRunResult {
        val cmd = mutableListOf("git", "commit")

        if (message.isNotEmpty()) {
            cmd += "-m"
            cmd += message
        } else {
            cmd += "--allow-empty-message"
            cmd += "-m"
            cmd += "''"
        }

        return cmd.runCmd()
    }

    // CallChain[size=2] = QGit.add() <-[Call]- QCompactLibResult.doGitTask()[Root]
    /**
     * [Manual](https://git-scm.com/docs/git-add)
     */
    suspend fun add(vararg fileOrDir: Path = arrayOf(pwd)): QRunResult {
        val cmd = mutableListOf("git", "add")

        for (p in fileOrDir) {
            val target = pwd.relativize(p).normalize()

            val path = if (target.pathString.isEmpty() || target == pwd) {
                "."
            } else {
                target.pathString
            }

            cmd += path
        }

        return cmd.runCmd()
    }

    // CallChain[size=2] = QGit.push_u_origin_main() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun push_u_origin_main(): QRunResult {
        return "git push -u origin main".runCmd()
    }

    // CallChain[size=2] = QGit.push() <-[Call]- QCompactLibResult.doGitTask()[Root]
    /**
     * [Manual](https://git-scm.com/docs/git-push)
     */
    suspend fun push(
        remoteBranch: QRemoteBranch = QRemoteBranch.DEFAULT,
        force: QPushForce = QPushForce.None,
    ): QRunResult {
        val cmd = mutableListOf("git", "push")

        if (remoteBranch != QRemoteBranch.DEFAULT) {
            cmd += remoteBranch.remoteName
            cmd += remoteBranch.remoteBranchName
        }

        when (force) {
            QPushForce.ForceSafely -> {
                cmd += listOf("--force-with-lease", "--force-with-includes")
            }

            QPushForce.ForceAtAnyCost -> {
                cmd += "--force"
            }

            QPushForce.None -> {
            }
        }

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.gh_repo_edit_add_topics() <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun gh_repo_edit_add_topics(
        userName: String,
        repoName: String = pwd.name,
        vararg topics: String
    ): QRunResult {
        val cmd = mutableListOf("gh", "repo", "edit", "$userName/$repoName", "--add-topic", topics.joinToString(","))

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.gh_repo_create_source() <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun gh_repo_create_source(
        repoName: String = pwd.name,
        push: Boolean = false,
        description: String = "",
        visibility: QRepoVisibility = QRepoVisibility.Private,
        disableWiki: Boolean = false,
        disableIssues: Boolean = false,
        homepageUrl: String = "",
        includeAllBranchesFromTemplateRepo: Boolean = false,
        remoteName: String = "",
        teamName: String = "",
    ): QRunResult {
        val cmd = mutableListOf("gh", "repo", "create", repoName)

        val localPath = pwd

        if (push) {
            cmd += "--push"
        }

        if (disableIssues) {
            cmd += "--disable-issues"
        }

        if (disableWiki) {
            cmd += "--disable-wiki"
        }

        cmd += "--source"
        cmd += localPath.absolutePathString()

        cmd += "--" + visibility.text

        if (includeAllBranchesFromTemplateRepo) {
            cmd += "--include-all-branches"
        }

        if (remoteName.isNotEmpty()) {
            cmd += "--remote"
            cmd += remoteName
        }

        if (teamName.isNotEmpty()) {
            cmd += "--team"
            cmd += teamName
        }

        if (description.isNotEmpty()) {
            cmd += "--description"
            cmd += description
        }

        if (homepageUrl.isNotEmpty()) {
            cmd += "--homepage"
            cmd += homepageUrl
        }

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.gh_repo_list() <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun gh_repo_list(): List<QGitHubRepo> {
        val json = "gh repo list --json name,url,isPrivate".runCmd().output
        return Json.decodeFromString(json)
    }

    // CallChain[size=3] = QGit.List<String>.runCmd() <-[Call]- QGit.gh_release_create() <-[Call]- QCompactLibResult.doGitTask()[Root]
    private suspend fun List<String>.runCmd(quiet: Boolean = false): QRunResult {
        return this.qRunInShell(pwd = pwd, shell = shell, quiet = quiet, printCmd = printCmd)
    }

    // CallChain[size=4] = QGit.String.runCmd() <-[Call]- QGit.config_get_remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibResult.doGitTask()[Root]
    private suspend fun String.runCmd(quiet: Boolean = false): QRunResult {
        return this.qRunInShell(pwd = pwd, shell = shell, quiet = quiet, printCmd = printCmd)
    }

    // CallChain[size=2] = QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun createGitHubRepo(
        userName: String,
        repoName: String = pwd.name,
        description: String = "",
        topics: List<String> = emptyList()
    ) {
        val result = gh_repo_create_source(
            repoName = repoName,
            description = description,
            visibility = QRepoVisibility.Private
        )

        val alreadyExists = result.error.contains("Name already exists on this account")

        if (!alreadyExists && topics.isNotEmpty()) {
            gh_repo_edit_add_topics(userName = userName, repoName = repoName, *topics.toTypedArray())
        }

        val url = gh_repo_list().find {
            it.name == repoName
        }!!.url

        if (!alreadyExists && url.isNotEmpty()) {
            remote_add(url = url)

            // fatal: the requested upstream branch 'origin/main' does not exist
            //  branch_set_upstream_to(localBranch = "main", remoteName = "origin", remoteBranch = "main")
        }
    }

    // CallChain[size=2] = QGit.isNoCommitsYet() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun isNoCommitsYet(): Boolean {
        val err = "git log -n 1".runCmd().error
        return err.lineSequence().any {
            it.endsWith("does not have any commits yet")
        }
    }

    // CallChain[size=2] = QGit.isNothingToCommit() <-[Call]- QCompactLibResult.doGitTask()[Root]
    suspend fun isNothingToCommit(): Boolean {
        return "git status".runCmd().output.trim().endsWith("nothing to commit, working tree clean")
    }
}

// CallChain[size=3] = QRepoVisibility <-[Ref]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal enum class QRepoVisibility {
    // CallChain[size=4] = QRepoVisibility.Public <-[Propag]- QRepoVisibility.Private <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    Public,
    // CallChain[size=3] = QRepoVisibility.Private <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    Private,
    // CallChain[size=4] = QRepoVisibility.Internal <-[Propag]- QRepoVisibility.Private <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    Internal;

    // CallChain[size=4] = QRepoVisibility.text <-[Call]- QGit.gh_repo_create_source() <-[Call]- QGit.createGitHubRepo() <-[Call]- QCompactLibResult.doGitTask()[Root]
    val text: String
        get() = name.toLowerCaseAsciiOnly()
}

// CallChain[size=2] = QGitInitIfExists <-[Ref]- QCompactLibResult.doGitTask()[Root]
internal enum class QGitInitIfExists {
    // CallChain[size=2] = QGitInitIfExists.DoNothing <-[Call]- QCompactLibResult.doGitTask()[Root]
    DoNothing,
    // CallChain[size=3] = QGitInitIfExists.Continue <-[Propag]- QGitInitIfExists.DoNothing <-[Call]- QCompactLibResult.doGitTask()[Root]
    Continue
}

// CallChain[size=3] = QPushForce <-[Ref]- QGit.push() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal enum class QPushForce {
    // CallChain[size=3] = QPushForce.None <-[Call]- QGit.push() <-[Call]- QCompactLibResult.doGitTask()[Root]
    None,
    // CallChain[size=3] = QPushForce.ForceSafely <-[Call]- QGit.push() <-[Call]- QCompactLibResult.doGitTask()[Root]
    ForceSafely,
    // CallChain[size=3] = QPushForce.ForceAtAnyCost <-[Call]- QGit.push() <-[Call]- QCompactLibResult.doGitTask()[Root]
    ForceAtAnyCost;
}

// CallChain[size=3] = QRemoteBranch <-[Ref]- QGit.push() <-[Call]- QCompactLibResult.doGitTask()[Root]
internal data class QRemoteBranch(val remoteName: String, val remoteBranchName: String) {
    companion object {
        // CallChain[size=3] = QRemoteBranch.DEFAULT <-[Call]- QGit.push() <-[Call]- QCompactLibResult.doGitTask()[Root]
        val DEFAULT = QRemoteBranch("", "")
        
    }
}