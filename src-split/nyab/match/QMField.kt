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

import java.lang.reflect.Field
import nyab.util.QFlagEnum
import nyab.util.qIsAssignableFrom

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=7] = qAnd() <-[Call]- QMField.and() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
private fun qAnd(vararg matches: QMField): QMField = QMatchFieldAnd(*matches)

// CallChain[size=6] = QMField.and() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
internal infix fun QMField.and(match: QMField): QMField {
    return if (this is QMatchFieldAnd) {
        QMatchFieldAnd(*matchList, match)
    } else {
        qAnd(this, match)
    }
}

// CallChain[size=9] = QMatchFieldAny <-[Call]- QMField.isAny() <-[Propag]- QMField.matches() <-[Cal ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
private object QMatchFieldAny : QMFieldA() {
    // CallChain[size=10] = QMatchFieldAny.matches() <-[Propag]- QMatchFieldAny <-[Call]- QMField.isAny( ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    override fun matches(value: Field): Boolean {
        return true
    }
}

// CallChain[size=9] = QMatchFieldNone <-[Call]- QMField.isNone() <-[Propag]- QMField.matches() <-[C ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
private object QMatchFieldNone : QMFieldA() {
    // CallChain[size=10] = QMatchFieldNone.matches() <-[Propag]- QMatchFieldNone <-[Call]- QMField.isNo ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    override fun matches(value: Field): Boolean {
        return false
    }
}

// CallChain[size=7] = QMatchFieldDeclaredOnly <-[Call]- QMField.DeclaredOnly <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
private object QMatchFieldDeclaredOnly : QMFieldA() {
    // CallChain[size=8] = QMatchFieldDeclaredOnly.declaredOnly <-[Propag]- QMatchFieldDeclaredOnly <-[C ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    override val declaredOnly = true

    // CallChain[size=8] = QMatchFieldDeclaredOnly.matches() <-[Propag]- QMatchFieldDeclaredOnly <-[Call ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    override fun matches(value: Field): Boolean {
        return true
    }
}

// CallChain[size=7] = QMatchFieldAnd <-[Ref]- QMField.and() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
private class QMatchFieldAnd(vararg match: QMField) : QMFieldA() {
    // CallChain[size=7] = QMatchFieldAnd.matchList <-[Call]- QMField.and() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    val matchList = match

    // CallChain[size=8] = QMatchFieldAnd.declaredOnly <-[Propag]- QMatchFieldAnd.matchList <-[Call]- QM ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    override val declaredOnly = matchList.any { it.declaredOnly }

    // CallChain[size=8] = QMatchFieldAnd.matches() <-[Propag]- QMatchFieldAnd.matchList <-[Call]- QMFie ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    override fun matches(value: Field): Boolean {
        return matchList.all { it.matches(value) }
    }
}

// CallChain[size=10] = QMFieldA <-[Call]- QMatchFieldNone <-[Call]- QMField.isNone() <-[Propag]- QM ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
private abstract class QMFieldA : QMField {
    // CallChain[size=11] = QMFieldA.declaredOnly <-[Propag]- QMFieldA <-[Call]- QMatchFieldNone <-[Call ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    override val declaredOnly: Boolean = false
}

// CallChain[size=6] = QMField <-[Ref]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
internal interface QMField {
    // CallChain[size=7] = QMField.declaredOnly <-[Call]- Class<*>.qFields() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    val declaredOnly: Boolean

    // CallChain[size=7] = QMField.matches() <-[Call]- Class<*>.qFields() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    fun matches(value: Field): Boolean

    // CallChain[size=8] = QMField.isAny() <-[Propag]- QMField.matches() <-[Call]- Class<*>.qFields() <- ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    fun isAny(): Boolean = this == QMatchFieldAny

    // CallChain[size=8] = QMField.isNone() <-[Propag]- QMField.matches() <-[Call]- Class<*>.qFields() < ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    fun isNone(): Boolean = this == QMatchFieldNone

    companion object {
        // CallChain[size=6] = QMField.DeclaredOnly <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
        val DeclaredOnly: QMField = QMatchFieldDeclaredOnly

        // CallChain[size=6] = QMField.type() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
        fun type(type: Class<*>, allowSubTypeField: Boolean = true): QMField {
            return QMatchFieldType(type, allowSubTypeField)
        }

        
    }
}

// CallChain[size=7] = QMatchFieldType <-[Call]- QMField.type() <-[Call]- Any.qFieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
private class QMatchFieldType(val receivingType: Class<*>, val allowSubTypeField: Boolean) : QMFieldA() {
    // CallChain[size=8] = QMatchFieldType.matches() <-[Propag]- QMatchFieldType <-[Call]- QMField.type( ... FieldValues() <-[Call]- QMyDepI.ALL <-[Propag]- QMyDepI <-[Ref]- QMyDep <-[Ref]- QCompactLib[Root]
    override fun matches(value: Field): Boolean {
        return if (allowSubTypeField) {
            // Number is assignable from Integer
            receivingType.qIsAssignableFrom(value.type)
        } else {
            value.type == receivingType
        }
    }
}