/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("SpellCheckingInspection", "UNCHECKED_CAST")

package nyab.util

import kotlin.reflect.KClass

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=11] = QFlag <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[Call]- q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
/**
 * Only Enum or QFlag can implement this interface.
 */
internal sealed interface QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=13] = QFlag.bits <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOptE ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val bits: Int

    // CallChain[size=13] = QFlag.contains() <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>. ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun contains(flags: QFlag<T>): Boolean {
        return (bits and flags.bits) == flags.bits
    }

    // CallChain[size=12] = QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOptEnums() <-[Call]- Path.q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun toEnumValues(): List<T>

    // CallChain[size=13] = QFlag.str() <-[Propag]- QFlag.toEnumValues() <-[Call]- QFlag<QOpenOpt>.toOpt ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun str(): String {
        return toEnumValues().joinToString(", ") { it.name }
    }

    companion object {
        // https://discuss.kotlinlang.org/t/reified-generics-on-class-level/16711/2
        // But, can't make constructor private ...

        // CallChain[size=11] = QFlag.none() <-[Call]- Path.qReader() <-[Call]- Path.qFetchLinesAround() <-[ ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        inline fun <reified T>
        none(): QFlag<T> where T : QFlag<T>, T : Enum<T> {
            return QFlagSet<T>(T::class, 0)
        }
    }
}

// CallChain[size=12] = QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Path.qReader() <-[Call]- Path.qFetchLin ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal interface QFlagEnum<T> : QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=13] = QFlagEnum.bits <-[Propag]- QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Path.qReader ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override val bits: Int
        get() = 1 shl (this as T).ordinal
    // CallChain[size=13] = QFlagEnum.toEnumValues() <-[Propag]- QFlagEnum <-[Ref]- QOpenOpt <-[Ref]- Pa ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun toEnumValues(): List<T> = listOf(this) as List<T>
}

// CallChain[size=12] = QFlagSet <-[Call]- QFlag.none() <-[Call]- Path.qReader() <-[Call]- Path.qFet ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
/**
 * Mutable bit flag
 */
internal class QFlagSet<T>(val enumClass: KClass<T>, override var bits: Int) : QFlag<T> where T : QFlag<T>, T : Enum<T> {
    // CallChain[size=14] = QFlagSet.enumValues <-[Call]- QFlagSet.toEnumValues() <-[Propag]- QFlagSet < ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    val enumValues: Array<T> by lazy { enumClass.qEnumValues() }

    // CallChain[size=13] = QFlagSet.toEnumValues() <-[Propag]- QFlagSet <-[Call]- QFlag.none() <-[Call] ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun toEnumValues(): List<T> =
        enumValues.filter { contains(it) }
}

// CallChain[size=9] = QFlag<T>.plus() <-[Call]- Path.qCopyFileTo() <-[Call]- QBackupFile.createBack ... ckup() <-[Call]- Path.qWrite() <-[Call]- QGit.init() <-[Call]- QCompactLibResult.doGitTask()[Root]
@Suppress("UNCHECKED_CAST")
internal operator fun <T> QFlag<T>.plus(other: QFlag<T>): QFlagSet<T> where T : QFlag<T>, T : Enum<T> {
    return if (this is QFlagSet) {
        this.bits = this.bits or other.bits
        this
    } else if (other is QFlagSet) {
        other.bits = this.bits or other.bits
        other
    } else {
        QFlagSet(this::class as KClass<T>, bits or other.bits)
    }
}