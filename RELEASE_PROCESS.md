# Release checklist

1. Verify the version and Minecraft, Java, Fabric Loader, and optional Mod Menu dependencies in `gradle.properties` and `fabric.mod.json`.
2. Build with Java 21 using `./gradlew clean build`. The automated checks run as part of the build.
3. Check the remapped JAR under `build/libs` for the expected `fabric.mod.json`. Use that JAR for the Minecraft smoke tests.
4. Test locking, drops, swaps, the hotbar overlay, and restoring locks after switching profiles or worlds. Record the settings, environment, and any untested paths in [docs/TESTING.md](docs/TESTING.md).
5. Update [CHANGELOG.md](CHANGELOG.md) and compute a SHA-256 checksum for the approved JAR.
6. Publish the approved JAR and checksum with the matching Git tag. Keep test saves, logs, and local configuration out of the release.

For a Modrinth listing, check the distribution rights for the code and artwork and follow the platform's current content rules. [Modrinth Content Rules](https://modrinth.com/legal/rules)
