import java.io.ByteArrayOutputStream

plugins {
	kotlin("multiplatform") version "1.6.10"
	kotlin("plugin.serialization") version "1.6.10"
	application
	antlr
	idea
}

group = "com.franzmandl"
version = "1.0.0-SNAPSHOT"

repositories {
	mavenCentral()
}

kotlin {
	jvm {
		compilations.all {
			kotlinOptions.jvmTarget = "11"
		}
		withJava()
		testRuns["test"].executionTask.configure {
			useJUnitPlatform()
		}
	}
	js(LEGACY) {
		binaries.executable()
		nodejs {
			useCommonJs()
		}
	}
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
		val jvmMain by getting {
			dependencies {
				implementation("org.antlr:antlr4:4.9.3")
				implementation("org.springframework.boot:spring-boot-starter-security:2.6.3")
				implementation("org.springframework.boot:spring-boot-starter-web:2.6.3")
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation("org.assertj:assertj-core:3.22.0")
				implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
				implementation("org.springframework.boot:spring-boot-starter-test:2.6.3")
				implementation("org.springframework.security:spring-security-test:5.6.1")
			}
		}
		val jsMain by getting
		val jsTest by getting
	}
}

application {
	mainClass.set("com.franzmandl.compiler.server.ServerKt")
}

tasks.named<JavaExec>("run") {
	dependsOn(tasks.named<Jar>("jvmJar"))
	classpath(tasks.named<Jar>("jvmJar"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		allWarningsAsErrors = true
	}
}

// Adapted from https://github.com/AbhyudayaSharma/react-git-info
task("customGenerateGitInfoSource") {
	val generatedDirectory = File("${project.projectDir}/src/jvmMain/java/com/franzmandl/compiler/generated")
	generatedDirectory.mkdirs()
	val gitLogOutputStream = ByteArrayOutputStream()
	project.exec {
		commandLine = "git log --format=%D%n%h%n%H%n%cI%n%B -n 1 HEAD --".split(" ")
		standardOutput = gitLogOutputStream
	}
	val gitLogResult = gitLogOutputStream.toString().split(Regex("\r?\n"))
	val refs = gitLogResult[0].split(", ")
	val shortHash = gitLogResult[1]
	val hash = gitLogResult[2]
	val date = gitLogResult[3]
	var branch: String? = null
	val tags = mutableListOf<String>()
	for (ref in refs) {
		branch = Regex("^HEAD -> (.*)\$").matchEntire(ref)?.groups?.get(1)?.value ?: branch
		Regex("^tag: (.*)\$").matchEntire(ref)?.groups?.get(1)?.value?.let(tags::add)
	}
	fun quote(string: String?) = if (string != null) "\"$string\"" else "null"
	File(generatedDirectory, "GitInfo.java").writeText(
		"package com.franzmandl.compiler.generated;" +
				"\n" +
				"\npublic class GitInfo {" +
				"\n\tpublic static final String branch = " + quote(branch) + ";" +
				"\n\tpublic static final String shortHash = " + quote(shortHash) + ";" +
				"\n\tpublic static final String hash = " + quote(hash) + ";" +
				"\n\tpublic static final String date = " + quote(date) + ";" +
				"\n\tpublic static final String[] tags = new String[] {" + tags.joinToString(", ") { quote(it) } + "};" +
				"\n}" +
				"\n"
	)
}

task<JavaExec>("customGenerateGrammarSource") {
	mainClass.set("org.antlr.v4.Tool")
	args = listOf(
		"-no-listener",
		"-package",
		"com.franzmandl.compiler.generated",
		"-o",
		"${project.projectDir}/src/jvmMain/java/com/franzmandl/compiler/generated",
		"${project.projectDir}/src/jvmMain/antlr/Jova.g4"
	)
	classpath = configurations.compileClasspath.get()
}

task("customGenerateSource") {
	dependsOn("customGenerateGrammarSource", "customGenerateGitInfoSource")
}

tasks.named("compileKotlinJvm") {
	dependsOn("customGenerateSource")
}

// only necessary until https://youtrack.jetbrains.com/issue/KT-37964 is resolved
distributions {
	main {
		contents {
			from("$buildDir/libs") {
				rename("${rootProject.name}-jvm", rootProject.name)
				into("lib")
			}
		}
	}
}