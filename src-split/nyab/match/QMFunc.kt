/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.match

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.extensionReceiverParameter
import nyab.util.QFlagEnum

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=7] = qAnd() <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any?.qToS ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private fun qAnd(vararg matches: QMFunc): QMFunc = QMatchFuncAnd(*matches)

// CallChain[size=6] = QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal infix fun QMFunc.and(match: QMFunc): QMFunc {
    return if (this is QMatchFuncAnd) {
        QMatchFuncAnd(*matchList, match)
    } else {
        qAnd(this, match)
    }
}

// CallChain[size=8] = QMatchFuncAny <-[Call]- QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsIn ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private object QMatchFuncAny : QMFuncA() {
    // CallChain[size=9] = QMatchFuncAny.matches() <-[Propag]- QMatchFuncAny <-[Call]- QMFunc.isAny() <- ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=8] = QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtensions ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private object QMatchFuncNone : QMFuncA() {
    // CallChain[size=9] = QMatchFuncNone.matches() <-[Propag]- QMatchFuncNone <-[Call]- QMFunc.isNone() ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return false
    }
}

// CallChain[size=7] = QMatchFuncDeclaredOnly <-[Call]- QMFunc.DeclaredOnly <-[Call]- qToStringRegis ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private object QMatchFuncDeclaredOnly : QMFuncA() {
    // CallChain[size=8] = QMatchFuncDeclaredOnly.declaredOnly <-[Propag]- QMatchFuncDeclaredOnly <-[Cal ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override val declaredOnly = true

    // CallChain[size=8] = QMatchFuncDeclaredOnly.matches() <-[Propag]- QMatchFuncDeclaredOnly <-[Call]- ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=7] = QMatchFuncIncludeExtensionsInClass <-[Call]- QMFunc.IncludeExtensionsInClass  ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private object QMatchFuncIncludeExtensionsInClass : QMFuncA() {
    // CallChain[size=8] = QMatchFuncIncludeExtensionsInClass.includeExtensionsInClass <-[Propag]- QMatc ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override val includeExtensionsInClass = true

    // CallChain[size=8] = QMatchFuncIncludeExtensionsInClass.matches() <-[Propag]- QMatchFuncIncludeExt ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return true
    }
}

// CallChain[size=7] = QMatchFuncAnd <-[Ref]- QMFunc.and() <-[Call]- qToStringRegistry <-[Call]- Any ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private class QMatchFuncAnd(vararg match: QMFunc) : QMFuncA() {
    // CallChain[size=7] = QMatchFuncAnd.matchList <-[Call]- QMFunc.and() <-[Call]- qToStringRegistry <- ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val matchList = match

    // CallChain[size=8] = QMatchFuncAnd.declaredOnly <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMFu ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override val declaredOnly = matchList.any { it.declaredOnly }

    // CallChain[size=8] = QMatchFuncAnd.includeExtensionsInClass <-[Propag]- QMatchFuncAnd.matchList <- ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override val includeExtensionsInClass: Boolean = matchList.any { it.includeExtensionsInClass }

    // CallChain[size=8] = QMatchFuncAnd.matches() <-[Propag]- QMatchFuncAnd.matchList <-[Call]- QMFunc. ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return matchList.all { it.matches(value) }
    }
}

// CallChain[size=9] = QMFuncA <-[Call]- QMatchFuncNone <-[Call]- QMFunc.isNone() <-[Propag]- QMFunc ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private abstract class QMFuncA : QMFunc {
    // CallChain[size=10] = QMFuncA.declaredOnly <-[Propag]- QMFuncA <-[Call]- QMatchFuncNone <-[Call]-  ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override val declaredOnly: Boolean = false
    // CallChain[size=10] = QMFuncA.includeExtensionsInClass <-[Propag]- QMFuncA <-[Call]- QMatchFuncNon ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override val includeExtensionsInClass: Boolean = false
}

// CallChain[size=7] = QMFunc <-[Ref]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry < ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal interface QMFunc {
    // CallChain[size=7] = QMFunc.declaredOnly <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qTo ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val declaredOnly: Boolean

    // CallChain[size=7] = QMFunc.includeExtensionsInClass <-[Propag]- QMFunc.IncludeExtensionsInClass < ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val includeExtensionsInClass: Boolean

    // CallChain[size=7] = QMFunc.matches() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStr ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun matches(value: KFunction<*>): Boolean

    // CallChain[size=7] = QMFunc.isAny() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStrin ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun isAny(): Boolean = this == QMatchFuncAny

    // CallChain[size=7] = QMFunc.isNone() <-[Propag]- QMFunc.IncludeExtensionsInClass <-[Call]- qToStri ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun isNone(): Boolean = this == QMatchFuncNone

    companion object {
        // CallChain[size=6] = QMFunc.DeclaredOnly <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        val DeclaredOnly: QMFunc = QMatchFuncDeclaredOnly

        // CallChain[size=6] = QMFunc.IncludeExtensionsInClass <-[Call]- qToStringRegistry <-[Call]- Any?.qT ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        // TODO OnlyExtensionsInClass
        val IncludeExtensionsInClass: QMFunc = QMatchFuncIncludeExtensionsInClass

        

        // TODO vararg, nullability, param names, type parameter
        // TODO handle createType() more carefully

        // CallChain[size=6] = QMFunc.nameExact() <-[Call]- qToStringRegistry <-[Call]- Any?.qToString() <-[Call]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        fun nameExact(text: String, ignoreCase: Boolean = false): QMFunc {
            return QMatchFuncName(QM.exact(text, ignoreCase = ignoreCase))
        }

        
    }
}

// CallChain[size=7] = QMatchFuncName <-[Call]- QMFunc.nameExact() <-[Call]- qToStringRegistry <-[Ca ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private class QMatchFuncName(val nameMatcher: QM) : QMFuncA() {
    // CallChain[size=8] = QMatchFuncName.matches() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameExac ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(value: KFunction<*>): Boolean {
        return nameMatcher.matches(value.name)
    }

    // CallChain[size=8] = QMatchFuncName.toString() <-[Propag]- QMatchFuncName <-[Call]- QMFunc.nameExa ... l]- Any?.qToLogString() <-[Call]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun toString(): String {
        return this::class.simpleName + ":" + nameMatcher.toString()
    }
}