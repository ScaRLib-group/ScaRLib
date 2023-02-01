plugins {
    java
    scala
    application
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
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.scalatest:scalatest_2.13:3.3.0-SNAP3")
    testImplementation("org.scalatestplus:junit-4-13_3:3.2.15.0")
    testRuntimeOnly("org.scala-lang.modules:scala-xml_3:2.1.0")
}

/*tasks.getByName<Test>("test") {
    useJUnitPlatform()
}*/