plugins {
    id("java")
    id("scala")
}

group = "it.unibo.scarlib.vmas"
version = "1.6.5-dev04-577796d"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.scala2)
    implementation(libs.slf4j)
    implementation(libs.logback)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    implementation(project(":scarlib-core"))
    implementation(project(":dsl-core"))
    implementation(libs.scalapy)
}

tasks.test {
    useJUnitPlatform()
}