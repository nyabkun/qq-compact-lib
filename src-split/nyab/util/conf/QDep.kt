/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.util.conf

import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import nyab.conf.QE
import nyab.conf.QMyDep
import nyab.conf.QMyDepConf
import nyab.conf.QMyLicense
import nyab.conf.QMyPath
import nyab.match.QM

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = QDep <-[Ref]- QCompactLib[Root]
class QDep(
    val moduleName: String,
    val moduleIdx: Int,
    val moduleDir: Path?,
    val level: QDepLevel,
    val buildGradleKts: Path?,
    val conf: QMyDepConf?,
    val group: String?,
    val name: String?,
    val version: String?,
    val license: QMyLicense?,
    val parent: String?,
    val children: List<String>,
    val libJar: Path,
    val srcZip: Path?,
    val javadocZip: Path?,
    val libDir: Path?,
    val licenseFile: Path?,
    val pomFile: Path?,
    val manifestMfFile: Path?,
    val packages: List<String>,
    val rawSrcLine: String?,
) {
    // CallChain[size=2] = QDep.checkImportPath <-[Ref]- qCreateSingleSrcBase()[Root]
    /**
     * Examine the import path to determine if this library is needed.
     */
    var checkImportPath: (importPath: String) -> Boolean = { importPath ->
        val imp = if (importPath.startsWith("import ")) {
            importPath.substring("import ".length).trim()
        } else {
            importPath
        }

        packages.any { pkg ->
            imp.startsWith(pkg)
        }
    }

    // CallChain[size=2] = QDep.isKotlinStdLib <-[Call]- QCompactLib.createBuildGradleKts()[Root]
    val isKotlinStdLib: Boolean = this.rawSrcLine?.startsWith("implementation(kotlin(\"stdlib-jdk") == true

    
}

// CallChain[size=4] = QDepLevel <-[Ref]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
enum class QDepLevel {
    // CallChain[size=5] = QDepLevel.TOP_LEVEL <-[Propag]- QDepLevel.TRANSITIVE <-[Call]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    TOP_LEVEL,
    // CallChain[size=4] = QDepLevel.TRANSITIVE <-[Call]- QMyDep.xmlParserAPIs <-[Propag]- QMyDep <-[Ref]- QCompactLib[Root]
    TRANSITIVE
}