plugins {
    java
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.shadow)
    signing
}

group = "io.github.davidedomini"

repositories {
    mavenCentral()
}

allprojects {

    apply(plugin = "org.danilopianini.publish-on-central")
    apply(plugin = "org.danilopianini.git-sensitive-semantic-versioning-gradle-plugin")
    apply(plugin = "java")
    apply(plugin = "scala")

    gitSemVer {
        buildMetadataSeparator.set("-")
        maxVersionLength.set(20)
    }

    val sourceJar by tasks.registering(Jar::class) {
        from(sourceSets.named("main").get().allSource)
        archiveClassifier.set("sources-${project.name}")
    }

    val scaladocJar by tasks.registering(Jar::class) {
        dependsOn("scaladoc")
        val destinationDirectory = tasks.named<ScalaDoc>("scaladoc").get().destinationDir
        from(destinationDirectory)
        archiveClassifier.set("docs-${project.name}")
    }

    publishOnCentral {
        projectUrl.set("https://github.com/davidedomini/ScaRLib")
        scmConnection.set("git:git@github.com:davidedomini/ScaRLib")
        licenseName.set("GNU GENERAL PUBLIC LICENSE")
    }

    publishing {
        publications {
            withType<MavenPublication> {
                artifact(sourceJar)
                artifact(scaladocJar)
                pom {
                    developers {
                        developer {
                            name.set("Davide Domini")
                            email.set("davide.domini@studio.unibo.it")
                            url.set("https://davidedomini.github.io/")
                        }
                        developer {
                            name.set("Filippo Cavallari")
                            email.set("filippo.cavallari2@studio.unibo.it")
                            url.set("https://filocava99.github.io/mypage/")
                        }
                    }
                }
            }
        }
    }

    if(System.getenv("CI") == true.toString()) {
        signing {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }
}

dependencies {
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
