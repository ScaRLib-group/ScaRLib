const config = require('semantic-release-preconfigured-conventional-commits')

// Commands necessary to the release phase release
const publishCommands = `
./gradlew assemble --parallel || exit 1
git tag -a -f \${nextRelease.version} \${nextRelease.version} -F CHANGELOG.md || exit 2
git push --force origin \${nextRelease.version} || exit 3
./gradlew clean build
./gradlew uploadAllPublicationsToMavenCentralNexus releaseStagingRepositoryOnMavenCentral
`
// Only release on branch main
const releaseBranches = ["main"]

config.branches = releaseBranches

config.plugins.push(
    // Custom release
    ["@semantic-release/exec", {
        "publishCmd": publishCommands,
    }],
    // Release also in GitHub
    ["@semantic-release/github", {
        "assets": [
            { "path": "build/libs/*.jar" },
        ]
    }],
    ["@semantic-release/git", {
        "assets": ["CHANGELOG.md", "package.json"],
        "message": "chore(release)!: [skip ci] ${nextRelease.version} released"
    }],
)

// JS Semantic Release configuration must export the JS configuration object
module.exports = config