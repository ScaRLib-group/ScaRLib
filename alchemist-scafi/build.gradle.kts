plugins {
    java
    scala
}

group = "io.github.davidedomini"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.scala2)
    implementation(libs.alchemist)
    implementation(libs.alchemistScafi)
    implementation(libs.alchemistProtelis)
    implementation(libs.alchemistGui)
    implementation(libs.slf4j)
    implementation(libs.logback)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    implementation(project(":scarlib-core"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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
