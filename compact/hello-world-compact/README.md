<!--- version = v2023-05-26-alpha --->

# 🐕 hello-world-compact

Kotlin library that can say Hello.

## How to use
- Just copy and paste Single-File version [CompactHelloWorld.kt](src-single/CompactHelloWorld.kt) into your project.
- Or you can use Jar version. See [Maven Dependency Section](#jar-version-maven-dependency).
- Feel free to fork or copy to your own codebase 😍

## Example

### output
<p align="center">
    
</p>

### code example

Full Source : [CompactHelloWorldExample.kt](src-example/CompactHelloWorldExample.kt)

```kotlin
CompactHelloWorld().hello()
```

Please see [CompactHelloWorldTest.kt](src-test-split/CompactHelloWorldTest.kt) for more code examples.
Single-File version [src-test-single/CompactHelloWorldTest.kt](src-test-single/CompactHelloWorldTest.kt) is a self-contained source code that includes a runnable main function.
You can easily copy and paste it into your codebase.        

## Public API

- [`CompactHelloWorld`](src-split/chaos/chaotic/CompactHelloWorld.kt#L18-L24) *class*

## Single-File version Dependency

If you copy & paste [CompactHelloWorld.kt](src-single/CompactHelloWorld.kt),
fefer to [build.gradle.kts](build.gradle.kts) to directly check project settings.

This library has no dependent libraries.


## Jar version Maven Dependency

If you prefer a jar library,
you can use [jitpack.io](https://jitpack.io/#nyabkun/hello-world-compact) repository.

### build.gradle ( Groovy )
```groovy
repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.nyabkun:hello-world-compact:v2023-05-26-alpha'
}
```

### build.gradle.kts ( Kotlin )
```kotlin
repositories {
    ...
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nyabkun:hello-world-compact:v2023-05-26-alpha")
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
        <groupId>com.github.nyabkun</groupId>
        <artifactId>hello-world-compact</artifactId>
        <version>v2023-05-26-alpha</version>
    </dependency>
</dependencies>
```

## How did I create this library

- This library was created using [qq-compact-lib](https://github.com/nyabkun/qq-compact-lib) to generates a compact, self-contained library.
- **qq-compact-lib** is a Kotlin library that can extract code elements from your codebase and make a compact library.
- It utilizes [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) to resolve function calls and class references.
- The original repository is currently being organized, and I'm gradually extracting and publishing smaller libraries.

