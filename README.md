# 🐕 qq-compact-lib

qq-compact-lib allows you to extract specific parts from your codebase and publish them as a reusable library.

This library itself is also extracted portions of my codebase.

▧ How to use :
- Just copy and paste split-src directory and gradle dependencies into your project.
- There is no release in jar format yet.
- You have to edit [QMyDep.kt](src-split/nyab/conf/QMyDep.kt) if your codebase has external library dependencies.

▧ Note :
- This library is still under development and is in the alpha stage.
- This library was originally designed for my personal use.
- Users of this library may need to make various adjustments on their end to adapt it for their sources.

▧ Mechanics :
- This program will decompose the source code into three levels of nodes.
  - **Top Level**
    - top level class / function / property
  - **Second Level**
    - Inner functions, properties, and companion objects within a top-level class
  - **Third Level**
    - function / property within a second level companion object
- When you specify a node from the user's codebase to be exposed as a library API, the program will retrieve the nodes called or referenced by that node.
- This retrieval process continues recursively, forming a chain of dependencies.
- This program uses [kotlin-compiler-embeddable](https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-compiler-embeddable) and [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) to resolve dependencies for function calls and references to classes.

## Code Example

When you run the `gralde qRunExample` command in this repository, it analyzes the sample source code under the [rsc-test/src](rsc-test/src) directory and creates a compact library that contains only the necessary classes and functions.

```kotlin
fun main(): Unit = runBlocking {
    val result = build()

    // git task includes
    // - create local git repository
    // - commit
    // - create github repository
    // - push
    // - release
//    result.doGitTask()
}

private suspend fun build(): QCompactLibResult {
    val result = qCompactLib(
        libName = "hello-world-compact",
        baseFileName = "CompactHelloWorld",
        author = "nyabkun",
    ) {
        versionUpdateStrategy = QVersionUpdateStrategy.UpdateOnlyIfSplitSrcHashChanged

        repoDescription = """Kotlin library that can say Hello."""

        readmeDescription = """Kotlin library that can say Hello."""

        versionSuffix = "-alpha"

        references = ""

        srcDirs {
            main("rsc-test/src")
            test("rsc-test/src-test")
        }

        repoTopics = listOf("kotlin-library", "hello-world", "compact-library")

        createSingleFileSrc = true

        createLibJarFromSplitFileSrc = true

        exampleSrcFile = Paths.get("rsc-test/src-example/${baseFileName}Example.kt")

        destMainSrcFileName = "$baseFileName.kt"

        destTestSrcFileName = "${baseFileName}Test.kt"

        destExampleSrcFileName = "${baseFileName}Example.kt"

        ifFileExists {
            ifExistsReadeMeFile = QIfExistsWrite.BackupAndOverwriteAtomic
        }

        readme {
            default()
        }

        rootOfChain {
            mainTopLevel {
                fileName == "${lib.baseFileName}.kt"
            }
            testTopLevel {
                fileName == "${lib.baseFileName}Test.kt"
            }
        }

        splitSrcFileImportFilter filter@{
            if (fileName == "QDep.kt" && (importPath == "nyab.util.children" || importPath == "nyab.util.parent")) {
                return@filter false
            }

            default()
        }

        singleFileAnnotation {
            """@file:Suppress("UNCHECKED_CAST")"""
        }

        topLevelElementsSorter = Comparator<QTopLevelCompactElement> { a, b ->
            a.analysisCtx.isTest.compareTo(b.analysisCtx.isTest)
        }.thenComparator { a, b ->
            a.filePath.compareTo(b.filePath)
        }.thenComparator { a, b ->
            a.lineNumber.compareTo(b.lineNumber)
        }

        nonRootNodeVisitor {
            isMustDiscard {
                if (pkgFqName.contains("q_ignore")) {
                    return@isMustDiscard true
                }

                false
            }

            isMustMark {
                isEnumEntry() || isClassInitializer() || isOverridden() || isInOpenClass() || isInAbstractClass() || hasOpenKeyword() || isCompanionObject() || isInInterface()
            }
        }

        srcCode {
            visibilityChange {

                if (compactEle.isChainRoot()) {
                    QKtVisibilityChange.NoChange
                } else if (!srcSetType.isSingleFileSelfContained) {
                    QKtVisibilityChange.ToInternal
                } else {
                    QKtVisibilityChange.ToPrivate
                }
            }
        }
    }

    return result
}
```

full source: [QCompactLibExample.kt](src-example/QCompactLibExample.kt)

## Dependency

Refer to [build.gradle.kts](build.gradle.kts) to directly check project settings.

```kotlin
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.20")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
```

## My Compact Libraries

The following libraries are all extracted from my codebase using this library.

- [qq-benchmark](https://github.com/nyabkun/qq-benchmark)
- [qq-shell-color](https://github.com/nyabkun/qq-shell-color)
- [qq-tree](https://github.com/nyabkun/qq-tree)