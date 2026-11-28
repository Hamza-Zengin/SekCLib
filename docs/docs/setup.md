---
sidebar_position: 1
description: Info on how to add the library to your mod
---

# Adding the library to your mod
At the moment we only have plans to support Minecraft 26.3 and NeoForge (ModDevGradle).

If you want older versions of Minecraft please check if the features you need are already supported in older releases.

## build.gradle (Maven / GitHub Packages)
```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/hamza-zengin/sekclib")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation "com.sekwah:sekclib:0.1.0"
}
```
Don't forget to also add the entry to your `META-INF/neoforge.mods.toml` file too.
```toml
[[dependencies.your_mod_id]]
    modId="sekclib"
    type="required"
    versionRange="[0.1.0,1.0.0)"
    ordering="AFTER"
    side="BOTH"
```

# Including the mod as jar in jar

If you would like to include the mod within your mod using ModDevGradle, you can add it via `jarJar`:

I only ask that you list it as an embedded library on Curse or Modrinth. (lets me see what projects are using mine ^.^)

```groovy
dependencies {
    implementation "com.sekwah:sekclib:0.1.0"
    jarJar("com.sekwah:sekclib:0.1.0")
}
```
