plugins {
    java
    scala
}

group = "io.github.davidedomini"

scala {
    zincVersion.set("1.6.1")
}

sourceSets {
    main {
        scala {
            setSrcDirs(listOf("src/main/scala"))
        }
    }
    test {
        scala {
            setSrcDirs(listOf("src/test/scala"))
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.scala2)
    implementation(libs.scalapy)
    testImplementation(libs.scalaTest)
    testImplementation(libs.scalaTestPlus)
    testImplementation(libs.junit)
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Gianluca Aguzzi")
                    email.set("gianluca.aguzzi@unibo.it")
                }
            }
        }
    }
}
