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
enum class QKtVisibility {
    // << Root of the CallChain >>
    Public,
    // << Root of the CallChain >>
    Protected,
    // << Root of the CallChain >>
    Private,
    // << Root of the CallChain >>
    Internal,
    // << Root of the CallChain >>
    Unknown
}

// << Root of the CallChain >>
enum class QDeclarationType {
    // << Root of the CallChain >>
    Unknown,
    // << Root of the CallChain >>
    Function,
    // << Root of the CallChain >>
    Property,
    // << Root of the CallChain >>
    Class,
    // << Root of the CallChain >>
    DataClass,
    // << Root of the CallChain >>
    EnumClass,
    // << Root of the CallChain >>
    AnnotationClass,
    // << Root of the CallChain >>
    ClassInitializer,
    // << Root of the CallChain >>
    CompanionObject,
    // << Root of the CallChain >>
    ObjectLiteral,
    // << Root of the CallChain >>
    TypeAlias,
    // << Root of the CallChain >>
    PrimaryConstructor;
}

// << Root of the CallChain >>
enum class QKtVisibilityChange {
    // << Root of the CallChain >>
    NoChange,
    // << Root of the CallChain >>
    ToPrivate,
    // << Root of the CallChain >>
    ToInternal
}

// << Root of the CallChain >>
enum class QChainNodeVisitResult {
    // << Root of the CallChain >>
    /**
     * This node is the target API we want to use directly in the user code.
     * This node is the root of the call chain.
     */
    RootOfChain,

    // << Root of the CallChain >>
    /**
     * Add this node if this node is in call chain.
     */
    Chainable,

    // << Root of the CallChain >>
    /**
     * Add this node and search for next chain nodes in src text of this node.
     */
    ForceChain,

    // << Root of the CallChain >>
    /**
     * Ignore this node and src text inside this node.
     */
    Ignore
}

// << Root of the CallChain >>
enum class QChainNodeCompositeType {
    // << Root of the CallChain >>
    /**
     * Properties, Functions, etc.
     * Simple source code elements that do not inspect dependencies down to the internal structure.
     */
    NoChildren,

    // << Root of the CallChain >>
    /**
     * Class, companion object, etc.
     * Resolve the dependencies for each element in the class body and extract only the necessary parts.
     */
    HasChildren;
}

// << Root of the CallChain >>
/**
 * ex.
 * class (top) -> property / function (second)
 * class (top) -> companion object (second) -> property / function (third)
 */
enum class QSrcLevel(val intValue: Int) {
    // << Root of the CallChain >>
    Top(0),
    // << Root of the CallChain >>
    Second(1),
    // << Root of the CallChain >>
    Third(2),
    // << Root of the CallChain >>
    Deep(100);

    companion object {
        // << Root of the CallChain >>
        fun fromIntValue(intValue: Int): QSrcLevel {
            return when (intValue) {
                0 -> Top
                1 -> Second
                2 -> Third
                else -> Deep
            }
        }
    }
}

// << Root of the CallChain >>
enum class QChainNodeType {
    // << Root of the CallChain >>
    ClassInitializer,
    // << Root of the CallChain >>
    CompanionObject,
    // << Root of the CallChain >>
    CompanionObjectMemberProperty,
    // << Root of the CallChain >>
    CompanionObjectMemberFunction,
    // << Root of the CallChain >>
    SecondLevelProperty,
    // << Root of the CallChain >>
    SecondLevelFunction,
    // << Root of the CallChain >>
    TopLevelProperty,
    // << Root of the CallChain >>
    TopLevelFunction
}

// << Root of the CallChain >>
enum class QNodeVisited {
    // << Root of the CallChain >>
    MarkThisNodeAndContinueChain,
    // << Root of the CallChain >>
    DiscardThisNodeAndStopChain
}

// << Root of the CallChain >>
enum class QLibPhase {
    // << Root of the CallChain >>
    InitialState,
    // << Root of the CallChain >>
    ConfigurationFinished,
    // << Root of the CallChain >>
    MainSrcAnalysis,
    // << Root of the CallChain >>
    CleanExistingSrc,
    // << Root of the CallChain >>
    ReadExampleSrc,
    // << Root of the CallChain >>
    CreateTestSrcAnalysisAndSrc,
    // << Root of the CallChain >>
    CreateMainSrc,
    // << Root of the CallChain >>
    CheckSrcHash,
    // << Root of the CallChain >>
    CreateBuildGradle,
    // << Root of the CallChain >>
    GradleBuild,

    // << Root of the CallChain >>
    CreateGitIgnore,
    // << Root of the CallChain >>
    CreateLicense,
    // << Root of the CallChain >>
    CreateChainNodeCount,
    // << Root of the CallChain >>
    CreateReadme,
    // << Root of the CallChain >>
    Result,
    // << Root of the CallChain >>
    FinishTask
}

// << Root of the CallChain >>
enum class QAnalysisPhase {
    // << Root of the CallChain >>
    CompilerAnalysisSettings,
    // << Root of the CallChain >>
    RegisterRootOfChain,
    // << Root of the CallChain >>
    CompilerAnalysisStarted,
    // << Root of the CallChain >>
    CompilerAnalysisFinished,
    // << Root of the CallChain >>
    CallChainResolvingStarted,
    // << Root of the CallChain >>
    CallChainResolvingFinished,
    // << Root of the CallChain >>
    CallChainAnalysisFinalWork,
    // << Root of the CallChain >>
    CreateSingleSrcBase,
    // << Root of the CallChain >>
    CreateSingleSrc,
    // << Root of the CallChain >>
    CreateSplitSrc
}