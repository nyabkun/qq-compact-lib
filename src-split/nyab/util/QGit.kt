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
import nyab.build.qOpenExplorer
import nyab.conf.QE
import nyab.conf.QMyBranch
import nyab.conf.QMyGit
import nyab.conf.QMyLicense
import nyab.conf.QMyPath
import nyab.ex.util.qDownloadSrc
import nyab.match.QM
import nyab.util.backup.qTryBackup
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=6] = TAB <-[Call]- QGitCommit.init { <-[Propag]- QGitCommit.hash <-[Call]- QGit.restoreLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
private const val TAB = "\t"

// CallChain[size=4] = QGitBranch <-[Ref]- QGit.branches() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal class QGitBranch(val local: String, val upstream: String) {
    // CallChain[size=5] = QGitBranch.toString() <-[Propag]- QGitBranch <-[Ref]- QGit.branches() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    override fun toString(): String {
        return qBrackets("Local", local, "Upstream", upstream)
    }
}

// CallChain[size=4] = QGitHubRelease <-[Ref]- QGit.gh_release_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal data class QGitHubRelease(val title: String, val tag: String, val isLatest: Boolean, val date: String) {

    companion object {
        // CallChain[size=4] = QGitHubRelease.parseLine() <-[Call]- QGit.gh_release_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
        fun parseLine(line: String): QGitHubRelease? {
            val spl = line.split("\t")
            return if (spl.size == 4) {
                QGitHubRelease(spl[0].trim(), spl[1].trim(), spl[2].trim() == "Latest", spl[3])
            } else {
                null
            }
        }
    }
}

// CallChain[size=4] = QGitHubRepo <-[Ref]- QGit.gh_repo_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
@Serializable
internal data class QGitHubRepo(val name: String, val url: String, val isPrivate: Boolean)

// CallChain[size=6] = QGit <-[Ref]- QGitObj.QGitObj() <-[Call]- QGitObj.whatChanged() <-[Call]- QGi ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal open class QGit(val pwd: Path = Paths.get(".")) {
    // CallChain[size=3] = QGit.shell <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    var shell = QShell.DEFAULT_PWSH
    // CallChain[size=3] = QGit.printCmd <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    var printCmd = QOut.CONSOLE

    // CallChain[size=3] = QGit.isRepoExists() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun isRepoExists(url: String): Boolean {
        val src = qDownloadSrc(url = url)
        return src.trim() == "Not Found"
    }

    // CallChain[size=2] = QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun openRepository() {
        qOpenBrowser(config_get_remote_origin_url())
    }

    // CallChain[size=2] = QGit.openJitPackPage() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun openJitPackPage() {
        val url = config_get_remote_origin_url()

        val user = url.replaceFirst(Regex("https://github.com/(.*?)/.*\\.git"), "$1")
        val repoName = url.replaceFirst(Regex("https://github.com/.*?/(.*)\\.git"), "$1")

        qOpenBrowser("https://jitpack.io/#${user}/${repoName}")
    }

    // CallChain[size=3] = QGit.hasRemote() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun hasRemote(): Boolean {
        val result = "git remote".runCmd()
        return result.success && result.output.isNotEmpty()
    }

    // CallChain[size=3] = QGit.remote_add() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun remote_add(url: String, remoteName: String = "origin"): QRunResult {
        return "git remote add $remoteName $url".runCmd()
    }

    // CallChain[size=2] = QGit.getGitHubLatestRelease() <-[Call]- QCompactLibRepositoryTask.release()[Root]
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

    // CallChain[size=3] = QGit.gh_repo_edit_visibility() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gh_repo_edit_visibility(visibility: QRepoVisibility): QRunResult {
        return "gh repo edit --visibility ${visibility.text}".runCmd()
    }

    // CallChain[size=3] = QGit.gh_release_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gh_release_list(): List<QGitHubRelease> {
        val result = "gh release list".runCmd()
        return result.output.lineSequence().mapNotNull { line ->
            QGitHubRelease.parseLine(line)
        }.toList()
    }

    // CallChain[size=2] = QGit.gh_release_create() <-[Call]- QCompactLibRepositoryTask.release()[Root]
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

    // CallChain[size=3] = QGit.remote_v() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun remote_v(): QRunResult {

        return "git remote -v".runCmd()
    }

    // CallChain[size=3] = QGit.branch_vv() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun branch_vv(): QRunResult {
        return "git branch -vv".runCmd()
    }

    // CallChain[size=3] = QGit.config_get_remote_origin_url() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun config_get_remote_origin_url(): String {
        return "git config --get remote.origin.url".runCmd().output.trim()
    }

    // CallChain[size=3] = QGit.restore_staged() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    /**
     * https://stackoverflow.com/a/58830990
     */
    suspend fun restore_staged(file: Path): QRunResult {
        return "git restore --staged ${file.pathString.qQuoteArg(shell = QShell.DEFAULT_PWSH)}".runCmd()
    }

    // CallChain[size=3] = QGit.restore_staged() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun restore_staged(filePath: String): QRunResult {
        return "git restore --staged ${filePath.qQuoteArg(shell = QShell.DEFAULT_PWSH)}".runCmd()
    }

    // CallChain[size=3] = QGit.switch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun switch(branch: QGitBranch = QMyBranch.main): QRunResult {
        return "git switch ${branch.local}".runCmd() // PWSH
    }

    // CallChain[size=3] = QGit.renameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun renameBranch(
        oldBranch: QGitBranch,
        newBranchName: String,
        rewriteQMyBranchSrcCodeAutomatically: Boolean = true,
    ): QRunResult {
        val result = "git branch -m ${oldBranch.local} $newBranchName".runCmd() // PWSH

        if (rewriteQMyBranchSrcCodeAutomatically)
            qRunMethodInNewJVMProcess(
                QMyBranch::class.qContainingFileMainMethod(),
                shell = QShell.DEFAULT // currently PowerShell does not work.
            )

        return result
    }

    // CallChain[size=3] = QGit.git_branch_M_main() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun git_branch_M_main(): QRunResult {
        return "git branch -M main".runCmd()
    }

    // CallChain[size=3] = QGit.branches() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun branches(): List<QGitBranch> {
        val output =
            "git branch --no-color --format '%(refname:short)$TAB%(upstream:short)'".runCmd(quiet = true).output

        return output.lines().filter {
            if (it.isBlank())
                return@filter false
            val spl = it.split(TAB)
            (spl.size == 1 || spl.size == 2)
        }.map {
            val spl = it.split(TAB)
            if (spl.size == 1) {
                QGitBranch(spl[0].trim(), "")
            } else {
                QGitBranch(spl[0].trim(), spl[1].trim())
            }
        }
    }

    // CallChain[size=3] = QGit.whatchanged() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun whatchanged(hash: String): List<QGitCommit> {
        val output = "git whatchanged ${qLogPrettyOpt(shell)} --all --find-object=$hash".runCmd(quiet = true).output
        return QGitCommit.parsePrettyOutput(output)
    }

    // CallChain[size=3] = QGit.logLatestCommits() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    // -<number>, -n <number>, --max-count=<number>
    // Limit the number of commits to output.
    suspend fun logLatestCommits(size: Int = 10, skip: Int = 0): List<QGitCommit> {
        val output = "git log ${qLogPrettyOpt(shell)} -$size --skip=$skip".runCmd(quiet = true).output

        return QGitCommit.parsePrettyOutput(output)
    }

    // CallChain[size=3] = QGit.log() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun log(): List<QGitCommit> {
        val output =
            "git log ${qLogPrettyOpt(shell)} --all".runCmd(quiet = true).output
        return QGitCommit.parsePrettyOutput(output)
    }

    // CallChain[size=3] = QGit.findLargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun findLargeFiles(limit: Int = 50): List<QGitObj> {
        val revListOutput =
            """git rev-list --objects --all | git cat-file --batch-check=${shell.quote}%(objecttype)$TAB%(objectname)$TAB%(objectsize)$TAB%(objectsize:disk)$TAB%(rest)${shell.quote}""".runCmd().output

        revListOutput.log

        val allObjects =
            revListOutput.lines().filter { it.isNotBlank() && it.split(TAB).size == 5 }.map { QGitObj(it, this) }

        val objSizeRanking = allObjects.filter {
            it.path.isNotEmpty()
        }.sortedBy {
            -it.size
        }.take(limit)

        val rankingStr = objSizeRanking.joinToString("\n").qPrettyAlignBrackets()

        rankingStr.log

        objSizeRanking.first().whatChanged()

        return objSizeRanking
    }

    // CallChain[size=3] = QGit.gitHub_clone_sparse_checkout() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gitHub_clone_sparse_checkout(
        githubUrl: String,
        destDir: Path = pwd,
    ): Path {
        val result = """(.*?)/(tree|blob)/(.+?)/(.*?)$""".re.find(githubUrl).qaNotNull()

        val repoUrl = result.groups[1]!!.value + ".git"
        val branch = result.groups[3]!!.value
        val dir = result.groups[4]!!.value

        return clone_sparse_checkout(repoUrl, listOf(dir), destDir, branch)
    }

    // CallChain[size=3] = QGit.clone() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun clone(repoUrl: String): QRunResult {
        if (pwd.exists())
            QE.ShouldBeEmptyDir.throwItDir(pwd)

        pwd.qCreateDir()

        return """git clone $repoUrl ${pwd.pathString.qQuoteArg()}""".runCmd()
    }

    // CallChain[size=3] = QGit.clone_sparse_checkout() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    // https://stackoverflow.com/a/63786181/5570400
    suspend fun clone_sparse_checkout(
        repoUrl: String,
        targetDirs: List<String>,
        destDir: Path = pwd,
        branch: String = "main",
    ): Path {
        destDir.qCreateDir(QIfExistsCreateDir.DoNothing)

        if (destDir.qIsNotEmpty())
            QE.ShouldBeEmptyDir.throwItDir(destDir)

        // TODO craete dsl

        // --sparse
        //  Employ a sparse-checkout, with only files in the toplevel directory initially being present.
        //  The git-sparse-checkout[1] command can be used to grow the working directory as needed.
        //
        // --filter
        //  Use the partial clone feature and request that the server sends a subset of reachable objects according to a given object filter.
        //  --filter=blob:none will filter out all blobs (file contents) until needed by Git.
        //
        // --no-checkout
        //  No checkout of HEAD is performed after the clone is complete.
        "git clone --depth=1 --filter=blob:none --no-checkout --sparse $repoUrl ${destDir.qQuoteArg()}".qRunInShell(
            destDir,
            printCmd = printCmd
        )
//    "git config core.sparsecheckout true".qCmd(this)
//    (this sep ".git/info/sparse-checkout").qWrite(targetDirs, QIfExistsWrite.OverwriteDirect)
//    "git remote add origin $repoUrl".qCmd(this)

        val dirs = targetDirs.joinToString(" ") { it.qQuoteArg() }

        "git sparse-checkout add $dirs".qRunInShell(pwd = destDir)
        "git switch $branch".qRunInShell(pwd = destDir)

        return destDir sep targetDirs.first()
    }

    // CallChain[size=3] = QGit.branch_set_upstream_to() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    /**
     * [Manual](https://git-scm.com/docs/git-branch)
     */
    suspend fun branch_set_upstream_to(
        localBranch: String = "main",
        remoteName: String = "origin",
        remoteBranch: String = localBranch,
    ): QRunResult {
        return "git branch $localBranch --set-upstream-to $remoteName/$remoteBranch".runCmd()
    }

    // CallChain[size=3] = QGit.init() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
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

    // CallChain[size=2] = QGit.commit() <-[Call]- QCompactLibRepositoryTask.release()[Root]
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

    // CallChain[size=3] = QGit.add() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
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

    // CallChain[size=2] = QGit.add_A() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun add_A(): QRunResult {
        return "git add -A".runCmd()
    }

    // CallChain[size=3] = QGit.push_u_origin_main() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun push_u_origin_main(): QRunResult {
        return "git push -u origin main".runCmd()
    }

    // CallChain[size=2] = QGit.push() <-[Call]- QCompactLibRepositoryTask.release()[Root]
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

    // CallChain[size=3] = QGit.add_commit_push() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun add_commit_push(message: String = "[WIP]") {
        add(pwd)
        commit(message = message)
        push()
    }

    // CallChain[size=3] = QGit.revert_HEAD() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun revert_HEAD() {
        "git revert HEAD".runCmd()
    }

    // CallChain[size=3] = QGit.add_commit() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun add_commit(message: String = "[WIP]") {
        add_A()
        commit(message = message)
    }

    // CallChain[size=3] = QGit.gh_gist_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gh_gist_list(limit: Int = -1, visibility: QGistListVisibility = QGistListVisibility.Any): QRunResult {
        val cmd = mutableListOf("gh", "gist", "list")

        if (limit > 0) {
            cmd += "--limit"
            cmd += limit.toString()
        }

        when (visibility) {
            QGistListVisibility.Public -> {
                cmd += "--public"
            }

            QGistListVisibility.Secret -> {
                cmd += "--secret"
            }

            QGistListVisibility.Any -> {
            }
        }

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.gh_gist_create() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    /**
     * [Official Manual](https://cli.github.com/manual/gh_gist_create)
     */
    private suspend fun gh_gist_create(
        files: List<Path>,
        description: String,
        openWebBrowser: Boolean = false,
        visibility: QGistVisibility = QGistVisibility.Secret,
    ): QRunResult {
        val cmd = mutableListOf("gh", "gist", "create")

        files.qaNotNull()

        for (p in files) {
            cmd += p.absolutePathString()
        }

        if (description.isNotEmpty()) {
            cmd += "--desc"
            cmd += description
        }

        if (openWebBrowser) {
            cmd += "--web"
        }

        if (visibility == QGistVisibility.Public) {
            cmd += "--public"
        }

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.gh_repo_edit_add_topics() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gh_repo_edit_add_topics(
        userName: String,
        repoName: String = pwd.name,
        vararg topics: String
    ): QRunResult {
        val cmd = mutableListOf("gh", "repo", "edit", "$userName/$repoName", "--add-topic", topics.joinToString(","))

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.gh_repo_create_source() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
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

    // CallChain[size=3] = QGit.gh_repo_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gh_repo_list(): List<QGitHubRepo> {
        val json = "gh repo list --json name,url,isPrivate".runCmd().output
        return Json.decodeFromString(json)
    }

    // CallChain[size=3] = QGit.List<String>.runCmd() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    private suspend fun List<String>.runCmd(quiet: Boolean = false): QRunResult {
        return this.qRunInShell(pwd = pwd, shell = shell, quiet = quiet, printCmd = printCmd)
    }

    // CallChain[size=3] = QGit.String.runCmd() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    private suspend fun String.runCmd(quiet: Boolean = false): QRunResult {
        return this.qRunInShell(pwd = pwd, shell = shell, quiet = quiet, printCmd = printCmd)
    }

    // CallChain[size=3] = QGit.gh_repo_create() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    /**
     * [Official Manual](https://cli.github.com/manual/gh_repo_create)
     */
    suspend fun gh_repo_create(
        repoName: String = pwd.name,
        push: Boolean = false,
        cloneToLocal: Boolean = false,
        description: String = "",
        license: QMyLicense = QMyLicense.MIT,
        visibility: QRepoVisibility = QRepoVisibility.Private,
        disableWiki: Boolean = false,
        disableIssues: Boolean = false,
        homepageUrl: String = "",
        templateRepoUrl: String = "",
        includeAllBranchesFromTemplateRepo: Boolean = false,
        remoteName: String = "",
        teamName: String = "",
        // https://github.com/github/gitignore
        gitIgnoreTemplate: String = "Kotlin",
    ): QRunResult {
        val cmd = mutableListOf("gh", "repo", "create", repoName)

        val localPath = pwd

        if (push) {
            cmd += "--push"
        }

        // the `--source` option is not supported with `--clone`, `--template`, `--license`, or `--gitignore`
        if (cloneToLocal) {
            cmd += "--clone"
        }

        if (disableIssues) {
            cmd += "--disable-issues"
        }

        if (disableWiki) {
            cmd += "--disable-wiki"
        }

        cmd += "--source"
        cmd += localPath.absolutePathString()

        cmd += when (visibility) {
            QRepoVisibility.Private ->
                "--private"

            QRepoVisibility.Internal ->
                "--internal"

            QRepoVisibility.Public ->
                "--public"
        }

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

        // the `--source` option is not supported with `--clone`, `--template`, `--license`, or `--gitignore`
        if (templateRepoUrl.isNotEmpty()) {
            cmd += "--template"
            cmd += templateRepoUrl
        }

        // the `--source` option is not supported with `--clone`, `--template`, `--license`, or `--gitignore`
        if (gitIgnoreTemplate.isNotEmpty()) {
            cmd += "--gitignore"
            cmd += gitIgnoreTemplate
        }

        // the `--source` option is not supported with `--clone`, `--template`, `--license`, or `--gitignore`
        if (license.ghKey.isNotEmpty()) {
            cmd += "--license"
            cmd += license.ghKey
        }

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.gitHubDownloadSingleDirToZip() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gitHubDownloadSingleDirToZip(
        githubRepoDirectoryUrl: String,
        destZip: Path,
        openExplorer: Boolean = true,
        ifExistsDestZip: QIfExistsCreate = QIfExistsCreate.DeleteAndCreateFile,
    ): Path {
        val tmp = QMyPath.temp sep destZip.nameWithoutExtension

        tmp.qForceDelete()

        val zip = gitHub_clone_sparse_checkout(
            githubRepoDirectoryUrl,
            tmp,
        ).qCreateZip(destZip, ifExistsCreate = ifExistsDestZip)

        tmp.qForceDelete()

        if (openExplorer)
            zip.qOpenExplorer()

        return zip
    }

    // CallChain[size=3] = QGit.gitHubDownloadAndUpdateMyself() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gitHubDownloadAndUpdateMyself(
        githubRepoDirOrFileUrl: String,
        destFile: Path = qThisFilePath, // << callerFile
        createBackupFile: Boolean = true
    ): Path {
        destFile.qaFile()

        if (createBackupFile) {
            if (destFile.exists()) {
                destFile.qTryBackup()
            }
        }

        return gitHubDownloadSingleDirOrFile(
            githubRepoDirOrFileUrl,
            destFile,
            openExplorer = false,
            ifExistsDestDir = QIfExistsCopyDir.Overwrite,
            ifExistsDestFile = QIfExistsCopyFile.Overwrite,
        )
    }

    // CallChain[size=3] = QGit.gitHubDownloadSingleDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun gitHubDownloadSingleDirOrFile(
        githubRepoDirOrFileUrl: String,
        destDirOrFile: Path = pwd,
        openExplorer: Boolean = true,
        tempCloneDir: Path = QMyPath.temp,
        ifExistsDestFile: QIfExistsCopyFile = QIfExistsCopyFile.Overwrite,
        ifExistsDestDir: QIfExistsCopyDir = QIfExistsCopyDir.Overwrite,
    ): Path {
        val tmp = tempCloneDir sep destDirOrFile.fileName.toString() + "-clone-from-github"
        tmp.qForceDelete()

        val cloneDirOrFile = gitHub_clone_sparse_checkout(
            githubRepoDirOrFileUrl,
            tmp,
        )

        cloneDirOrFile.qDirOrFileSizeStr().log

        val dest = if (cloneDirOrFile.isDirectory()) {
            cloneDirOrFile.qMoveDirTo(
                destDirOrFile,
                ifExistsContentFile = QIfExistsCopyFile.Overwrite,
                ifExistsDestDir = ifExistsDestDir
            )
        } else {
            cloneDirOrFile.qMoveFileTo(
                destDirOrFile,
                ifExistsDestFile = ifExistsDestFile
            )
        }

        tmp.qForceDelete()

        if (openExplorer)
            dest.qOpenExplorer()

        return dest
    }

    // CallChain[size=3] = QGit.gh_repo_delete() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    /**
     * [Official Manual](https://cli.github.com/manual/gh_repo_delete)
     */
    suspend fun gh_repo_delete(
        repoName: String = pwd.name,
        confirmDelete: Boolean = false,
    ): QRunResult {
        val cmd = mutableListOf("gh", "repo", "delete", repoName)

        if (confirmDelete) {
            cmd += "--yes"
        }

        return cmd.runCmd()
    }

    // CallChain[size=3] = QGit.searchAndRestoreDeletedFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun searchAndRestoreDeletedFile(matcher: QM): QRunResult {
        val filePath = searchDeletedFile(matcher)

        return if (filePath.size == 1) {
            restoreLostFile(filePath.first())
        } else {
            QRunResult.NotExecuted
        }
    }

    // CallChain[size=3] = QGit.searchDeletedFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun searchDeletedFile(matcher: QM): List<String> {
        val output = "git log --all --diff-filter=D --summary".runCmd(quiet = true).output

        return output.lines().asSequence().map {
            it.trim()
        }.filter {
            it.startsWith("delete mode ")
        }.map {
            it.substring("delete mode 100644".length).trim()
        }.filter { filePath ->
            matcher.matches(filePath)
        }.toList()
    }

    // CallChain[size=3] = QGit.restoreLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    // https://stackoverflow.com/a/7203551
    suspend fun restoreLostFile(filePath: String): QRunResult {
        val quotedFilePath = filePath.qQuoteArg(shell)

        val output =
            "git log ${qLogPrettyOpt(shell)} --all --full-history -- $quotedFilePath".runCmd().output

        val commits = QGitCommit.parsePrettyOutput(output)
        return "git checkout ${commits.first().hash}^ -- $quotedFilePath".runCmd()
    }

    // CallChain[size=3] = QGit.createGitHubRepo() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
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

    // CallChain[size=3] = QGit.isNoCommitsYet() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun isNoCommitsYet(): Boolean {
        val err = "git log -n 1".runCmd().error
        return err.lineSequence().any {
            it.endsWith("does not have any commits yet")
        }
    }

    // CallChain[size=2] = QGit.isNothingToCommit() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun isNothingToCommit(): Boolean {
        return "git status".runCmd().output.trim().endsWith("nothing to commit, working tree clean")
    }
}

// CallChain[size=7] = QGitObjType <-[Ref]- QGitObj.type <-[Call]- QGitObj.toString() <-[Propag]- QG ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal enum class QGitObjType {
    // CallChain[size=8] = QGitObjType.Blob <-[Propag]- QGitObjType <-[Ref]- QGitObj.type <-[Call]- QGit ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Blob,
    // CallChain[size=8] = QGitObjType.Tree <-[Propag]- QGitObjType <-[Ref]- QGitObj.type <-[Call]- QGit ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Tree,
    // CallChain[size=8] = QGitObjType.Tag <-[Propag]- QGitObjType <-[Ref]- QGitObj.type <-[Call]- QGitO ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Tag,
    // CallChain[size=8] = QGitObjType.Commit <-[Propag]- QGitObjType <-[Ref]- QGitObj.type <-[Call]- QG ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Commit;

    companion object {
        // CallChain[size=6] = QGitObjType.fromText() <-[Call]- QGitObj.init { <-[Propag]- QGitObj.whatChang ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
        fun fromText(text: String): QGitObjType {
            return when (text) {
                "blob" -> Blob
                "tree" -> Tree
                "tag" -> Tag
                "commit" -> Commit
                else -> qUnreachable(text)
            }
        }
    }
}

// CallChain[size=4] = qLogPrettyOpt() <-[Call]- QGit.restoreLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
// https://stackoverflow.com/a/1441062
// in Powershell
// git log --pretty=format:'%H%x09%an%x09%ad%x09%s' --date=short
private fun qLogPrettyOpt(shell: QShell) =
    "--pretty=format:${shell.quote}%H$TAB%an$TAB%ad$TAB%s${shell.quote} --date=short"

// CallChain[size=5] = QGitCommit <-[Ref]- QGitCommit.parsePrettyOutput() <-[Call]- QGit.restoreLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal class QGitCommit(rawText: String) {
    // CallChain[size=4] = QGitCommit.hash <-[Call]- QGit.restoreLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val hash: String
    // CallChain[size=6] = QGitCommit.author <-[Call]- QGitCommit.toString() <-[Propag]- QGitCommit.hash ... reLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val author: String
    // CallChain[size=6] = QGitCommit.date <-[Call]- QGitCommit.toString() <-[Propag]- QGitCommit.hash < ... reLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val date: String
    // CallChain[size=6] = QGitCommit.msg <-[Call]- QGitCommit.toString() <-[Propag]- QGitCommit.hash <- ... reLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val msg: String

    // CallChain[size=5] = QGitCommit.init { <-[Propag]- QGitCommit.hash <-[Call]- QGit.restoreLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    init {
        val spl = rawText.split(TAB)

        hash = spl[0]
        author = spl[1]
        date = spl[2]
        msg = spl[3]
    }

    // CallChain[size=5] = QGitCommit.toString() <-[Propag]- QGitCommit.hash <-[Call]- QGit.restoreLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    override fun toString(): String {
        return qBrackets("Hash", hash, "Author", author, "Date", date, "Msg", msg)
    }

    companion object {
        // CallChain[size=4] = QGitCommit.parsePrettyOutput() <-[Call]- QGit.restoreLostFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
        fun parsePrettyOutput(output: String): List<QGitCommit> {
            return output.lines().filter {
                !it.startsWith(":") && it.isNotBlank() && it.split(TAB).size == 4
            }.map {
                QGitCommit(it)
            }
        }
    }
}

// CallChain[size=4] = QGitObj <-[Ref]- QGit.findLargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal class QGitObj(val rawText: String, val git: QGit) {
    // CallChain[size=6] = QGitObj.type <-[Call]- QGitObj.toString() <-[Propag]- QGitObj.whatChanged() < ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val type: QGitObjType
    // CallChain[size=5] = QGitObj.hash <-[Call]- QGitObj.whatChanged() <-[Call]- QGit.findLargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val hash: String
    // CallChain[size=4] = QGitObj.size <-[Call]- QGit.findLargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val size: Int
    // CallChain[size=7] = QGitObj.diskSize <-[Call]- QGitObj.diskSizeStr <-[Call]- QGitObj.toString() < ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val diskSize: Int
    // CallChain[size=4] = QGitObj.path <-[Call]- QGit.findLargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val path: String

    // CallChain[size=5] = QGitObj.init { <-[Propag]- QGitObj.whatChanged() <-[Call]- QGit.findLargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    init {
        val spl = rawText.split(TAB)
        type = QGitObjType.fromText(spl[0])
        hash = spl[1]
        size = spl[2].toInt()
        diskSize = spl[3].toInt()
        path = spl[4]
    }

    // CallChain[size=6] = QGitObj.sizeStr <-[Call]- QGitObj.toString() <-[Propag]- QGitObj.whatChanged( ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val sizeStr: String by lazy { size.qToSizeString() }

    // CallChain[size=6] = QGitObj.diskSizeStr <-[Call]- QGitObj.toString() <-[Propag]- QGitObj.whatChan ... LargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val diskSizeStr: String by lazy { diskSize.qToSizeString() }

    // CallChain[size=4] = QGitObj.whatChanged() <-[Call]- QGit.findLargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    suspend fun whatChanged(): List<QGitCommit> {
        return git.whatchanged(hash)
    }

    // CallChain[size=5] = QGitObj.toString() <-[Propag]- QGitObj.whatChanged() <-[Call]- QGit.findLargeFiles() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    override fun toString(): String {
        return qBrackets(
            "Type",
            type.name,
            "Size",
            sizeStr,
            " DiskSize ",
            diskSizeStr,
            "Path",
            path.qWithMaxLengthMiddleDots(80),
            "Hash",
            hash
        )
    }
}

// CallChain[size=4] = QRepoVisibility <-[Ref]- QGit.createGitHubRepo() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal enum class QRepoVisibility {
    // CallChain[size=5] = QRepoVisibility.Public <-[Propag]- QRepoVisibility.Private <-[Call]- QGit.createGitHubRepo() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Public,
    // CallChain[size=4] = QRepoVisibility.Private <-[Call]- QGit.createGitHubRepo() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Private,
    // CallChain[size=5] = QRepoVisibility.Internal <-[Propag]- QRepoVisibility.Private <-[Call]- QGit.c ... GitHubRepo() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Internal;

    // CallChain[size=4] = QRepoVisibility.text <-[Call]- QGit.gh_repo_create_source() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    val text: String
        get() = name.toLowerCaseAsciiOnly()
}

// CallChain[size=4] = QGistVisibility <-[Ref]- QGit.gh_gist_create() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal enum class QGistVisibility {
    // CallChain[size=4] = QGistVisibility.Public <-[Call]- QGit.gh_gist_create() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Public,
    // CallChain[size=4] = QGistVisibility.Secret <-[Call]- QGit.gh_gist_create() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Secret
}

// CallChain[size=4] = QGitInitIfExists <-[Ref]- QGit.init() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal enum class QGitInitIfExists {
    // CallChain[size=4] = QGitInitIfExists.DoNothing <-[Call]- QGit.init() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    DoNothing,
    // CallChain[size=4] = QGitInitIfExists.Continue <-[Call]- QGit.init() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Continue
}

// CallChain[size=4] = QGistListVisibility <-[Ref]- QGit.gh_gist_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal enum class QGistListVisibility {
    // CallChain[size=4] = QGistListVisibility.Public <-[Call]- QGit.gh_gist_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Public,
    // CallChain[size=4] = QGistListVisibility.Secret <-[Call]- QGit.gh_gist_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Secret,
    // CallChain[size=4] = QGistListVisibility.Any <-[Call]- QGit.gh_gist_list() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    Any
}

// CallChain[size=3] = QPushForce <-[Ref]- QGit.push() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal enum class QPushForce {
    // CallChain[size=3] = QPushForce.None <-[Call]- QGit.push() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    None,
    // CallChain[size=3] = QPushForce.ForceSafely <-[Call]- QGit.push() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    ForceSafely,
    // CallChain[size=3] = QPushForce.ForceAtAnyCost <-[Call]- QGit.push() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    ForceAtAnyCost;
}

// CallChain[size=3] = QRemoteBranch <-[Ref]- QGit.push() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal data class QRemoteBranch(val remoteName: String, val remoteBranchName: String) {
    companion object {
        // CallChain[size=3] = QRemoteBranch.DEFAULT <-[Call]- QGit.push() <-[Call]- QCompactLibRepositoryTask.release()[Root]
        val DEFAULT = QRemoteBranch("", "")
        
    }
}