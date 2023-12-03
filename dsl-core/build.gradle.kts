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
    implementation(libs.scala2)
    implementation(libs.scalaReflect)
    testImplementation(libs.scalaTest)
    testImplementation(libs.scalaTestPlus)
    testImplementation(libs.junit)
    implementation(project(":scarlib-core"))
}
