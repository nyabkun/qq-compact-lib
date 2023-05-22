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

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=12] = String.qMatches() <-[Call]- Path.qFind() <-[Call]- Collection<Path>.qFind()  ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal fun String.qMatches(matcher: QM): Boolean = matcher.matches(this)

// CallChain[size=12] = QMatchAny <-[Call]- QM.isAny() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFr ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal object QMatchAny : QM {
    // CallChain[size=13] = QMatchAny.matches() <-[Propag]- QMatchAny <-[Call]- QM.isAny() <-[Propag]- Q ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(text: String): Boolean {
        return true
    }

    // CallChain[size=13] = QMatchAny.toString() <-[Propag]- QMatchAny <-[Call]- QM.isAny() <-[Propag]-  ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString()
    }
}

// CallChain[size=12] = QMatchNone <-[Call]- QM.isNone() <-[Propag]- QM.exact() <-[Call]- qSrcFileAt ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal object QMatchNone : QM {
    // CallChain[size=13] = QMatchNone.matches() <-[Propag]- QMatchNone <-[Call]- QM.isNone() <-[Propag] ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(text: String): Boolean {
        return false
    }

    // CallChain[size=13] = QMatchNone.toString() <-[Propag]- QMatchNone <-[Call]- QM.isNone() <-[Propag ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun toString(): String {
        return this::class.simpleName.toString()
    }
}

// CallChain[size=11] = QM <-[Ref]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAt ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
internal interface QM {
    // CallChain[size=11] = QM.matches() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qS ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun matches(text: String): Boolean

    // CallChain[size=11] = QM.isAny() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrc ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun isAny(): Boolean = this == QMatchAny

    // CallChain[size=11] = QM.isNone() <-[Propag]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSr ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    fun isNone(): Boolean = this == QMatchNone

    companion object {
        // CallChain[size=4] = QM.any() <-[Call]- QPathSearchCondition.nameMatcher <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
        fun any(): QM = QMatchAny

        // CallChain[size=10] = QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcFileLinesAtFrame() <-[C ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        fun exact(text: String, ignoreCase: Boolean = false): QM = QExactMatch(text, ignoreCase)

        // CallChain[size=3] = QM.matches() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
        fun matches(ptn: Regex): QM = QRegexEntireMatch(ptn)

        // CallChain[size=8] = QM.startsWith() <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Cal ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
        fun startsWith(text: String, ignoreCase: Boolean = false): QM = QStartsWithMatch(text, ignoreCase)

        
    }
}

// CallChain[size=4] = QRegexEntireMatch <-[Call]- QM.matches() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
private class QRegexEntireMatch(val ptn: Regex) : QM {
    // CallChain[size=5] = QRegexEntireMatch.matches() <-[Propag]- QRegexEntireMatch <-[Call]- QM.matches() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    override fun matches(text: String): Boolean {
        return ptn.matches(text)
    }

    // CallChain[size=5] = QRegexEntireMatch.toString() <-[Propag]- QRegexEntireMatch <-[Call]- QM.matches() <-[Call]- qJAVA_HOME <-[Call]- QCompactLibAnalysis.compilerConfiguration[Root]
    override fun toString(): String {
        return this::class.simpleName + "(ptn=$ptn)"
    }
}

// CallChain[size=11] = QExactMatch <-[Call]- QM.exact() <-[Call]- qSrcFileAtFrame() <-[Call]- qSrcF ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private class QExactMatch(val textExact: String, val ignoreCase: Boolean = false) : QM {
    // CallChain[size=12] = QExactMatch.matches() <-[Propag]- QExactMatch <-[Call]- QM.exact() <-[Call]- ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(text: String): Boolean {
        return text.equals(textExact, ignoreCase)
    }

    // CallChain[size=12] = QExactMatch.toString() <-[Propag]- QExactMatch <-[Call]- QM.exact() <-[Call] ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun toString(): String {
        return this::class.simpleName + "(textExact=$textExact, ignoreCase=$ignoreCase)"
    }
}

// CallChain[size=9] = QStartsWithMatch <-[Call]- QM.startsWith() <-[Call]- QMyPath.src_root <-[Call ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
private class QStartsWithMatch(val textStartsWith: String, val ignoreCase: Boolean = false) : QM {
    // CallChain[size=10] = QStartsWithMatch.matches() <-[Propag]- QStartsWithMatch <-[Call]- QM.startsW ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun matches(text: String): Boolean {
        return text.startsWith(textStartsWith, ignoreCase)
    }

    // CallChain[size=10] = QStartsWithMatch.toString() <-[Propag]- QStartsWithMatch <-[Call]- QM.starts ...  QException.QException() <-[Ref]- QE.throwIt() <-[Call]- QTopLevelCompactElement.toSrcCode()[Root]
    override fun toString(): String {
        return this::class.simpleName + "(textStartsWith=$textStartsWith, ignoreCase=$ignoreCase)"
    }
}