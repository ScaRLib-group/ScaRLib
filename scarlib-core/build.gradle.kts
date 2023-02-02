plugins {
    java
    scala
}

group = "it.unibo.scarlib"

scala {
    zincVersion.set("1.6.1")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala3-library_3:3.2.2")
    implementation("dev.scalapy:scalapy-core_2.13:0.5.3")
    testImplementation("junit:junit:4.13.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}