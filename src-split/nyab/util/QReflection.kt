/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE", "FunctionName")

package nyab.util

import java.lang.StackWalker.StackFrame
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberExtensionFunctions
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.streams.asSequence
import nyab.conf.QE
import nyab.conf.QMyPath
import nyab.match.QM
import nyab.match.QMFunc
import nyab.match.and

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=6] = KClass<*>.qFunctions() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun KClass<*>.qFunctions(matcher: QMFunc = QMFunc.DeclaredOnly and QMFunc.IncludeExtensionsInClass): List<KFunction<*>> {
    val list = mutableListOf<KFunction<*>>()

    var functions = if (matcher.declaredOnly) {
        this.declaredFunctions
    } else {
        this.memberFunctions
    }

    list += functions.filter { matcher.matches(it) }

    if (matcher.includeExtensionsInClass) {
        functions = if (matcher.declaredOnly) {
            this.declaredMemberExtensionFunctions
        } else {
            this.memberExtensionFunctions
        }

        list += functions.filter { matcher.matches(it) }
    }

    return list
}

// CallChain[size=3] = KClass<*>.qFunctionByName() <-[Call]- Any.qInvokeInstanceFunctionByName() <-[Call]- Any.qInvokeBuild()[Root]
internal fun KClass<*>.qFunctionByName(name: String): KFunction<*> {
    return this.qFunction(QMFunc.nameExact(name))
}

// CallChain[size=4] = KClass<*>.qFunction() <-[Call]- KClass<*>.qFunctionByName() <-[Call]- Any.qInvokeInstanceFunctionByName() <-[Call]- Any.qInvokeBuild()[Root]
internal fun KClass<*>.qFunction(matcher: QMFunc = QMFunc.DeclaredOnly and QMFunc.IncludeExtensionsInClass): KFunction<*> {
    var functions = if (matcher.declaredOnly) {
        this.declaredFunctions
    } else {
        this.memberFunctions
    }

    var func = functions.find {
        matcher.matches(it)
    }

    if (func == null && matcher.includeExtensionsInClass) {
        functions = if (matcher.declaredOnly) {
            this.declaredMemberExtensionFunctions
        } else {
            this.memberExtensionFunctions
        }
    }

    func = functions.find {
        matcher.matches(it)
    }

    if (func == null) {
        QE.FunctionNotFound.throwItBrackets("Class", this.simpleName, "Matcher", matcher)
    }

    return func
}

// CallChain[size=2] = Any.qInvokeInstanceFunctionByName() <-[Call]- Any.qInvokeBuild()[Root]
internal fun Any.qInvokeInstanceFunctionByName(
        vararg params: Any?,
        name: String,
): Any? {
    val func = this::class.qFunctionByName(name)
    func.isAccessible = true

    return func.call(this, *params)
}

// CallChain[size=15] = KClass<E>.qEnumValues() <-[Call]- QFlagSet.enumValues <-[Call]- QFlagSet.toE ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun <E : Enum<E>> KClass<E>.qEnumValues(): Array<E> {
    return java.enumConstants as Array<E>
}

// CallChain[size=2] = qThisFileName <-[Call]- QSingleSrcBase.toSrcCode()[Root]
internal val qThisFileName: String
    get() = qCallerFileName()

// CallChain[size=3] = qThisSrcLineSignature <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
internal val qThisSrcLineSignature: String
    get() = qCallerSrcLineSignature()

// CallChain[size=9] = qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLinesAtFra ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun qSrcFileAtFrame(frame: StackFrame, srcRoots: List<Path> = QMyPath.src_root, pkgDirHint: String? = null): Path = qCacheItOneSec(
        frame.fileName + frame.lineNumber + srcRoots.map { it }.joinToString() + pkgDirHint
) {
    val pkgDir = pkgDirHint ?: frame.declaringClass.packageName.replace(".", "/")

    var srcFile: Path? = null

    for (dir in srcRoots) {
        val root = dir.toAbsolutePath()
        val fileInPkgDir = root.resolve(pkgDir).resolve(frame.fileName)
        if (fileInPkgDir.exists()) {
            srcFile = fileInPkgDir
            break
        } else {
            val fileNoPkgDir = root.resolve(frame.fileName)
            if (fileNoPkgDir.exists()) {
                srcFile = fileNoPkgDir
            }
        }
    }

    if (srcFile != null)
        return@qCacheItOneSec srcFile

    return@qCacheItOneSec srcRoots.qFind(QM.exact(frame.fileName), maxDepth = 100)
            .qaNotNull(QE.FileNotFound, qBrackets("FileName", frame.fileName, "SrcRoots", srcRoots))
}

// CallChain[size=3] = qCallerFileName() <-[Call]- qThisFileName <-[Call]- QSingleSrcBase.toSrcCode()[Root]
internal fun qCallerFileName(stackDepth: Int = 0): String {
    return qStackFrame(stackDepth + 2).fileName
}

// CallChain[size=4] = qCallerSrcLineSignature() <-[Call]- qThisSrcLineSignature <-[Call]- qTimeIt() <-[Call]- QCompactLibAnalysis.analysisResult[Root]
internal fun qCallerSrcLineSignature(stackDepth: Int = 0): String {
    val frame = qStackFrame(stackDepth + 2)

    return if (frame.declaringClass?.canonicalName == null) {
        if (frame.fileName != null) {
            if (frame.methodName != "invokeSuspend") {
                frame.fileName + " - " + frame.methodName + " - " + frame.lineNumber
            } else {
                frame.fileName + " - " + frame.lineNumber
            }
        } else {
            frame.methodName + " - " + frame.lineNumber
        }
    } else {
        frame.declaringClass.canonicalName + "." + frame.methodName + " - " + frame.lineNumber
    }
}

// CallChain[size=6] = qStackFrames() <-[Call]- QException.stackFrames <-[Call]- QException.getStack ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal inline fun qStackFrames(
        stackDepth: Int = 0,
        size: Int = 1,
        noinline filter: (StackFrame) -> Boolean = QE.STACK_FRAME_FILTER,
): List<StackFrame> {
    return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { s: Stream<StackFrame> ->
        s.asSequence().filter(filter).drop(stackDepth).take(size).toList()
    }
}

// CallChain[size=9] = qStackFrame() <-[Call]- qSrcFileLinesAtFrame() <-[Call]- qMySrcLinesAtFrame() ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal inline fun qStackFrame(
        stackDepth: Int = 0,
        noinline filter: (StackFrame) -> Boolean = QE.STACK_FRAME_FILTER,
): StackFrame {
    return qStackFrames(stackDepth, 1, filter)[0]
}

// CallChain[size=7] = KType.qToClass() <-[Call]- KType.qIsSuperclassOf() <-[Call]- qToStringRegistr ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun KType.qToClass(): KClass<*>? {
    return if (this.classifier != null && this.classifier is KClass<*>) {
        this.classifier as KClass<*>
    } else {
        null
    }
}

// CallChain[size=6] = KType.qIsSuperclassOf() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString( ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun KType.qIsSuperclassOf(cls: KClass<*>): Boolean {
    return try {
        val thisClass = qToClass()

        if (thisClass?.qualifiedName == "kotlin.Array" && cls.qualifiedName == "kotlin.Array") {
            true
        } else {
            thisClass?.isSuperclassOf(cls) ?: false
        }
    } catch (e: Throwable) {
        // Exception in thread "main" kotlin.reflect.jvm.internal.KotlinReflectionInternalError: Unresolved class: ~
        false
    }
}