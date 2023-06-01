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

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = QE <-[Ref]- QTopLevelCompactElement.toSrcCode()[Root]
internal typealias QE = QMyException

// CallChain[size=3] = QMyException <-[Ref]- QE <-[Ref]- QTopLevelCompactElement.toSrcCode()[Root]
internal enum class QMyException {
    // CallChain[size=4] = QMyException.Other <-[Call]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    Other,

    // CallChain[size=2] = QMyException.Unreachable <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    Unreachable,
    // CallChain[size=3] = QMyException.ShouldBeTrue <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
    ShouldBeTrue,
    // CallChain[size=3] = QMyException.ShouldBeFalse <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
    ShouldBeFalse,
    // CallChain[size=11] = QMyException.ShouldNotBeNull <-[Call]- T.qaNotNull() <-[Call]- qSrcFileAtFra ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    ShouldNotBeNull,
    // CallChain[size=4] = QMyException.ShouldBeEmptyDir <-[Call]- QGit.clone_sparse_checkout() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    ShouldBeEmptyDir,
    // CallChain[size=6] = QMyException.ShouldNotBeZero <-[Call]- Int.qaNotZero() <-[Call]- CharSequence ... ll]- Any.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    ShouldNotBeZero,
    // CallChain[size=3] = QMyException.ShouldBeEqual <-[Call]- Any.shouldBe() <-[Call]- QChainNode.chainFrom()[Root]
    ShouldBeEqual,
    // CallChain[size=3] = QMyException.ShouldBeDirectory <-[Call]- Path.qCopyFileIntoDir() <-[Call]- QCompactLib.createLibrary()[Root]
    ShouldBeDirectory,
    // CallChain[size=5] = QMyException.ShouldBeRegularFile <-[Call]- Path.qaFile() <-[Call]- QGit.gitHu ... dateMyself() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    ShouldBeRegularFile,
    // CallChain[size=7] = QMyException.ShouldBeRelativePath <-[Call]- Path.qToRelativePath() <-[Call]-  ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    ShouldBeRelativePath,
    // CallChain[size=9] = QMyException.ShouldBeEvenNumber <-[Call]- qBrackets() <-[Call]- qMySrcLinesAt ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    ShouldBeEvenNumber,
    // CallChain[size=5] = QMyException.ShouldNotBeSubDirectory <-[Call]- Path.qMoveDirTo() <-[Call]- QG ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    ShouldNotBeSubDirectory,

    // CallChain[size=4] = QMyException.CommandFail <-[Call]- List<String>.qExec() <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    CommandFail,
    // CallChain[size=10] = QMyException.FileNotFound <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLine ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    FileNotFound,
    // CallChain[size=3] = QMyException.DirectoryNotFound <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    DirectoryNotFound,
    // CallChain[size=4] = QMyException.FileAlreadyExists <-[Call]- Path.qWrite() <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    FileAlreadyExists,
    // CallChain[size=5] = QMyException.DirectoryAlreadyExists <-[Call]- Path.qMoveDirTo() <-[Call]- QGi ... eDirOrFile() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    DirectoryAlreadyExists,
    // CallChain[size=10] = QMyException.FetchLinesFail <-[Call]- Path.qFetchLinesAround() <-[Call]- qSr ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    FetchLinesFail,
    // CallChain[size=11] = QMyException.LineNumberExceedsMaximum <-[Call]- Path.qLineAt() <-[Call]- Pat ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    LineNumberExceedsMaximum,
    // CallChain[size=9] = QMyException.CreateZipFileFail <-[Call]- Path.qCreateZip() <-[Call]- QBackupF ... ) <-[Call]- Path.qConvertContent() <-[Call]- QCompactLibRepositoryTask.updateReadmeVersion()[Root]
    CreateZipFileFail,
    // CallChain[size=3] = QMyException.IllegalArgument <-[Call]- String.qWithMaxLengthMiddleDots() <-[Call]- QChainNode.chainsToString()[Root]
    IllegalArgument,
    // CallChain[size=4] = QMyException.OpenBrowserFail <-[Call]- qOpenBrowser() <-[Call]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    OpenBrowserFail,
    // CallChain[size=3] = QMyException.FileOpenFail <-[Call]- Path.qOpenEditor() <-[Call]- QCompactLibResult.openEditorAll()[Root]
    FileOpenFail,
    // CallChain[size=5] = QMyException.FunctionNotFound <-[Call]- KClass<*>.qFunction() <-[Call]- KClas ... qFunctionByName() <-[Call]- Any.qInvokeInstanceFunctionByName() <-[Call]- Any.qInvokeBuild()[Root]
    FunctionNotFound,
    // CallChain[size=8] = QMyException.MethodNotFound <-[Call]- Class<*>.qMethod() <-[Call]- Class<*>.q ... nameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
    MethodNotFound,
    // CallChain[size=2] = QMyException.ImportOffsetFail <-[Call]- KtFile.qImportOffsetRange()[Root]
    ImportOffsetFail,

    // CallChain[size=2] = QMyException.InvalidPhaseTransition <-[Call]- QPhase.nextPhase()[Root]
    InvalidPhaseTransition;

    
}

// CallChain[size=4] = qSTACK_FRAME_FILTER <-[Call]- QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal val qSTACK_FRAME_FILTER: (StackWalker.StackFrame) -> Boolean = {
    !it.className.startsWith("org.gradle") &&
            !it.className.startsWith("org.testng") &&
            !it.className.startsWith("worker.org.gradle") &&
            !it.methodName.endsWith("\$default") && it.fileName != null &&
//            && !it.className.startsWith(QException::class.qualifiedName!!)
//            && it.methodName != "invokeSuspend"
            it.declaringClass != null
//            && it.declaringClass.canonicalName != null
//            && !it.declaringClass.canonicalName.startsWith("kotlin.coroutines.jvm.internal.")
//            && !it.declaringClass.canonicalName.startsWith("kotlinx.coroutines")
}