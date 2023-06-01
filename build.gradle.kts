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

val qMavenArtifactId = "qq-compact-lib"
val qKotlinVersion = "1.8.20"
val qJvmSourceCompatibility = "17"
val qJvmTargetCompatibility = "17"

plugins {
    val kotlinVersion = "1.8.20"
    
    application
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("maven-publish")
    
}

group = "com.github.nyabkun"

version = "v2023-06-01-alpha"

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


sourceSets.main {
    java.srcDirs("src-split")

    resources.srcDirs("rsc")
}

sourceSets.test {
    java.srcDirs("src-test-split")

    resources.srcDirs("rsc-test")
}

sourceSets.register("example") {
    java.srcDirs(
        "src-split",
        "src-example"
    )

    resources.srcDirs("rsc")
}

tasks.getByName("compileExampleKotlin").dependsOn("jar")

val exampleImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val exampleRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}


dependencies {
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.20")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

tasks {
    jar {
        enabled = false
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

    

    

    register<JavaExec>("qRunExample") {
        mainClass.set("compact.QCompactLibExampleKt")
        classpath = sourceSets.get("example").runtimeClasspath
    }
    
    getByName<Test>("test") {
        dependsOn("qRunExample")
    }
}

