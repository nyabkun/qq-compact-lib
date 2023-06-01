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

import nyab.compact.QBuildGradleKtsScope
import nyab.util.qNiceIndent
import nyab.util.slash

// qq-compact-lib is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=2] = QMyCompactLibGradle <-[Ref]- QBuildGradleKtsScope.default()[Root]
internal object QMyCompactLibGradle {
    // CallChain[size=3] = QMyCompactLibGradle.base <-[Ref]- QMyCompactLibGradle.build_gradle_kts <-[Ref]- QBuildGradleKtsScope.default()[Root]
    private val base: QBuildGradleKtsScope.() -> String = {
        """
        /*
         * Copyright 2023. nyabkun
         *
         * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
         *
         * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
         *
         * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
         */

        import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
        
        println("This Gradle script starts with -Dfile.encoding=" + System.getProperty("file.encoding"))
        
        val qMavenArtifactId = "${lib.mavenArtifactId}"
        val qKotlinVersion = "${lib.kotlinVersion}"
        val qJvmSourceCompatibility = "${lib.jvmSourceCompatibility}"
        val qJvmTargetCompatibility = "${lib.jvmTargetCompatibility}"
        
        plugins {
            val kotlinVersion = "${lib.kotlinVersion}"
            
            application
            kotlin("jvm") version kotlinVersion
            kotlin("plugin.serialization") version kotlinVersion
            id("maven-publish")
            ${if (lib.enableSpotless) """id("com.diffplug.spotless") version "6.18.0"""" else ""}
        }

        group = "${lib.group}"

        version = "$version"

        repositories {
            mavenCentral()
            maven("https://jitpack.io")
        }

        java {
            toolchain {
                sourceCompatibility = JavaVersion.toVersion(qJvmSourceCompatibility)
                targetCompatibility = JavaVersion.toVersion(qJvmTargetCompatibility)
            }
        }
        
        ${
            if (lib.enableSpotless) {
                """
                    spotless {
                        kotlin {
                            target("src*/**/*.kt")
                            ktfmt()
                            trimTrailingWhitespace()
                            indentWithSpaces()
                            endWithNewline()
                        }
                    }
                """.qNiceIndent(2)
            } else {
                ""
            }
        }
    """.trimIndent()
    }

    // CallChain[size=3] = QMyCompactLibGradle.srcSets <-[Ref]- QMyCompactLibGradle.build_gradle_kts <-[Ref]- QBuildGradleKtsScope.default()[Root]
    private val srcSets: QBuildGradleKtsScope.() -> String = {
        val mainSplit = if (mainSplitMainClass.isNotEmpty()) {
            """
                application {
                    mainClass.set("$mainSplitMainClass")
                }
            """.trimIndent()
        } else {
            ""
        }

        val example = if (exampleMainClass.isNotEmpty()) {
            """
                sourceSets.register("example") {
                    ${
                        if (lib.createLibJarFromSplitFileSrc) {
                            """
                                java.srcDirs("${lib.destProjDir.relativize(lib.destExampleDir).slash}")
                                val jarFile = "${"$"}buildDir/libs/${"$"}qMavenArtifactId-${"$"}version.jar"
                                compileClasspath += files(jarFile)
                                runtimeClasspath += files(jarFile)
                            """.qNiceIndent(5)
                        } else {
                            """
                                java.srcDirs(
                                    "${lib.destProjDir.relativize(lib.destSplitMainDir).slash}",
                                    "${lib.destProjDir.relativize(lib.destExampleDir).slash}"
                                )
                            """.qNiceIndent(5)
                        }
                    }
                
                    resources.srcDirs("rsc")
                }
                
                tasks.getByName("compileExampleKotlin").dependsOn("jar")

                val exampleImplementation: Configuration by configurations.getting {
                    extendsFrom(configurations.implementation.get())
                }

                val exampleRuntimeOnly: Configuration by configurations.getting {
                    extendsFrom(configurations.runtimeOnly.get())
                }
            """.trimIndent()
        } else {
            ""
        }

        """
            ${mainSplit.qNiceIndent(3)}
            
            sourceSets.main {
                java.srcDirs("${lib.destProjDir.relativize(lib.destSplitMainDir).slash}")

                resources.srcDirs("rsc")
            }

            sourceSets.test {
                java.srcDirs("${lib.destProjDir.relativize(lib.destSplitTestDir).slash}")

                resources.srcDirs("rsc-test")
            }
            
            ${example.qNiceIndent(3)}
            
            ${
                if(lib.createSingleFileSrc) {
                    """
                        sourceSets.register("single") {
                            java.srcDirs("${lib.destProjDir.relativize(lib.destSingleMainDir).slash}")
                        }

                        val singleImplementation: Configuration by configurations.getting {
                            extendsFrom(configurations.implementation.get())
                        }

                        val singleRuntimeOnly: Configuration by configurations.getting {
                            extendsFrom(configurations.runtimeOnly.get())
                        }

                        sourceSets.register("testSingle") {
                            java.srcDirs("${lib.destProjDir.relativize(lib.destSingleTestDir).slash}")

                            resources.srcDirs("rsc-test")
                        }

                        val testSingleImplementation: Configuration by configurations.getting {
                            extendsFrom(configurations.testImplementation.get())
                        }

                        val testSingleRuntimeOnly: Configuration by configurations.getting {
                            extendsFrom(configurations.testRuntimeOnly.get())
                        }
                    """.qNiceIndent(3)
                } else {
                    ""
                }
            }
        """.trimIndent()
    }

    // CallChain[size=3] = QMyCompactLibGradle.tasks <-[Ref]- QMyCompactLibGradle.build_gradle_kts <-[Ref]- QBuildGradleKtsScope.default()[Root]
    private val tasks: QBuildGradleKtsScope.() -> String = {
        val qRunTestSplit = if (testSplitMainClass.isNotEmpty()) {
            """
                register<JavaExec>("qRunTestSplit") {
                    mainClass.set("$testSplitMainClass")
                    classpath = sourceSets.get("test").runtimeClasspath
                }
            """.trimIndent()
        } else {
            ""
        }

        val qRunTestSingle = if (lib.createSingleFileSrc && testSingleMainClass.isNotEmpty()) {
            """
                register<JavaExec>("qRunTestSingle") {
                    mainClass.set("$testSingleMainClass")
                    classpath = sourceSets.get("testSingle").runtimeClasspath
                }
            """.trimIndent()
        } else {
            ""
        }

        val qRunExample = if (exampleMainClass.isNotEmpty()) {
            """
                register<JavaExec>("qRunExample") {
                    mainClass.set("$exampleMainClass")
                    classpath = sourceSets.get("example").runtimeClasspath
                }
            """.trimIndent()
        } else {
            ""
        }

        val testDependsOn = mutableListOf<String>()

        if (lib.createSingleFileSrc && testSingleMainClass.isNotEmpty() && lib.runTest ) {
            testDependsOn += "dependsOn(\"qRunTestSingle\")"
        }
        if (testSplitMainClass.isNotEmpty() && lib.runTest ) {
            testDependsOn += "dependsOn(\"qRunTestSplit\")"
        }
        if (exampleMainClass.isNotEmpty() && lib.runExample ) {
            testDependsOn += "dependsOn(\"qRunExample\")"
        }

        val test = """
            getByName<Test>("test") {
                ${testDependsOn.joinToString("\n").qNiceIndent(4)}
            }
        """.trimIndent()

        """
        tasks {
            ${
                if( lib.createLibJarFromSplitFileSrc) {
                    """
                        jar {
                            enabled = true
                            
                            archiveBaseName.set(qMavenArtifactId)

                            from(sourceSets.main.get().output)

                            manifest {
                                attributes(
                                    "Implementation-Title" to qMavenArtifactId,
                                    "Implementation-Version" to project.version
                                )
                            }
                        }
                        
                        val qSrcJar by registering(Jar::class) {
                            archiveBaseName.set(qMavenArtifactId)
                            archiveClassifier.set("sources")
                            from(sourceSets.main.get().allSource)
                        }
                    
                        artifacts {
                            archives(qSrcJar)
                            archives(jar)
                        }
                    """.qNiceIndent(3)
                } else {
                    """
                        jar {
                            enabled = false
                        }
                    """.qNiceIndent(3)
                }
            }
            
            distZip {
                enabled = false
            }
            
            distTar {
                enabled = false
            }
            
            startScripts {
                enabled = false
            }

            withType<JavaCompile>().configureEach {
                options.encoding = "UTF-8"
            }

            withType<JavaExec>().configureEach {
                jvmArgs("-Dfile.encoding=UTF-8")
            }

            withType<KotlinCompile>().configureEach {
                kotlinOptions.jvmTarget = JavaVersion.toVersion(qJvmTargetCompatibility).toString()
            }

            ${qRunTestSplit.qNiceIndent(3)}

            ${qRunTestSingle.qNiceIndent(3)}

            ${qRunExample.qNiceIndent(3)}
            
            ${test.qNiceIndent(3)}
        }
        """
    }

    // CallChain[size=3] = QMyCompactLibGradle.publishing <-[Ref]- QMyCompactLibGradle.build_gradle_kts <-[Ref]- QBuildGradleKtsScope.default()[Root]
    val publishing: QBuildGradleKtsScope.() -> String = {
        if( lib.createLibJarFromSplitFileSrc ) {
            """
                publishing {
                    publications {
                        create<MavenPublication>("maven") {
                            artifactId = qMavenArtifactId
                           
                            from(components["kotlin"])
                
                            artifact(tasks.getByName("qSrcJar")) {
                                classifier = "sources"
                            }
                        }
                    }
                //      // GitHub Packages
                //      repositories {
                //          maven {
                //              url = uri("https://maven.pkg.github.com/${lib.gitHubUserName}/${lib.gitHubRepoName}")
                //              credentials {
                //                  username = "${lib.gitHubUserName}"
                //                  password = File("../../.q_gpr.key").readText(Charsets.UTF_8).trim()
                //              }
                //          }
                //      }
                }
            """.trimIndent()
        } else {
            ""
        }
    }

    // CallChain[size=3] = QMyCompactLibGradle.dependencies <-[Ref]- QMyCompactLibGradle.build_gradle_kts <-[Ref]- QBuildGradleKtsScope.default()[Root]
    val dependencies: QBuildGradleKtsScope.() -> String = {
        val lines = (mainCtx.dependencyLines + (testCtx?.dependencyLines ?: emptyList())).joinToString("\n")

        """
        dependencies {
            ${lines.qNiceIndent(3)}
        }
        """
    }
    // CallChain[size=2] = QMyCompactLibGradle.build_gradle_kts <-[Ref]- QBuildGradleKtsScope.default()[Root]
    val build_gradle_kts: QBuildGradleKtsScope.() -> String = {
        """
            ${base(this).qNiceIndent(3)}
            
            ${srcSets(this).qNiceIndent(3)}
            
            ${dependencies(this).qNiceIndent(3)}
            
            ${tasks(this).qNiceIndent(3)}
    
            ${publishing(this).qNiceIndent(3)}
        """.trimIndent()
    }
}