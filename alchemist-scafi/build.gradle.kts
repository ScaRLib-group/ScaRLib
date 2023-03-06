plugins {
    java
    scala
}

group = "it.unibo.scarlib"

/*
scala {
    zincVersion.set("1.6.1")
}
*/

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.7")
    implementation("it.unibo.alchemist:alchemist:25.7.1")
    implementation("it.unibo.alchemist:alchemist-incarnation-scafi:25.7.1")
    implementation("it.unibo.alchemist:alchemist-incarnation-protelis:25.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation(project(":scarlib-core"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runTrySimulationAlchemistScafi") {
    group = "try simulation"
    mainClass.set("it.unibo.experiment.MySimulationExperiment")
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs(
        //"-Djna.library.path=/Library/Frameworks/Python.framework/Versions/3.7/lib/",
        "-Djna.library.path=/Users/davidedomini/opt/anaconda3/lib"
        //"-Dscalapy.python.library=python3.11"
    )
}

tasks.register<JavaExec>("runCohesionAndCollision") {
    group = "try simulation"
    mainClass.set("it.unibo.experiment.cc.CohesionAndCollisionExperiment")
    classpath = sourceSets["main"].runtimeClasspath
}