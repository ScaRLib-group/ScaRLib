plugins {
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
    implementation("org.scala-lang:scala3-library_3:3.2.2")
    testImplementation("org.scalatest:scalatest_3:3.2.15")
    testImplementation("org.scalatestplus:junit-4-13_3:3.2.15.0")
    implementation("org.scala-lang:scala-reflect:2.13.10")
    testImplementation("junit:junit:4.13.2")
    implementation(project(":scarlib-core"))
}
