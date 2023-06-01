/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("UNCHECKED_CAST")

package nyab.util

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.useLines
import kotlin.reflect.KClass
import nyab.conf.QE
import nyab.conf.QMyPath
import nyab.match.QMField
import nyab.match.and

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = qThisFilePackageName <-[Call]- QSingleSrcBase.toSrcCode()[Root]
internal val qThisFilePackageName: String = qCallerPackageName(0)

// CallChain[size=2] = Path.qGetKotlinPackageNameQuickAndDirty() <-[Call]- Path.qMainClassFqName()[Root]
internal fun Path.qGetKotlinPackageNameQuickAndDirty(charset: Charset = Charsets.UTF_8): String {
    val regex = Regex("package\\s+(\\S+)")
    return this.bufferedReader(charset).use {
        it.lineSequence().forEach { line ->
            val match = regex.find(line)
            if (match != null) {
                return match.groups[1]!!.value
            }
        }

        ""
    }
}

// CallChain[size=2] = Path.qHasKotlinMainFunctionQuickAndDirty() <-[Call]- Path.qMainClassFqName()[Root]
internal fun Path.qHasKotlinMainFunctionQuickAndDirty(charset: Charset = Charsets.UTF_8): Boolean {
    this.useLines(charset) { lines ->
        for (line in lines) {
            if (line.startsWith("fun main(")) {
                return true
            }
        }
    }

    return false
}

// CallChain[size=6] = Class<*>.qFields() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
internal fun Class<*>.qFields(matcher: QMField = QMField.DeclaredOnly): List<Field> {
    val allFields = if (matcher.declaredOnly) declaredFields else this.fields

    return allFields.filter { matcher.matches(it) }
}

// CallChain[size=5] = Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
internal inline fun <reified T> Any.qFieldValues(matcher: QMField = QMField.DeclaredOnly): List<T> {
    val aMatcher = matcher and QMField.type(T::class.java)

    val fields = this::class.java.qFields(aMatcher)

    for (field in fields) {
        field.isAccessible = true
    }

    return fields.map { it.get(this) as T }.toList()
}

// CallChain[size=7] = Class<*>.qMethod() <-[Call]- Class<*>.qMethod() <-[Call]- qGetMethodByFqName( ... nameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Class<*>.qMethod(declaredOnly: Boolean = false, find: (Method) -> Boolean): Method {
    val allMethods = if (declaredOnly) declaredMethods else methods
    return allMethods.find(find).qaNotNull(QE.MethodNotFound)
}

// CallChain[size=6] = Class<*>.qMethod() <-[Call]- qGetMethodByFqName() <-[Call]- KClass<*>.qContai ... nameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Class<*>.qMethod(name: String, declaredOnly: Boolean = false): Method {
    return qMethod(declaredOnly) { it.name == name }
}

// CallChain[size=10] = Class<*>.qPrimitiveToWrapper() <-[Call]- Class<*>.qIsAssignableFrom() <-[Cal ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
internal fun Class<*>.qPrimitiveToWrapper(): Class<*> = qJVMPrimitiveToWrapperMap[this] ?: this

// CallChain[size=11] = qJVMPrimitiveToWrapperMap <-[Call]- Class<*>.qPrimitiveToWrapper() <-[Call]- ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
internal val qJVMPrimitiveToWrapperMap by lazy {
    val map = HashMap<Class<*>, Class<*>>()

    map[java.lang.Boolean.TYPE] = java.lang.Boolean::class.java
    map[java.lang.Byte.TYPE] = java.lang.Byte::class.java
    map[java.lang.Character.TYPE] = java.lang.Character::class.java
    map[java.lang.Short.TYPE] = java.lang.Short::class.java
    map[java.lang.Integer.TYPE] = java.lang.Integer::class.java
    map[java.lang.Long.TYPE] = java.lang.Long::class.java
    map[java.lang.Double.TYPE] = java.lang.Double::class.java
    map[java.lang.Float.TYPE] = java.lang.Float::class.java
    map
}

// CallChain[size=9] = Class<*>.qIsAssignableFrom() <-[Call]- QMatchFieldType.matches() <-[Propag]-  ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
internal fun Class<*>.qIsAssignableFrom(subclass: Class<*>, autoboxing: Boolean = true): Boolean {
    return if (autoboxing) {
        this.qPrimitiveToWrapper().isAssignableFrom(subclass.qPrimitiveToWrapper())
    } else {
        this.isAssignableFrom(subclass)
    }
}

// CallChain[size=5] = qGetMethodByFqName() <-[Call]- KClass<*>.qContainingFileMainMethod() <-[Call] ... nameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun qGetMethodByFqName(methodFqName: String): Method {
    val clsName = methodFqName.substring(0, methodFqName.lastIndexOf('.'))
    val methodName = methodFqName.substring(methodFqName.lastIndexOf('.') + 1, methodFqName.length)
    val cls = Class.forName(clsName)
    return cls.qMethod(methodName, declaredOnly = true)
}

// CallChain[size=5] = Method.qFqName() <-[Call]- qRunMethodInNewJVMProcess() <-[Call]- QGit.renameBranch() <-[Propag]- QGit.openRepository() <-[Call]- QCompactLibRepositoryTask.release()[Root]
internal fun Method.qFqName(): String {
    return this.declaringClass.name + "." + this.name
}

// CallChain[size=3] = qCallerPackageName() <-[Call]- qThisFilePackageName <-[Call]- QSingleSrcBase.toSrcCode()[Root]
internal fun qCallerPackageName(stackDepth: Int = 0): String {
    val frame = qStackFrame(stackDepth + 2)
    return frame.declaringClass.packageName
}