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

import nyab.compact.QJitpackYmlScope
import nyab.compact.QReadmeScope
import nyab.util.QFType
import nyab.util.qImgSize
import nyab.util.qList
import nyab.util.qNiceIndent
import nyab.util.qWithNewLineSurround
import nyab.util.slash

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = QMyCompactLib <-[Ref]- QCompactLib[Root]
internal object QMyCompactLib {
    // CallChain[size=2] = QMyCompactLib.jitpack_yml <-[Ref]- QJitpackYmlScope.default()[Root]
    val jitpack_yml: QJitpackYmlScope.() -> String = {
        """
        jdk:
          - ${lib.jdkForBuild}
        """.trimIndent()
    }

    // CallChain[size=2] = QMyCompactLib.git_ignore <-[Call]- QCompactLib[Root]
    val git_ignore: String = """
        # Compiled class file
        *.class
        
        # Log file
        *.log
        
        # Package Files
        *.jar
        *.war
        *.nar
        *.ear
        *.zip
        *.tar.gz
        *.rar
        
        # C/C++
        *.lib
        *.obj
        
        # Virtual machine crash logs  http://www.java.com/en/download/help/error_hotspot.xml
        hs_err_pid*
        replay_pid*
        *.hprof
        
        # Node  https://github.com/github/gitignore/blob/7ad34f7adba8546a19e3245e163e5502c338490a/Node.gitignore
        node_modules/
        
        ### Gradle
        
        !/gradle/wrapper/**
        .gradle
        /build
        /out
        
        # My settings @nyabkun
        .temp
        *gpr.key
        .q_*
        q_ignore
        /user
        /userHome
    """.trimIndent()

    // CallChain[size=2] = QMyCompactLib.src_header <-[Call]- QCompactLib[Root]
    val src_header: String = """
        /*
         * Copyright 2023. nyabkun
         *
         * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
         *
         * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
         *
         * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
         */
    """.trimIndent()

    // CallChain[size=2] = QMyCompactLib.license <-[Call]- QCompactLib[Root]
    val license = """
        MIT License

        Copyright (c) 2021 nyabkun
        
        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:
        
        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.
        
        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
    """.trimIndent()

// automatically generated by QCompactLib

    // CallChain[size=2] = QMyCompactLib.titleSection <-[Ref]- QReadmeScope.defaultTitleSection()[Root]
    val titleSection: QReadmeScope.() -> String = {
        if(lib.createSingleFileSrc) {
            """
            # ${QMyMark.README_TITLE} $libName
            
            ${lib.readmeDescription.qNiceIndent(3)}
            
            - Just copy and paste 🟦 Single-File version [${lib.destMainSrcFileName}]($destSingleMainSrcFilePath) into your project.${
                 if( lib.createLibJarFromSplitFileSrc ) {
                     "- Or you can use 🟩 Split-File Jar version. See [Maven Dependency Section](#-split-file-jar-version-maven-dependency).".qNiceIndent(3).qWithNewLineSurround()
                 } else {
                     ""
                 }
            }
            - Feel free to fork or copy to your own codebase.
            """.trimIndent()
        } else {
            """
            # ${QMyMark.README_TITLE} $libName
            
            ${lib.readmeDescription.qNiceIndent(3)}
            
            ${
                if( lib.createLibJarFromSplitFileSrc ) {
                    "- You can use 🟩 Split-File Jar version. See [Maven Dependency Section](#-split-file-jar-version-maven-dependency).".qNiceIndent(3).qWithNewLineSurround()
                } else {
                    "- Just copy and paste ${lib.destSplitMainDir}"
                }
            }
            """
        }
    }

    // CallChain[size=2] = QMyCompactLib.referenceSection <-[Ref]- QReadmeScope.defaultReferenceSection()[Root]
    val referenceSection: QReadmeScope.() -> String = {
        if( lib.references.isEmpty() ) {
            ""
        } else {
            """
                ## References
                
                ${lib.references}
            """.trimIndent()
        }
    }

    // CallChain[size=2] = QMyCompactLib.exampleSection <-[Ref]- QReadmeScope.defaultExampleSection()[Root]
    val exampleSection: QReadmeScope.() -> String = example@{
        if (exampleNonMainFunctions.isEmpty())
            return@example ""

        val seeTestFile = if (destSingleTestSrcFilePath.isNotEmpty()) {
            """
                Please see [${lib.destTestSrcFileName}](${destSplitTestSrcFilePath}) for more code examples.
                Single-File version [${destSingleTestSrcFilePath}](${destSingleTestSrcFilePath}) is a self-contained source code that includes a runnable main function.
                You can easily copy and paste it into your codebase.
            """.trimIndent()
        } else {
            ""
        }

        val images = lib.destProjDir.resolve("img").qList(QFType.File)

        val maxWidth = 500
        val elementsByLines = mutableListOf<List<String>>()
        var curLineElements = mutableListOf<String>()
        var curWidth = 0
        for (img in images) {
            val imgPath = lib.destProjDir.relativize(img)
            val imgSize = img.qImgSize()
            curWidth += imgSize.width

            if (curWidth >= maxWidth) {
                elementsByLines += curLineElements
                curWidth = imgSize.width
                curLineElements = mutableListOf()
            }

            curLineElements += """<img src="${imgPath.slash}" width="${imgSize.width}" alt="${imgPath.fileName}">"""
        }
        elementsByLines += curLineElements

        val imgSrc = if(elementsByLines.isEmpty()) {
            ""
        } else {
            elementsByLines.joinToString("\n") {
                """
                <p align="center">
                    ${it.joinToString("\n").qNiceIndent(4)}
                </p>
            """.trimIndent()
            }
        }

//    ${imgElements.qNiceIndent(4)}
        """
            ## Example
            
            ### output
            ${imgSrc.qNiceIndent(3)}

            ### code
            
            Full Source  [${lib.destExampleSrcFileName}](${destExampleSrcFilePath})
            
            ${
                if( exampleMainFunction != null ) {
                    """
                    ```kotlin
                    ${ exampleMainFunction.body.qNiceIndent(3) }
                    ```
                    """.qNiceIndent(3)
                } else {
                    ""
                }
            }
    
            ${seeTestFile.qNiceIndent(3)}        
        """.trimIndent()
    }

    // CallChain[size=2] = QMyCompactLib.aboutSection <-[Ref]- QReadmeScope.defaultAboutSection()[Root]
    val aboutSection: QReadmeScope.() -> String = {
        """
            ## How did I create this library
        
            I created this library by developing a program within my own codebase that automatically resolves dependencies at the method or property level, extracts necessary code elements, and generates a compact, self-contained, single-file library.
        
            The program uses [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) to resolve dependencies for function calls and references to classes.
        
            Although my original repository is currently disorganized, I have been gradually extracting and publishing small libraries. I also plan to prepare the original repository for publication in the future
        """.trimIndent()
    }

    // CallChain[size=2] = QMyCompactLib.dependenciesSection <-[Ref]- QReadmeScope.defaultDependenciesSection()[Root]
    val dependenciesSection: QReadmeScope.() -> String = {
        val noDep = mainDependencyLines.isEmpty()
        val noTestDep = testDependencyLines.isEmpty()

        val textLine = if (noDep && noTestDep) {
            "This library has no dependent libraries."
        } else if (noDep) {
            "This library has only test dependencies."
        } else {
            ""
        }

        val depLines = mainDependencyLines.joinToString("\n") +
                if (!noTestDep) {
                    "\n" + testDependencyLines.joinToString("\n")
                } else {
                    ""
                }

        val dependencies = if (depLines.isNotEmpty()) {
            """
                ```kotlin
                dependencies {
                    ${depLines.qNiceIndent(5)}
                }
                ```
            """.trimIndent()
        } else {
            ""
        }

        """
            ## 🟦 Single-File version Dependency
            
            If you copy & paste [${lib.destMainSrcFileName}]($destSingleMainSrcFilePath).
    
            Refer to [build.gradle.kts](build.gradle.kts) to directly check project settings.
            
            $textLine
        
            ${dependencies.qNiceIndent(3)}
        """.trimIndent()
    }

    // CallChain[size=2] = QMyCompactLib.mavenSection <-[Ref]- QReadmeScope.defaultMavenSection()[Root]
    val mavenSection: QReadmeScope.() -> String = {
        """
            ## 🟩 Split-File Jar version Maven Dependency
            
            If you prefer a jar library. Add [jitpack.io](https://jitpack.io/#${lib.gitHubUserName}/${lib.gitHubRepoName}) repository to the build script.
            
            ### build.gradle ( Groovy )
            ```groovy
            repositories {
                ...
                maven { url 'https://jitpack.io' }
            }
            
            dependencies {
                implementation 'com.github.${lib.gitHubUserName}:${lib.libName}:${version}'
            }
            ```
            
            ### build.gradle.kts ( Kotlin )
            ```kotlin
            repositories {
                ...
                maven("https://jitpack.io")
            }
            
            dependencies {
                implementation("com.github.${lib.gitHubUserName}:${lib.libName}:${version}")
            }
            ```
            
            ### pom.xml
            ```xml
            <repositories>
                ...
                <repository>
                    <id>jitpack.io</id>
                    <url>https://jitpack.io</url>
                </repository>
            </repositories>
            
            <dependencies>
                ...
                <dependency>
                    <groupId>com.github.${lib.gitHubUserName}</groupId>
                    <artifactId>${lib.libName}</artifactId>
                    <version>${version}</version>
                </dependency>
            </dependencies>
            ```
        """.trimIndent()
    }
}