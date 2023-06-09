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

// CallChain[size=5] = QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
@Suppress("EnumEntryName")
enum class QMyDepConf {
    // region #DepConf [Auto Generated by QConvert.kt]

    // CallChain[size=6] = QMyDepConf.implementationDependenciesMetadata <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    implementationDependenciesMetadata,
    // CallChain[size=6] = QMyDepConf.experimentImplementationDependenciesMetadata <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    experimentImplementationDependenciesMetadata,
    // CallChain[size=6] = QMyDepConf.testImplementationDependenciesMetadata <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    testImplementationDependenciesMetadata,
    // CallChain[size=6] = QMyDepConf.testRuntimeOnlyDependenciesMetadata <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    testRuntimeOnlyDependenciesMetadata,
    // CallChain[size=6] = QMyDepConf.runtimeOnlyDependenciesMetadata <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    runtimeOnlyDependenciesMetadata,

    // CallChain[size=6] = QMyDepConf.implementation <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    implementation,
    // CallChain[size=6] = QMyDepConf.runtimeOnly <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    runtimeOnly,
    // CallChain[size=6] = QMyDepConf.testImplementation <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    testImplementation,
    // CallChain[size=6] = QMyDepConf.testRuntimeOnly <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    testRuntimeOnly,

    // CallChain[size=6] = QMyDepConf.compact <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    compact,
    // CallChain[size=6] = QMyDepConf.compactLibraryCompileClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    compactLibraryCompileClasspath,
    // CallChain[size=6] = QMyDepConf.compactLibraryRuntimeClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    compactLibraryRuntimeClasspath,
    // CallChain[size=6] = QMyDepConf.compileClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    compileClasspath,
    // CallChain[size=6] = QMyDepConf.experimentCompileClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    experimentCompileClasspath,
    // CallChain[size=6] = QMyDepConf.experimentImplementation <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    experimentImplementation,
    // CallChain[size=6] = QMyDepConf.experimentRuntimeClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    experimentRuntimeClasspath,
    // CallChain[size=6] = QMyDepConf.kotlinCompilerClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    kotlinCompilerClasspath,
    // CallChain[size=6] = QMyDepConf.kotlinCompilerPluginClasspathCompact <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    kotlinCompilerPluginClasspathCompact,
    // CallChain[size=6] = QMyDepConf.kotlinCompilerPluginClasspathExperiment <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    kotlinCompilerPluginClasspathExperiment,
    // CallChain[size=6] = QMyDepConf.kotlinCompilerPluginClasspathMain <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    kotlinCompilerPluginClasspathMain,
    // CallChain[size=6] = QMyDepConf.kotlinCompilerPluginClasspathTest <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    kotlinCompilerPluginClasspathTest,
    // CallChain[size=6] = QMyDepConf.kotlinKlibCommonizerClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    kotlinKlibCommonizerClasspath,
    // CallChain[size=6] = QMyDepConf.ksp <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    ksp,
    // CallChain[size=6] = QMyDepConf.ktlint <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    ktlint,
    // CallChain[size=6] = QMyDepConf.ktlintBaselineReporter <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    ktlintBaselineReporter,
    // CallChain[size=6] = QMyDepConf.runtimeClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    runtimeClasspath,
    // CallChain[size=6] = QMyDepConf.testCompileClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    testCompileClasspath,
    // CallChain[size=6] = QMyDepConf.testRuntimeClasspath <-[Propag]- QMyDepConf <-[Ref]- QDep.QDep() <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    testRuntimeClasspath;

    // endregion #DepConf [Auto Generated by QConvert.kt]
}