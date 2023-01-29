plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.1"
}

rootProject.name = "ScaRLib"
include("dsl-core")

gitHooks {
    commitMsg {
        conventionalCommits()
    }
    createHooks(true)
}