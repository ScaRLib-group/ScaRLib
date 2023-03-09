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
    implementation("it.unibo.alchemist:alchemist:25.7.1")
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("it.unibo.alchemist:alchemist-swingui:25.7.1")
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

tasks.register<JavaExec>("runCohesionAndCollisionEval") {
    group = "try simulation"
    mainClass.set("it.unibo.experiment.cc.CohesionAndCollisionEval")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("runCohesionAndCollisionTraining") {
    group = "try simulation"
    mainClass.set("it.unibo.experiment.cc.CohesionAndCollisionTraining")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("runFollowLeaderExperiment") {
    group = "try simulation"
    mainClass.set("it.unibo.experiment.follow.FollowLeaderExperiment")
    classpath = sourceSets["main"].runtimeClasspath

/*
>>>>>>> 41757eb (build: update build gradle)
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
}*/