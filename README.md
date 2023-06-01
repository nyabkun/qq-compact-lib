# 🐕 qq-compact-lib

qq-compact-lib allows you to extract specific parts from your codebase and publish them as a reusable library.

This library itself is also extracted portions of my codebase.

## How to use
- Just copy and paste split-src directory and gradle dependencies into your project
- There is no release in jar format yet
- You have to edit [QMyDep.kt](src-split/nyab/conf/QMyDep.kt), if you want to automatically add external library dependencies

## Note
- This library is still under development and is in the alpha stage
- This library was originally designed for my personal use
- Users of this library may need to make various adjustments on their end to adapt it for their sources

## Mechanics
- This program will decompose the source code into three levels of nodes.
  - **Top Level**
    - top level class / function / property
  - **Second Level**
    - Inner functions, properties, and companion objects within a top-level class
  - **Third Level**
    - function / property within a second level companion object
- When you specify a public api node from your project, the program will automatically retrieve the dependent nodes.
- This retrieval process continues recursively, forming a chain of call or reference dependencies.
- This program uses [kotlin-compiler-embeddable](https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-compiler-embeddable) and [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) to resolve dependencies for function calls and references to classes.

## Code Example

When you run the `gralde qRunExample` command in this repository, it analyzes the sample source code under the [rsc-test/src](rsc-test/src) directory and creates a compact library that contains only the necessary classes and functions.

```kotlin
fun main(): Unit = runBlocking {
  // build includes
  // - analyse src dirs
  // - create compact src code
  //   - single-file version
  //   - split-file version
  // - create test src code ( if exists )
  //   - single-file version
  //   - split-file version
  // - create example src code
  // - create jars
  // - create build.gradle.kts
  // - create README.md, LICENSE, .gitignore, etc
  // - run test ( if exists )
  // - run example
  val result = build()

//    if( result.buildSuccess ) {
//        // - create GitHub repository
//        result.createGitHubRepo(
//            result.lib.author,
//            result.lib.libName,
//            result.lib.readmeDescription,
//            listOf("kotlin-library", "hello-world", "compact-library")
//        )
//
//        // - add / commit / push
//        // - release jars
//        result.release("[auto] Reflect the changes of main repository.")
//    }
}

private suspend fun build(): QCompactLibResult {
  val result = qCompactLib(
    libName = "hello-world-compact",
    author = "nyabkun",
  ) {
    baseFileName("CompactHelloWorld")
    exampleSrcFile = Paths.get("rsc-test/src-example/CompactHelloWorldExample.kt")

    versionUpdateStrategy = QVersionUpdateStrategy.UpdateOnlyIfSplitSrcHashChanged

    readmeDescription = """Kotlin library that can say Hello."""

    versionSuffix = "-alpha"

    references = ""

    srcDirs {
      main("rsc-test/src")
      test("rsc-test/src-test")
    }

    createSingleFileSrc = true

    createLibJarFromSplitFileSrc = true

    ifFileExists {
      ifExistsReadMeFile = QIfExistsWrite.BackupAndOverwriteAtomic
//                        ifExistsReadeMeFile = QIfExistsWrite.DoNothing
    }

    readme {
      default()
    }

    splitSrcFileImportFilter filter@{
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
  implementation("org.apache.httpcomponents:httpclient:4.5.14")
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