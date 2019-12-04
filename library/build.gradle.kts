import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    id("com.android.library")
    id("maven-publish")
    id("com.jfrog.bintray").version("1.8.4")
}

val lz4Version = "1.9.2"

group = "io.maryk.lz4"
version = lz4Version

val lz4Home = projectDir.resolve("lz4/lz4-$lz4Version")

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.0")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        externalNativeBuild {
            cmake {
                targets += "liblz4"
                arguments += "-DLZ4_PATH=${lz4Home.absolutePath}/lib/"
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    externalNativeBuild {
        cmake {
            setPath("CMakeLists.txt")
            setVersion("3.10.2")
        }
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

val javadoc by tasks.creating(Javadoc::class) {
    source(android.sourceSets["main"].java.srcDirs)
    classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from(javadoc.destinationDir)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

val downloadLz4 by tasks.creating(Exec::class) {
    workingDir = projectDir
    commandLine("./downloadLz4.sh", lz4Version)
}

tasks.withType<com.android.build.gradle.tasks.ExternalNativeBuildJsonTask> {
    dependsOn(downloadLz4)
}

afterEvaluate {
    val publishTasks = mutableListOf<Jar>()

    android.libraryVariants.all { variant ->
        val name = variant.buildType.name
        if (name != com.android.builder.core.BuilderConstants.DEBUG) {
            val task = project.tasks.create<Jar>("jar${name.capitalize()}") {
                dependsOn(variant.javaCompileProvider)
                dependsOn(variant.externalNativeBuildProviders)
                from(variant.javaCompileProvider.get().destinationDir)
                from("${buildDir.absolutePath}/intermediates/library_and_local_jars_jni/$name") {
                    include("**/*.so")
                    into("lib")
                }
            }
            publishTasks.add(task)
            artifacts.add("archives", task)
        }
        true
    }

    publishing {
        publications {
            register<MavenPublication>("lz4").configure {
                artifact(sourcesJar)
                artifact(javadocJar)
                publishTasks.forEach(::artifact)
                groupId = groupId
                artifactId = "lz4-android"
                version = lz4Version

                //The publication doesn't know about our dependencies, so we have to manually add them to the pom
                pom.withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    //Iterate over the compile dependencies (we don't want the test ones), adding a <dependency> node for each
                    configurations.compile.get().allDependencies.forEach {
                        dependenciesNode.appendNode ("dependency").apply {
                            appendNode("groupId", it.group)
                            appendNode("artifactId", it.name)
                            appendNode("version", it.version)
                        }
                    }
                }
            }
        }
    }


    fun findProperty(s: String) = project.findProperty(s) as String?
    bintray {
        user = findProperty("bintrayUser")
        key = findProperty("bintrayApiKey")
        publish = true
        pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
            repo = "maven"
            name = "lz4-android"
            userOrg = "maryk"
            setLicenses("Apache-2.0")
            setPublications(*project.publishing.publications.names.toTypedArray())
            vcsUrl = "https://github.com/marykdb/lz4-android.git"
        })
    }

    project.publishing.publications.withType<MavenPublication>().forEach { publication ->
        publication.pom.withXml {
            asNode().apply {
                appendNode("name", project.name)
                appendNode("description", "lz4 compression library")
                appendNode("url", "https://github.com/marykdb/lz4-android")
                appendNode("licenses").apply {
                    appendNode("license").apply {
                        appendNode("name", "The Apache Software License, Version 2.0")
                        appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        appendNode("distribution", "repo")
                    }
                }
                appendNode("developers").apply {
                    appendNode("developer").apply {
                        appendNode("id", "jurmous")
                        appendNode("name", "Jurriaan Mous")
                    }
                }
                appendNode("scm").apply {
                    appendNode("url", "https://github.com/marykdb/lz4-android.git")
                }
            }
        }
    }
}
