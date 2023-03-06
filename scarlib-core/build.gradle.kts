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
    implementation("org.scala-lang:scala-library:2.13.7")
    implementation("dev.scalapy:scalapy-core_2.13:0.5.3")
    testImplementation("org.scalatest:scalatest_3:3.2.15")
    testImplementation("org.scalatestplus:junit-4-13_3:3.2.15.0")
    testImplementation("junit:junit:4.13.2")
}
