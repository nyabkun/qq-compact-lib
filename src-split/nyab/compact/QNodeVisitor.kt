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

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// << Root of the CallChain >>
class QNodeVisitor(
    val isMustMark: QIsMustMarkScope.() -> Boolean,
    val isMustDiscard: QIsMustDiscardScope.() -> Boolean,
    val resolved: QNodeResolvedScope.() -> QNodeVisited,
    val propagated: QNodePropagatedScope.() -> QNodeVisited,
)

// << Root of the CallChain >>
@QCompactLibDsl
class QNodeVisitorScope internal constructor() {
    // << Root of the CallChain >>
    private var isMustDiscard: QIsMustDiscardScope.() -> Boolean = QIsMustDiscardScope::default

    // << Root of the CallChain >>
    private var isMustMark: QIsMustMarkScope.() -> Boolean = QIsMustMarkScope::default

    // << Root of the CallChain >>
    private var resolved: QNodeResolvedScope.() -> QNodeVisited = QNodeResolvedScope::default

    // << Root of the CallChain >>
    private var propagated: QNodePropagatedScope.() -> QNodeVisited = QNodePropagatedScope::default

    // << Root of the CallChain >>
    private fun build(): QNodeVisitor {
        return QNodeVisitor(
            isMustMark = isMustMark,
            isMustDiscard = isMustDiscard,
            resolved = resolved,
            propagated = propagated,
        )
    }

    // << Root of the CallChain >>
    fun isMustMark(block: QIsMustMarkScope.() -> Boolean) {
        this.isMustMark = block
    }

    // << Root of the CallChain >>
    fun isMustDiscard(block: QIsMustDiscardScope.() -> Boolean) {
        this.isMustDiscard = block
    }

    // << Root of the CallChain >>
    fun resolved(block: QNodeResolvedScope.() -> QNodeVisited) {
        this.resolved = block
    }

    // << Root of the CallChain >>
    fun propagated(block: QNodePropagatedScope.() -> QNodeVisited) {
        this.propagated = block
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QIsMustMarkScope internal constructor(
    val lib: QCompactLib,
    val target: QChainNode,
    val from: QChainNode,
    val isPropagated: Boolean
) {
    // << Root of the CallChain >>
    fun default(): Boolean {
        if( !isPropagated )
            return false

        if (lib.noPropagationClasses.any { target.topLevel.qFqName() == it.qualifiedName} ) {
            return false
        }

        if (lib.fullPropagationClasses.any { it.qualifiedName == target.topLevel.qFqName() }) {
            return true
        }

        return target.run {
            isEnumEntry() || isClassInitializer() || isOverridden() || isInOpenClass() || isInAbstractClass() || hasOpenKeyword() || isCompanionObject() || isInInterface()
        }
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QIsMustDiscardScope internal constructor(
    val lib: QCompactLib,
    val target: QChainNode,
    val from: QChainNode,
    val isPropagated: Boolean
) {
    // << Root of the CallChain >>
    fun default(): Boolean {
        return false
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QNodeResolvedScope internal constructor(
    val lib: QCompactLib,
    val target: QChainNode,
    val from: QChainNode,
    val type: QResolvedType,
) {
    // << Root of the CallChain >>
    fun default(): QNodeVisited {
        return QNodeVisited.MarkThisNodeAndContinueChain
    }
}

// << Root of the CallChain >>
@QCompactLibDsl
class QNodePropagatedScope internal constructor(
    val lib: QCompactLib,
    val target: QChainNode,
    val from: QChainNode,
    val type: QPropagationType,
) {
    // << Root of the CallChain >>
    fun default(): QNodeVisited {
//        if (lib.noPropagationClasses.any { target.topLevel.qFqName() == it.qualifiedName} ) {
//            return QNodeVisited.DiscardThisNodeAndStopChain
//        }

        return QNodeVisited.DiscardThisNodeAndStopChain
    }
}