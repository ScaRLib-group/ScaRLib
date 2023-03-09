plugins {
    java
    //id ("org.danilopianini.publish-on-central") version "3.3.2"
    //id("org.danilopianini.git-sensitive-semantic-versioning-gradle-plugin") version "1.1.4"
}

group = "io.github.davidedomini"

repositories {
    mavenCentral()
}

/*allprojects {
    apply(plugin = "org.danilopianini.publish-on-central")
    apply(plugin = "org.danilopianini.git-sensitive-semantic-versioning-gradle-plugin")
}*/

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
/*
publishOnCentral {
    projectUrl.set("https://github.com/davidedomini/ScaRLib")
    scmConnection.set("git:git@github.com:davidedomini/ScaRLib")
    licenseName.set("MIT")
}

publishing {
    publications {
        withType<MavenPublication> {
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
}*/