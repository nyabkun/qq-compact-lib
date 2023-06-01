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

import nyab.util.QGit

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleWrapper() {
    runCommand("gradle wrapper")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleBuild(): Boolean {
    val result = runCommand("gradlew build")
    return result.success
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.getGitHubLatestReleaseVersion(): String {
    return QGit(this.destProjDir).getGitHubLatestRelease()
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleJar() {
    runCommand("gradlew jar")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleTest() {
    runCommand("gradlew test")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradlePublish() {
    runCommand("gradlew publish")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleRunTestSingle() {
    runCommand("gradlew qRunTestSingle")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleRunTestSplit() {
    runCommand("gradlew qRunTestSplit")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleRunExample() {
    runCommand("gradlew qRunExample")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleRunMainSingle() {
    runCommand("gradlew qRunMainSingle")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleRunMainSplit() {
    runCommand("gradlew qRunMainSplit")
}

// << Root of the CallChain >>
internal suspend fun QCompactLib.gradleSpotlessApply(quiet: Boolean = true) {
    runCommand("gradlew spotlessApply", quiet)
}