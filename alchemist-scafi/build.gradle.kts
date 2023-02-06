plugins {
    java
}

group = "it.unibo.scarlib"

repositories {
    mavenCentral()
}

dependencies {
    implementation("it.unibo.alchemist:alchemist:25.7.1")
    implementation("it.unibo.alchemist:alchemist-incarnation-scafi:25.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}