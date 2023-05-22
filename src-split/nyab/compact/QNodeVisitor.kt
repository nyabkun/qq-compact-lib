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
    val isMustMark: QChainNode.() -> Boolean,
    val isMustDiscard: QChainNode.() -> Boolean,
    val resolved: QNodeResolvedScope.() -> QNodeVisited,
    val propagated: QNodePropagatedScope.() -> QNodeVisited,
)

// << Root of the CallChain >>
@QCompactLibDsl
class QNodeVisitorScope internal constructor() {
    // << Root of the CallChain >>
    private var isMustMark: QChainNode.() -> Boolean = {
        isEnumEntry() || isClassInitializer() || isOverridden() || isInOpenClass() || isInAbstractClass() || hasOpenKeyword() || isCompanionObject() || isInInterface()
    }

    // << Root of the CallChain >>
    private var isMustDiscard: QChainNode.() -> Boolean = {
        false
    }

    // << Root of the CallChain >>
    private var resolved: QNodeResolvedScope.() -> QNodeVisited = {
        QNodeVisited.MarkThisNodeAndContinueChain
    }

    // << Root of the CallChain >>
    private var propagated: QNodePropagatedScope.() -> QNodeVisited = {
        QNodeVisited.DiscardThisNodeAndStopChain
    }

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
    fun isMustMark(block: QChainNode.() -> Boolean) {
        this.isMustMark = block
    }

    // << Root of the CallChain >>
    fun isMustDiscard(block: QChainNode.() -> Boolean) {
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
class QNodeResolvedScope internal constructor(
        val target: QChainNode,
        val from: QChainNode,
        val type: QResolvedType,
)

// << Root of the CallChain >>
@QCompactLibDsl
class QNodePropagatedScope internal constructor(
        val target: QChainNode,
        val from: QChainNode,
        val type: QPropagationType,
)