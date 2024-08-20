plugins {
    id("build-standard-jetbrains-plugin-build")
}

// Configure "Bundling API Sources" - https://plugins.jetbrains.com/docs/intellij/bundling-plugin-openapi-sources.html#bundling-api-sources-in-gradle-build-script
tasks {
    val createOpenApiSourceJar by registering(Jar::class) {
        // Java sources
        from(sourceSets.main.get().java) {
            include("**/com/chriscarini/jetbrains/locchangecountdetector/**/*.java")
        }
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveClassifier.set("src")
    }

    buildPlugin {
        dependsOn(createOpenApiSourceJar)
        from(createOpenApiSourceJar) { into("lib/src") }
    }
}
