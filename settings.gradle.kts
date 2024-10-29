plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.3"
}

rootProject.name = "ScaRLib"

gitHooks {
    commitMsg {
        conventionalCommits()
    }
    createHooks(true)
}

include("scarlib-core")
include("dsl-core")
include("alchemist-scafi")
include("vmas")
