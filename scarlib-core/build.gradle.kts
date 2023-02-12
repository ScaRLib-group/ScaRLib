plugins {
    java
    scala
}

group = "it.unibo.scarlib"


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
    implementation("org.scala-lang:scala3-library_3:3.2.2")
    implementation("dev.scalapy:scalapy-core_2.13:0.5.3")
    testImplementation("org.scalatest:scalatest_3:3.2.15")
    testImplementation("org.scalatestplus:junit-4-13_3:3.2.15.0")
    testImplementation("junit:junit:4.13.2")
}

tasks.register<JavaExec>("runTrySimulation"){
    group = "try simulation"
    mainClass.set("it.unibo.scarlib.core.TrySimulation")
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs(
        //"-Djna.library.path=/Library/Frameworks/Python.framework/Versions/3.7/lib/",
        "-Djna.library.path=/Users/davidedomini/opt/anaconda3/lib"
        //"-Dscalapy.python.library=python3.11"
    )
}