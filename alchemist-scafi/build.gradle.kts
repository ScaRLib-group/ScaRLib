plugins {
    java
    scala
}

group = "io.github.davidedomini"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.7")
    implementation("it.unibo.alchemist:alchemist:25.7.2")
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("it.unibo.alchemist:alchemist-swingui:25.7.1")
    implementation("it.unibo.alchemist:alchemist-incarnation-scafi:25.7.1")
    implementation("it.unibo.alchemist:alchemist-incarnation-protelis:25.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation(project(":scarlib-core"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
